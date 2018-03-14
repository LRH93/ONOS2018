package org.onosproject.fwd;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.MacAddress;
import org.onosproject.bmv2.api.service.Bmv2Controller;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.onosproject.fwd.ReactiveForwarding.byteMerger;
import static org.onosproject.net.PortNumber.FLOOD;
import static org.onosproject.net.PortNumber.portNumber;
import static org.onosproject.net.flow.DefaultTrafficTreatment.emptyTreatment;
import static org.onosproject.net.flow.instructions.Instruction.Type.OUTPUT;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by root on 18-2-4.
 */
public class PacketParser {

    private final Logger log = getLogger(getClass());
    //TODO:这个是存储config.json的
    public HashMap<String, String> nameToDeviceID;
    public HashMap<String, String> deviceIDToName;

    public HashMap<String, String> nameToNID;
    public HashMap<String, String> NIDToName;

    public Bmv2Controller controller;
    //TODO:用来存储注册的信息
    public HashMap<String, String> registerInfo;
    JsonObject json;
    FlowRuleService flowRuleService;
    ApplicationId myAppId;
    private long counter;
    private _AR AR;
    private _BR BR;
    private _RM RM;
    private int RMIP;


    public static final int len_sid = 36;
    public static final int len_nid = 16;


    //发包用
    protected DeviceService deviceService;

    private HashMap<String, Integer> ReceiveCounter;

    PacketParser(Bmv2Controller controller, ApplicationId myAppId, FlowRuleService flowRuleService, DeviceService deviceService) {

        counter = 0;
        nameToDeviceID = new HashMap<>();
        deviceIDToName = new HashMap<>();
        nameToNID = new HashMap<>();
        NIDToName = new HashMap<>();

        registerInfo = new HashMap<>();

        this.AR = new _AR();
        this.BR = new _BR();
        this.RM = new _RM();

        String FileName = "/config.json";
        try {
            this.json = Json.parse(new BufferedReader(new InputStreamReader(
                    this.getClass().getResourceAsStream(FileName)))).asObject();

            JsonObject nameToDeviceIDJson = json.get("nameToDeviceID").asObject();
            List<String> names = nameToDeviceIDJson.names();
            for (String name : names) {
                String deviceID = nameToDeviceIDJson.get(name).asString();
                nameToDeviceID.put(name, deviceID);
                if (!deviceID.equals("")) {
                    deviceIDToName.put(deviceID, name);
                    //log.info("{}() {}->{}",_FUNC_(),name,deviceID);
                }
            }

            JsonObject nameToNIDJson = json.get("nameToNID").asObject();
            names = nameToDeviceIDJson.names();
            for(String name:names){
                String NID = nameToNIDJson.get(name).asString();
                nameToNID.put(name,NID);
                NIDToName.put(NID,name);
            }
        } catch (Exception e) {
            log.info("{}() Exception{}", _FUNC_(), e.toString());
        }

        this.RMIP = get_IP_byName("RM1");
        this.controller = controller;
        this.myAppId = myAppId;
        this.flowRuleService = flowRuleService;

        this.ReceiveCounter = new HashMap<>();
        this.deviceService = deviceService;
    }

