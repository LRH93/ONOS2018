# echo"sudo ln /usr/bin/ovs-controller /usr/bin/controller"
# coding=utf8
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.log import setLogLevel, info
from mininet.cli import CLI
from mininet.util import dumpNodeConnections

import json
import sys

import argparse
from time import sleep


class MyTopo(Topo):
    def __init__(self):
        # initilaize topology
        Topo.__init__(self)

        # add hosts and switches
        #        h1 = self.addHost( 'h1',ip="172.168.1.1/24",mac="00:04:00:00:00:0A",privateDirs=[ '~/h1' ])
        #        h2 = self.addHost( 'h2',ip="172.168.1.8/24",mac="00:04:00:00:00:0B",privateDirs=[ '~/h2' ])
        #        h3 = self.addHost( 'h3',ip="172.168.1.5/24",mac="00:04:00:00:00:0C",privateDirs=[ '~/h3' ])

        #        switch1 = self.addSwitch( 's1' )
        #        switch2 = self.addSwitch( 's2' )

        # TODO:创建3台主机
        client1 = self.addHost('client1', ip="192.168.10.2/24", mac="00:04:00:00:00:02")
        server1 = self.addHost('server1', ip="192.168.10.3/24", mac="00:04:00:00:00:03")
        server2 = self.addHost('server2', ip="192.168.20.3/24", mac="00:04:00:00:00:03")

        # TODO:创建4台BMv2
        RM1 = self.addSwitch('RM1')
        BR1 = self.addSwitch('BR1')
        RM2 = self.addSwitch('RM2')
        BR2 = self.addSwitch('BR2')

        '''
            # add links
            self.addLink(h1,switch1,1,1)
            self.addLink(switch1,h3,2,1)
            self.addLink(switch1,switch2,3,1)
            self.addLink(switch2,h2,2,1)
            #self.addLink(h1,h3,2,2)
            '''

        # TODO:增加连接关系
        self.addLink(RM1, client1, 1, 1)
        self.addLink(RM1, server1, 2, 1)
        self.addLink(RM1, BR1, 3, 1)
        self.addLink(BR1, BR2, 2, 2)
        self.addLink(BR2, RM2, 1, 1)
        self.addLink(RM2, server2, 2, 1)
        self.addLink(BR1, client1, 3, 2)
        self.addLink(BR2, server2, 3, 2)


# sudo mn --custom first.py --topo mytopo
# topos = { 'mytopo': ( lambda: MyTopo() ) }
# ip link delete s1-eth1 type veth


"""
h1 xterm
https://github.com/mininet/mininet/wiki/Introduction-to-Mininet

http://mininet.org/api/annotated.html

Host Configuration Methods

Mininet hosts provide a number of convenience methods for network configuration:

    IP(): Return IP address of a host or specific interface.
    MAC(): Return MAC address of a host or specific interface.
    setARP(): Add a static ARP entry to a host's ARP cache.
    setIP(): Set the IP address for a host or specific interface.
    setMAC(): Set the MAC address for a host or specific interface
"""


def main():
    print "_____liuruohan_____"
    net = Mininet(topo=None, build=False)

    fname = "/root/onos-p4-dev/p4src/config.json"

    try:
        fobj = open(fname, "r");
    except IOError, e:
        print "open file error";

    content = fobj.read()
    obj = json.loads(content)

    nameToDeviceID = obj["nameToDeviceID"]

    all = {}
    for key in nameToDeviceID:
        print key, nameToDeviceID[key]
        if nameToDeviceID[key] != "":
            # 说明这是一个bmv2设备
            all[key] = net.addSwitch(key.encode("utf-8"))
            print "add bmv2 %s" % key
            pass
        else:
            all[key] = net.addHost(key.encode("utf-8"))
            print "add host %s" % key

    #Client1 = net.addHost( 'Client1')
    #Server1 = net.addHost( 'Server1')
    #all["Client1"]=net.addHost( 'Client1')
    #all["Server1"]=net.addHost( 'Server1')

    #RM1 = net.addSwitch( 'RM1' )
    #BR1 = net.addSwitch( 'BR1' )
    #BR2 = net.addSwitch( 'BR2' )
    #AR1 = net.addSwitch( 'AR1' )
    #IR1 = net.addSwitch( 'IR1' )
    #IR2 = net.addSwitch( 'IR2' )
    #IR3 = net.addSwitch( 'IR3' )

    """
    all["RM1"]=net.addSwitch( "RM1" )
    all["BR1"]=net.addSwitch( 'BR1' )
    all["BR2"]=net.addSwitch( 'BR2' )
    all["AR1"]=net.addSwitch( 'AR1' )
    all["IR1"]=net.addSwitch( 'IR1' )
    all["IR2"]=net.addSwitch( 'IR2' )
    all["IR3"]=net.addSwitch( 'IR3' )
    """
    # print all

    connection = obj["connection"]

    once = {}
    for key in connection:
        # print key,connection[key]
        for key2 in connection[key]:
            if (key + key2 in once or key2 + key in once) == False:
                key_port = int(connection[key][key2]["port"])
                if key2 in connection:
                    key2_port = int(connection[key2][key]["port"])
                else:
                    key2_port = 0

                print key, key2, key_port, key2_port
                once[key + key2] = 1;
                once[key2 + key] = 1;
                net.addLink(all[key], all[key2], key_port, key2_port)



    """
    # TODO:增加连接关系
    net.addLink(RM1,IR2,0,0)
    net.addLink(IR2,IR1,1,0)
    net.addLink(IR2,BR1,3,0)
    net.addLink(IR2,IR3,2,0)
    net.addLink(IR1,BR2,2,0)
    net.addLink(IR1,AR1,1,1)
    net.addLink(IR3,AR1,1,0)
    net.addLink(AR1,Server1,2,0)
    net.addLink(AR1,Client1,3,0)
   """
    net.start()

    # os.popen('ovs-vsctl add-port s1 eth0')

    print "\n"
    print "Dumping host connections"
    dumpNodeConnections(net.hosts)
    print "Testing network connectivity"
    net.pingAll()
    """
    #首先根据名字从网络中获取主机
    h1 = net.get("h1")
    #对获取的主机执行命令
    h1.setIP("192.168.0.99",intf="%s-eth1"%h1.name)
    h1.setMAC("00:04:00:00:00:FF",intf="%s-eth1"%h1.name)
    h1.cmd("wireshark")
    """

    """
    s1=net.get("s1")
    s1.setIP("172.168.1.2",intf="%s-eth1"%s1.name)
    s1.setIP("172.168.1.3",intf="%s-eth2"%s1.name)
    s1.setIP("172.168.1.4",intf="%s-eth3"%s1.name)

    s2=net.get("s2")
    s2.setIP("172.168.1.6",intf="%s-eth1"%s2.name)
    s2.setIP("172.168.1.7",intf="%s-eth2"%s2.name)
    """

    """
    num_hosts=2
    for n in xrange(num_hosts):
        h = net.get('h%d' % (n + 1))
        print h.setIP("192.168.0.%d"%(n+1),intf="%s-eth2"%h.name)
        print "\n"+h.name
        #h.cmd("ifconfig %s-eth2 211.71.67.%d netmask 255.255.255.0"%( h.name, (n+1) ) )
        #print h.cmd("ifconfig")
        print "privateDirs="+str(h.privateDirs)
        #h.cmd("wireshark")
    """

    print "Initial Configuration"
    print net.hosts

    CLI(net)
    net.stop()


if __name__ == '__main__':
    setLogLevel('info')
    main()
