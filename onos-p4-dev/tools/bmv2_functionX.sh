#start a bmv2
bmv2(){
echo "The format of input:  bmv2 port"

if [ $1 -eq 40017 ];then
#[use] p4nmsg 40017 
       echo 40017 > /tmp/bmv2-40017-thrift-port
       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \
       --device-id 17 \
       -i 0@AR1-eth0 -i 1@AR1-eth1 -i 2@AR1-eth2 -i 3@AR1-eth3 \
       --thrift-port 40017 \
       --nanolog ipc:///tmp/bmv2-17-log.ipc --debugger \
       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\
       --controller-ip 127.0.0.1 --controller-port 40123
fi
if [ $1 -eq 40011 ];then
#[use] p4nmsg 40011 
       echo 40011 > /tmp/bmv2-40011-thrift-port
       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \
       --device-id 11 \
       -i 0@RM1-eth0 \
       --thrift-port 40011 \
       --nanolog ipc:///tmp/bmv2-11-log.ipc --debugger \
       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\
       --controller-ip 127.0.0.1 --controller-port 40123
fi
if [ $1 -eq 40012 ];then
#[use] p4nmsg 40012 
       echo 40012 > /tmp/bmv2-40012-thrift-port
       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \
       --device-id 12 \
       -i 0@IR1-eth0 -i 1@IR1-eth1 -i 2@IR1-eth2 \
       --thrift-port 40012 \
       --nanolog ipc:///tmp/bmv2-12-log.ipc --debugger \
       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\
       --controller-ip 127.0.0.1 --controller-port 40123
fi
if [ $1 -eq 40013 ];then
#[use] p4nmsg 40013 
       echo 40013 > /tmp/bmv2-40013-thrift-port
       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \
       --device-id 13 \
       -i 0@IR2-eth0 -i 1@IR2-eth1 -i 2@IR2-eth2 -i 3@IR2-eth3 \
       --thrift-port 40013 \
       --nanolog ipc:///tmp/bmv2-13-log.ipc --debugger \
       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\
       --controller-ip 127.0.0.1 --controller-port 40123
fi
if [ $1 -eq 40014 ];then
#[use] p4nmsg 40014 
       echo 40014 > /tmp/bmv2-40014-thrift-port
       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \
       --device-id 14 \
       -i 0@IR3-eth0 -i 1@IR3-eth1 \
       --thrift-port 40014 \
       --nanolog ipc:///tmp/bmv2-14-log.ipc --debugger \
       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\
       --controller-ip 127.0.0.1 --controller-port 40123
fi
if [ $1 -eq 40016 ];then
#[use] p4nmsg 40016 
       echo 40016 > /tmp/bmv2-40016-thrift-port
       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \
       --device-id 16 \
       -i 0@BR2-eth0 \
       --thrift-port 40016 \
       --nanolog ipc:///tmp/bmv2-16-log.ipc --debugger \
       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\
       --controller-ip 127.0.0.1 --controller-port 40123
fi
if [ $1 -eq 40015 ];then
#[use] p4nmsg 40015 
       echo 40015 > /tmp/bmv2-40015-thrift-port
       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \
       --device-id 15 \
       -i 0@BR1-eth0 \
       --thrift-port 40015 \
       --nanolog ipc:///tmp/bmv2-15-log.ipc --debugger \
       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\
       --controller-ip 127.0.0.1 --controller-port 40123
fi
}


bmv2all(){
    echo "start all bmv2!!!"
    echo "bmv2 40017 &"
    bmv2 40017 &
    echo "bmv2 40011 &"
    bmv2 40011 &
    echo "bmv2 40012 &"
    bmv2 40012 &
    echo "bmv2 40013 &"
    bmv2 40013 &
    echo "bmv2 40014 &"
    bmv2 40014 &
    echo "bmv2 40016 &"
    bmv2 40016 &
    echo "bmv2 40015 &"
    bmv2 40015 &
}


cliname(){

echo "cliname  deviceName"

if [ $1 == "AR1" ];then
     echo "cli 40017"
     cli 40017
fi
if [ $1 == "RM1" ];then
     echo "cli 40011"
     cli 40011
fi
if [ $1 == "IR1" ];then
     echo "cli 40012"
     cli 40012
fi
if [ $1 == "IR2" ];then
     echo "cli 40013"
     cli 40013
fi
if [ $1 == "IR3" ];then
     echo "cli 40014"
     cli 40014
fi
if [ $1 == "BR2" ];then
     echo "cli 40016"
     cli 40016
fi
if [ $1 == "BR1" ];then
     echo "cli 40015"
     cli 40015
fi
}


logname(){

echo "logname  deviceName"

if [ $1 == "AR1" ];then
     echo "p4nmsg 40017"
     p4nmsg 40017
fi
if [ $1 == "RM1" ];then
     echo "p4nmsg 40011"
     p4nmsg 40011
fi
if [ $1 == "IR1" ];then
     echo "p4nmsg 40012"
     p4nmsg 40012
fi
if [ $1 == "IR2" ];then
     echo "p4nmsg 40013"
     p4nmsg 40013
fi
if [ $1 == "IR3" ];then
     echo "p4nmsg 40014"
     p4nmsg 40014
fi
if [ $1 == "BR2" ];then
     echo "p4nmsg 40016"
     p4nmsg 40016
fi
if [ $1 == "BR1" ];then
     echo "p4nmsg 40015"
     p4nmsg 40015
fi
}


bmv2name(){

echo "bmv2name  deviceName"

if [ $1 == "AR1" ];then
     echo "bmv2 40017"
     bmv2 40017
fi
if [ $1 == "RM1" ];then
     echo "bmv2 40011"
     bmv2 40011
fi
if [ $1 == "IR1" ];then
     echo "bmv2 40012"
     bmv2 40012
fi
if [ $1 == "IR2" ];then
     echo "bmv2 40013"
     bmv2 40013
fi
if [ $1 == "IR3" ];then
     echo "bmv2 40014"
     bmv2 40014
fi
if [ $1 == "BR2" ];then
     echo "bmv2 40016"
     bmv2 40016
fi
if [ $1 == "BR1" ];then
     echo "bmv2 40015"
     bmv2 40015
fi
}