    //TODO:打印函数名字
    public static String _FUNC_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        return traceElement.getMethodName();
    }

    //TODO：默认RM1为本域是IP
    public int get_IP_byName(String deviceName) {
        int res = 0;
        JsonObject nameToIp = json.get("nameToIp").asObject();
        String IP_String = nameToIp.get(deviceName).asString();
        String[] strings = IP_String.split("\\.");
        for (String string : strings) {
            //System.out.println(string);
            res = res * 256 + Integer.parseInt(string);
        }
        //System.out.println(res+"__");
        return res;
    }

    boolean extract(PacketContext context) {

        boolean isCoLoR = false;
        counter++;
        //TODO:首先判断是谁发来的包
        DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
        log.info("{}() {} {} counter={}", _FUNC_(), deviceId, deviceIDToName.get(deviceId.toString()), counter);

        String port = context.inPacket().receivedFrom().port().name();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间

        String name = deviceIDToName.get(deviceId.toString());
        _bmv2 bmv2;
        if (name.charAt(0) == 'R' && name.charAt(1) == 'M') {
            System.out.println("From RM type bmv2 " + name + " " + port);
            bmv2 = this.RM;
        } else if (name.charAt(0) == 'A' && name.charAt(1) == 'R') {
            System.out.println("From AR type bmv2 " + name + " " + port);
            bmv2 = this.AR;
        } else if (name.charAt(0) == 'B' && name.charAt(1) == 'R') {
            System.out.println("From BR type bmv2 " + name + " " + port);
            bmv2 = this.BR;
        } else {
            System.out.println("Unknow type bmv2" + " " + port);
            //TODO：CoLoR设备无法解析的东西，我不处理了！
            return false;
        }

        InboundPacket pkt = context.inPacket();
        ByteBuffer originalBytebuffer = pkt.unparsed();

        //TODO:判断是什么类型的数据包
        //TODO:越过Mac和Ip
        int macLen = 14;
        int ipLen = 20;
        for (int i = 0; i < macLen + ipLen; i++) {
            //弹出数据，直到所需要的CoLoR字段
            originalBytebuffer.get();
        }
        byte versionType = originalBytebuffer.get();

        byte getType = (byte) 0xA0;
        byte dataType = (byte) 0xA1;
        byte registerType = (byte) 0xA2; //TODO：暂时用旧的数据包格式

        if (versionType == getType) {
            isCoLoR = true;
            log.info("{}() {}", _FUNC_(), "GET packet");
            bmv2.processGET(context, name);
        } else if (versionType == dataType) {
            isCoLoR = true;
            log.info("{}() {}", _FUNC_(), "DATA packet");
            bmv2.processDATA(context, name);
        } else if (versionType == registerType) {
            isCoLoR = true;
            log.info("{}() {}", _FUNC_(), "REGISTER packet");
            bmv2.processREGISTER(context, name);
        } else {
            log.info("{}() {}", _FUNC_(), "not CoLoR Type");
            return false;
        }


        if (ReceiveCounter.containsKey(name)) {
            int counter = ReceiveCounter.get(name);
            ReceiveCounter.put(name, counter + 1);
        } else {
            ReceiveCounter.put(name, 1);
        }
        System.out.println(ReceiveCounter.toString());

        return isCoLoR;
    }

    /*
      //TODO:这个是用来发包的
     */


    //TODO:自己写的函数，可以把byteBuffer发送出去
    public void send_byteBuffer(ByteBuffer byteBuffer, String DeviceName, int port) {
        DeviceId deviceId = DeviceId.deviceId(nameToDeviceID.get(DeviceName));

        TrafficTreatment treatment = null;
        DefaultOutboundPacket outPacket =
                new DefaultOutboundPacket(deviceId,
                        treatment,
                        byteBuffer);

        Bmv2PacketContext bmv2PacketContext = new Bmv2PacketContext(123L, null, outPacket, false);
        bmv2PacketContext.treatmentBuilder().setOutput(portNumber(port));

        //TODO:注释掉，暂时不发包
        bmv2PacketContext.send();
    }

    private class Bmv2PacketContext extends DefaultPacketContext {

        Bmv2PacketContext(long time, InboundPacket inPkt, OutboundPacket outPkt, boolean block) {
            super(time, inPkt, outPkt, block);
        }

        public void emit(OutboundPacket packet) {
            if (packet != null) {
                DeviceId deviceId = packet.sendThrough();
                Device device = deviceService.getDevice(deviceId);
                if (device.is(PacketProgrammable.class)) {
                    PacketProgrammable packetProgrammable = device.as(PacketProgrammable.class);
                    packetProgrammable.emit(packet);
                } else {
                    log.info("No PacketProgrammable behavior for device {}", deviceId);
                }
            }
        }

        @Override
        public void send() {

            if (this.block()) {
                log.info("Unable to send, packet context not blocked");
                return;
            }

            DeviceId deviceId = outPacket().sendThrough();
            ByteBuffer rawData = outPacket().data();

            TrafficTreatment treatment;
            if (outPacket().treatment() == null) {
                treatment = (treatmentBuilder() == null) ? emptyTreatment() : treatmentBuilder().build();
            } else {
                treatment = outPacket().treatment();
            }

            // BMv2 doesn't support FLOOD for packet-outs.
            // Workaround here is to perform multiple emits, one for each device port != packet inPort.
            Optional<Instructions.OutputInstruction> floodInst = treatment.allInstructions()
                    .stream()
                    .filter(i -> i.type().equals(OUTPUT))
                    .map(i -> (Instructions.OutputInstruction) i)
                    .filter(i -> i.port().equals(FLOOD))
                    .findAny();

            if (floodInst.isPresent() && treatment.allInstructions().size() == 1) {
                // Only one instruction and is FLOOD. Do the trick.
                PortNumber inPort = inPacket().receivedFrom().port();
                deviceService.getPorts(outPacket().sendThrough())
                        .stream()
                        .map(Port::number)
                        .filter(port -> !port.equals(inPort))
                        .map(outPort -> DefaultTrafficTreatment.builder().setOutput(outPort).build())
                        .map(outTreatment -> new DefaultOutboundPacket(deviceId, outTreatment, rawData))
                        .forEach(this::emit);
            } else {
                // Not FLOOD treatment, what to do is up to driver.
                emit(new DefaultOutboundPacket(deviceId, treatment, rawData));
            }
        }

        //TODO:使用方法
        /*
            //（1）构造MAC包头
            MacAddress MACsrc = MacAddress.valueOf("11:11:22:22:33:33");
            MacAddress MACdst = MacAddress.valueOf("44:44:55:55:66:66");
            Ethernet eth = new Ethernet()
                    .setDestinationMACAddress(MACdst)
                    .setSourceMACAddress(MACsrc)
                    .setEtherType((short) 0x0800);

            //（2）构造IP包头
            IPv4 iPv4 = new IPv4()
                    .setVersion((byte) 0x45)
                    .setDestinationAddress(IPv4.toIPv4Address("192.168.1.2"))
                    .setSourceAddress(IPv4.toIPv4Address("172.16.17.123"));

            //（3）数据字段
            String content = new String("ONOS send a packet");

            //把上面三者合并，并转化成bytebuffer
            byte[] data = content.getBytes();
            ByteBuffer byteBuffer = ByteBuffer.wrap(
                    byteMerger(
                            byteMerger(
                                    eth.serialize(),
                                    iPv4.serialize()
                            ),
                            data
                    )
            );

            // 打印bytebuffer
            for (int j = 0; j < byteBuffer.capacity(); j++) {
                System.out.printf("%2x", byteBuffer.get(j));
            }
            System.out.println();

            DeviceId deviceId = DeviceId.deviceId(nameToDeviceID.get("AR1"));
            ConnectPoint connectPoint = new ConnectPoint(deviceId, PortNumber.portNumber(1));
            DefaultInboundPacket inPacket =
                    new DefaultInboundPacket(connectPoint,
                            eth,
                            byteBuffer);

            TrafficTreatment treatment = null;
            DefaultOutboundPacket outPacket =
                    new DefaultOutboundPacket(deviceId,
                            treatment,
                            byteBuffer);

            Bmv2PacketContext bmv2PacketContext = new Bmv2PacketContext(123L, inPacket, outPacket, false);
            bmv2PacketContext.treatmentBuilder().setOutput(portNumber(1));

            //TODO:注释掉，暂时不发包
            bmv2PacketContext.send();
         */
    }

    //TODO:抽象类接口
    public interface _bmv2 {
        public void processGET(PacketContext context, String DeviceName);

        public void processDATA(PacketContext context, String DeviceName);

        public void processREGISTER(PacketContext context, String DeviceName);
    }

    public class _AR implements _bmv2 {

        @Override
        public void processGET(PacketContext context, String DeviceName) {
            //TODO：算出一条路径，下发给所有的路径上的bmv2设备
            PathCalculate pathCalculate = new PathCalculate();
            pathCalculate.addNodeFromJson(json);
            PathCalculate.Name_port name_port = pathCalculate.findMinPath(DeviceName, "RM1");

            FlowRuleCalculate flowRuleCalculate = new FlowRuleCalculate(controller, myAppId, json);
            //TODO:最后一个地址不要
            for (int i = 0; i < name_port.size - 1; i++) {
                String node = name_port.names.get(i);
                try {
                    FlowRule flowRule = flowRuleCalculate.XX.dstAddr_port__set_egress_port(node, RMIP, name_port.ports.get(i));
                    flowRuleService.applyFlowRules(flowRule);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                System.out.println(node + " to RM[" + RMIP + "] go through " + name_port.ports.get(i));
            }
            //TODO：转化成映射，好转化
            HashMap<String, Integer> name_2_port = pathCalculate.LinkedList_convert_to_HashMap(name_port);

            try {
                FlowRule flowRule = flowRuleCalculate.AR.get_protocol_dstAddr__set_dstAddr(DeviceName,BufferCalculate.getType ,RMIP);
                flowRuleService.applyFlowRules(flowRule);
            } catch (Exception e) {
                System.out.println(e.toString());
            }

            ByteBuffer byteBuffer = context.inPacket().unparsed();

            BufferCalculate bufferCalculate = new BufferCalculate(byteBuffer);

            bufferCalculate.set__ipv4__dstAddr(RMIP);
            ByteBuffer buf = bufferCalculate.getBuf();

            send_byteBuffer(buf, DeviceName, name_2_port.get(DeviceName));
        }

        @Override
        public void processDATA(PacketContext context, String DeviceName) {
            System.out.printf("AR process %s!!!!\n", _FUNC_());
            ByteBuffer byteBuffer = context.inPacket().unparsed();
            BufferCalculate bufferCalculate = new BufferCalculate(byteBuffer);
            byte[] nidC = bufferCalculate.ColoRData.nidC;
            String temp;
            try {
                temp = new String(nidC, "UTF-8");
            } catch (Exception e) {
                temp = "Error";
                return;
            }
            String client = NIDToName.get(temp);
            System.out.println("get packets came from: "+client);
            PathCalculate pathCalculate = new PathCalculate();
            pathCalculate.addNodeFromJson(json);
            PathCalculate.Name_port name_port = pathCalculate.findMinPath(DeviceName, client);
            FlowRuleCalculate flowRuleCalculate = new FlowRuleCalculate(controller, myAppId, json);
            //TODO:最后一个地址不要
            for (int i = 0; i < name_port.size - 1; i++) {
                String node = name_port.names.get(i);
                try {
                    FlowRule flowRule = flowRuleCalculate.XX.dstAddr_port__set_egress_port(node, get_IP_byName(client), name_port.ports.get(i));
                    flowRuleService.applyFlowRules(flowRule);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                System.out.println("Data packets: "+node + " to clinet[" + get_IP_byName(client) + "] go through " + name_port.ports.get(i));
            }

            //TODO:准备下发流表
            try {
                FlowRule flowRule = flowRuleCalculate.AR.data_nidC_dstAddr__set_dstAddr(DeviceName,nidC ,get_IP_byName(client));
                flowRuleService.applyFlowRules(flowRule);
            } catch (Exception e) {
                System.out.println(e.toString());
            }

            bufferCalculate.set__ipv4__dstAddr(get_IP_byName(client));
            ByteBuffer buf = bufferCalculate.getBuf();
            //TODO：转化成映射，好转化
            HashMap<String, Integer> name_2_port = pathCalculate.LinkedList_convert_to_HashMap(name_port);
            send_byteBuffer(buf, DeviceName, name_2_port.get(DeviceName));
        }

        @Override
        public void processREGISTER(PacketContext context, String DeviceName) {
            //TODO：算出一条路径，下发给所有的路径上的bmv2设备
            PathCalculate pathCalculate = new PathCalculate();
            pathCalculate.addNodeFromJson(json);
            PathCalculate.Name_port name_port = pathCalculate.findMinPath(DeviceName, "RM1");

            FlowRuleCalculate flowRuleCalculate = new FlowRuleCalculate(controller, myAppId, json);
            //TODO:最后一个地址不要
            for (int i = 0; i < name_port.size - 1; i++) {
                String node = name_port.names.get(i);
                try {
                    FlowRule flowRule = flowRuleCalculate.XX.dstAddr_port__set_egress_port(node, RMIP, name_port.ports.get(i));
                    flowRuleService.applyFlowRules(flowRule);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                System.out.println(node + " to RM[" + RMIP + "] go through " + name_port.ports.get(i));
            }
            //TODO：转化成映射，好转化
            HashMap<String, Integer> name_2_port = pathCalculate.LinkedList_convert_to_HashMap(name_port);

            try {
                FlowRule flowRule = flowRuleCalculate.AR.protocol_dstAddr__set_dstAddr(DeviceName,BufferCalculate.registerType, RMIP);
                flowRuleService.applyFlowRules(flowRule);
            } catch (Exception e) {
                System.out.println(e.toString());
            }

            ByteBuffer byteBuffer = context.inPacket().unparsed();

            BufferCalculate bufferCalculate = new BufferCalculate(byteBuffer);

            bufferCalculate.set__ipv4__dstAddr(RMIP);
            ByteBuffer buf = bufferCalculate.getBuf();

            send_byteBuffer(buf, DeviceName, name_2_port.get(DeviceName));

        }
    }


    public class _RM implements _bmv2 {

        @Override
        public void processGET(PacketContext context, String DeviceName) {
            System.out.printf("RM process %s!!!!\n", _FUNC_());
            ByteBuffer byteBuffer = context.inPacket().unparsed();

            BufferCalculate bufferCalculate = new BufferCalculate(byteBuffer);
            byte[] sid = bufferCalculate.CoLoRGet.sid;
            String key;
            try {
                key = new String(sid, "UTF-8");
            } catch (Exception e) {
                key = "Error";
            }
            System.out.println(key);
            if(registerInfo.containsKey(key)){
                String registerNID = registerInfo.get(key);
                System.out.printf("SID[%s] is registered by NID[%s]\n",key,registerInfo.get(key) );
                String Server = NIDToName.get(registerNID);
                System.out.printf("The server is [%s]\n",Server);

                PathCalculate pathCalculate = new PathCalculate();
                pathCalculate.addNodeFromJson(json);
                PathCalculate.Name_port name_port = pathCalculate.findMinPath(DeviceName,Server);

                int dstIP = get_IP_byName(Server);
                System.out.println(dstIP);
                FlowRuleCalculate flowRuleCalculate = new FlowRuleCalculate(controller, myAppId, json);
                for(int i=0;i<name_port.size-1;i++){
                    String node = name_port.names.get(i);
                    try {
                        FlowRule flowRule = flowRuleCalculate.XX.dstAddr_port__set_egress_port(node, dstIP, name_port.ports.get(i));
                        flowRuleService.applyFlowRules(flowRule);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                    System.out.printf("[%s] go to %s[%s] to through [%d]\n",node,Server,dstIP,name_port.ports.get(i));
                }
                //TODO：转化成映射，好转化
                HashMap<String, Integer> name_2_port = pathCalculate.LinkedList_convert_to_HashMap(name_port);
                bufferCalculate.set__ipv4__srcAddr(get_IP_byName(DeviceName));
                bufferCalculate.set__ipv4__dstAddr(get_IP_byName(Server));
                bufferCalculate.set__get__res(1);
                ByteBuffer buf = bufferCalculate.getBuf();

                BufferCalculate bufferCalculate1 =  new BufferCalculate(buf);
                //System.out.printf("res = %d\n",bufferCalculate1.CoLoRGet.res);

                //TODO：用流表的方式下发
                try {
                    byte res =1;
                    FlowRule flowRule = flowRuleCalculate.RM.sid_nid__inside_sid_set(DeviceName,sid,res,get_IP_byName(DeviceName),get_IP_byName(Server));
                    flowRuleService.applyFlowRules(flowRule);
                }catch (Exception e){
                    System.out.println(e.toString());
                }


                send_byteBuffer(buf, DeviceName, name_2_port.get(DeviceName));

            }else {
                System.out.printf("SID[%s]NOT FOUND!\n",key);
            }
        }

        @Override
        public void processDATA(PacketContext context, String DeviceName) {

        }

        @Override
        public void processREGISTER(PacketContext context, String DeviceName) {
            System.out.printf("RM process %s!!!!\n", _FUNC_());
            ByteBuffer byteBuffer = context.inPacket().unparsed();

            BufferCalculate bufferCalculate = new BufferCalculate(byteBuffer);

            byte[] sid = bufferCalculate.CoLoRRegister.sid;
            byte[] nids = bufferCalculate.CoLoRRegister.nidS;

            String key, value;
            try {
                key = new String(sid, "UTF-8");
                value = new String(nids, "UTF-8");
            } catch (Exception e) {
                key = "Error";
                value = "Error";
            }

            registerInfo.put(key, value);
            System.out.println("Register Information:");
            for (String a : registerInfo.keySet()) {
                System.out.printf("SID[%s] register by NID[%s]\n", a, registerInfo.get(a));
            }
        }
    }

    public class _BR implements _bmv2 {

        @Override
        public void processGET(PacketContext context, String DeviceName) {

        }

        @Override
        public void processDATA(PacketContext context, String DeviceName) {

        }

        @Override
        public void processREGISTER(PacketContext context, String DeviceName) {

        }
    }

}
