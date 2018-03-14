import json
import sys


def generate_a_bmv2(devicdId, deviceName, portNumber, thriftPort):
    str = "\n"
    str += "if [ $1 -eq %d ];then" % (thriftPort);
    str += "\n#[use] p4nmsg %d " % (thriftPort)
    str += "\n       echo %d > /tmp/bmv2-%d-thrift-port" % (thriftPort, thriftPort);
    str += "\n       $onos_p4_dev_ROOT/onos-bmv2/targets/simple_switch/simple_switch \\";
    str += "\n       --device-id %d \\" % (devicdId);
    str += "\n       "
    for i in range(portNumber):
        str += "-i %d@%s-eth%d " % (i, deviceName, i)
    str += "\\"
    str += "\n       --thrift-port %d \\" % (thriftPort)
    str += "\n       --nanolog ipc:///tmp/bmv2-%d-log.ipc --debugger \\" % (devicdId);
    str += "\n       --log-console -Lwarn $onos_p4_dev_ROOT/p4src/build/empty.json --\\";
    str += "\n       --controller-ip 127.0.0.1 --controller-port 40123"
    str += "\nfi"
    return str


onos_p4_dev_ROOT = sys.argv[1]

fname = onos_p4_dev_ROOT + "/p4src/config.json"

try:
    fobj = open(fname, "r");
except IOError, e:
    print "epen file error"

content = fobj.read()

obj = json.loads(content)
# print obj

for key in obj:
    for key2 in obj[key]:
        pass
        # print obj[key][key2]

output = onos_p4_dev_ROOT + "/tools/bmv2_functionX.sh"
try:
    fout = open(output, "w+");
except IOError, e:
    print e.strerror

fout.write("#start a bmv2\n");
fout.write("bmv2(){\n");
fout.write("echo \"The format of input:  bmv2 port\"\n")

connection = obj["connection"]
nameToDeviceID = obj["nameToDeviceID"]
for key in connection:
    deviceName = key
    portNumber = len(connection[key])
    solve = nameToDeviceID[key].split(':')[2].split('#')
    thriftPort = int(solve[0])
    devicdId = int(solve[1])
    fout.write(generate_a_bmv2(devicdId, deviceName, portNumber, thriftPort))
fout.write("\n}\n");


fout.write("\n\nbmv2all(){\n");
fout.write("    echo \"start all bmv2!!!\"");
for key in connection:
    solve = nameToDeviceID[key].split(':')[2].split('#')
    thriftPort = int(solve[0])
    fout.write("\n    echo \"bmv2 %s &\""%thriftPort)
    fout.write("\n    bmv2 %s &"%thriftPort)
fout.write("\n}\n");


fout.write("\n\ncliname(){\n");
fout.write("\necho \"cliname  deviceName\"\n");
str = "\n"
for key in connection:
    solve = nameToDeviceID[key].split(':')[2].split('#')
    deviceName = key
    thriftPort = int(solve[0])
    str = ""
    str += "\nif [ $1 == \"%s\" ];then" % (deviceName);
    str += "\n     echo \"cli %d\""%thriftPort
    str += "\n     cli %d"%thriftPort
    str += "\nfi"
    fout.write(str)
fout.write("\n}\n");


fout.write("\n\nlogname(){\n");
fout.write("\necho \"logname  deviceName\"\n");
str = "\n"
for key in connection:
    solve = nameToDeviceID[key].split(':')[2].split('#')
    deviceName = key
    thriftPort = int(solve[0])
    str = ""
    str += "\nif [ $1 == \"%s\" ];then" % (deviceName);
    str += "\n     echo \"p4nmsg %d\""%thriftPort
    str += "\n     p4nmsg %d"%thriftPort
    str += "\nfi"
    fout.write(str)
fout.write("\n}\n");


fout.write("\n\nbmv2name(){\n");
fout.write("\necho \"bmv2name  deviceName\"\n");
str = "\n"
for key in connection:
    solve = nameToDeviceID[key].split(':')[2].split('#')
    deviceName = key
    thriftPort = int(solve[0])
    str = ""
    str += "\nif [ $1 == \"%s\" ];then" % (deviceName);
    str += "\n     echo \"bmv2 %d\""%thriftPort
    str += "\n     bmv2 %d"%thriftPort
    str += "\nfi"
    fout.write(str)
fout.write("\n}\n");

# close the file
fout.close()
