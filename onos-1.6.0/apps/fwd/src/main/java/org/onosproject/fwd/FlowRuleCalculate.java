package org.onosproject.fwd;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.bmv2.api.context.Bmv2ActionModel;
import org.onosproject.bmv2.api.context.Bmv2Configuration;
import org.onosproject.bmv2.api.context.Bmv2DefaultConfiguration;
import org.onosproject.bmv2.api.runtime.Bmv2Action;
import org.onosproject.bmv2.api.runtime.Bmv2DeviceAgent;
import org.onosproject.bmv2.api.runtime.Bmv2ExtensionSelector;
import org.onosproject.bmv2.api.runtime.Bmv2ExtensionTreatment;
import org.onosproject.bmv2.api.service.Bmv2Controller;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.ExtensionSelector;
import org.onosproject.net.flow.instructions.ExtensionTreatment;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by root on 18-1-18.
 */
public class FlowRuleCalculate {

    public Bmv2Controller controller;
    ApplicationId myAppId;
    JsonObject json;
    private final Logger log = getLogger(getClass());


    public _AR AR;
    //TODO:所有的都可以
    public _XX XX;
    public _RM RM;

    FlowRuleCalculate(Bmv2Controller controller, ApplicationId myAppId, JsonObject json) {
        this.controller = controller;
        this.myAppId = myAppId;
        this.json = json;
        //System.out.println("Create a FlowRuleCalculate");
        AR = new _AR();
        XX = new _XX();
        RM = new _RM();
    }

    public String name_to_deviceID(String name) {
        JsonObject nameToDeviceID = json.get("nameToDeviceID").asObject();
        String deviceID = nameToDeviceID.get(name).asString();
        return deviceID;
    }

    public class _XX {
        //TODO:For IR/AR
        public FlowRule dstAddr_port__set_egress_port(String name, int dstAddr, int port) throws Exception {

            String deviceID = name_to_deviceID(name);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);

            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();

            Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

            HashMap<String, Integer> NameToIndex = new HashMap<String, Integer>();

            for (int i = 0; i < configNow.tables().size(); i++) {
                NameToIndex.put(configNow.table(i).name(), i);
            }

            Bmv2DefaultConfiguration myConfiguration = Bmv2DefaultConfiguration.parse(json);

            ExtensionSelector extSelector = Bmv2ExtensionSelector.builder()
                    .forConfiguration(myConfiguration)
                    .matchExact("ipv4", "dstAddr", dstAddr)
                    .build();

            ExtensionTreatment extTreatment = Bmv2ExtensionTreatment.builder()
                    .forConfiguration(myConfiguration)
                    .setActionName("set_egress_port")
                    .addParameter("port", port)
                    .build();

            TrafficSelector selector = DefaultTrafficSelector.builder()
                    .extension(extSelector, myDeviceId)
                    .build();

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .extension(extTreatment, myDeviceId)
                    .build();

            FlowRule rule = DefaultFlowRule.builder()
                    .forDevice(myDeviceId)
                    .fromApp(myAppId)
                    .forTable(NameToIndex.get("dstAddr_port"))
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .makePermanent()
                    .withPriority(100)
                    .build();

            return rule;
        }

        //TODO:给某一个设备下发全部的默认选项是，_drop（）
        public void set_all_default_entry() throws Exception {

            JsonObject nameToDeviceID = json.get("nameToDeviceID").asObject();
            List<String> names = nameToDeviceID.names();

            for (String name : names) {
                //TODO:如果不是网络设备直接跳过
                if (nameToDeviceID.get(name).asString().equals("")) {
                    continue;
                }
                //TODO:说明是一个bmv2设备
                log.info("{}() bmv2 {} set drop", _FUNC_(), name);
                String deviceID = name_to_deviceID(name);
                DeviceId myDeviceId = DeviceId.deviceId(deviceID);
                Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);
                String JsonString = agent.dumpJsonConfig();
                JsonObject json = Json.parse(JsonString).asObject();

                Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

                String tableName = "dstAddr_port";
                String actionName = "_drop";
                Bmv2Action action = Bmv2Action.builder().withName(actionName).build();

                if(configNow.table(tableName) == null){
                    //TODO:都没有这个表
                    continue;
                }

                Set<Bmv2ActionModel> actions = configNow.table(tableName).actions();
                //TODO：需要有drop动作的表才可以下发默认的动作
                boolean hasDrop = false;
                for (Bmv2ActionModel a : actions) {
                    if (a.name().toString().equals(actionName)) {
                        hasDrop = true;
                    }
                }
                if (hasDrop) {
                    agent.setTableDefaultAction(configNow.table(tableName).name(), action);
                }

            }
        }

        //TODO:给某一个设备下发全部的默认选项是，_drop（）
        public void table_set_default(String DeviceName, String actionName, String tableName) throws Exception {
            //TODO:说明是一个bmv2设备
            log.info("{}() bmv2 {} set drop", _FUNC_(), DeviceName);
            String deviceID = name_to_deviceID(DeviceName);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);

            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();

            Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

            Bmv2Action action = Bmv2Action.builder().withName(actionName).build();
            Set<Bmv2ActionModel> actions = configNow.table(tableName).actions();
            //TODO：需要有drop动作的表才可以下发默认的动作
            boolean has = false;
            for (Bmv2ActionModel a : actions) {
                if (a.name().toString().equals(actionName)) {
                    has = true;
                }
            }
            if (has) {
                agent.setTableDefaultAction(configNow.table(tableName).name(), action);
            }

        }

    }

    public class _AR {
        //TODO: For AR
        public FlowRule protocol_dstAddr__send_to_cpu(String name) throws Exception {

            String deviceID = name_to_deviceID(name);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);
            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();
            Bmv2DefaultConfiguration myConfiguration = Bmv2DefaultConfiguration.parse(json);

            HashMap<String, Integer> NameToIndex = new HashMap<String, Integer>();
            for (int i = 0; i < myConfiguration.tables().size(); i++) {
                NameToIndex.put(myConfiguration.table(i).name(), i);
            }

            ExtensionSelector extSelector = Bmv2ExtensionSelector.builder()
                    .forConfiguration(myConfiguration)
                    .matchExact("ipv4", "protocol", 0xA2)
                    .build();

            ExtensionTreatment extTreatment = Bmv2ExtensionTreatment.builder()
                    .forConfiguration(myConfiguration)
                    .setActionName("send_to_cpu")
                    .build();

            TrafficSelector selector = DefaultTrafficSelector.builder()
                    .extension(extSelector, myDeviceId)
                    .build();

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .extension(extTreatment, myDeviceId)
                    .build();

            FlowRule rule = DefaultFlowRule.builder()
                    .forDevice(myDeviceId)
                    .fromApp(myAppId)
                    .forTable(NameToIndex.get("protocol_dstAddr"))
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .makePermanent()
                    .withPriority(10)
                    .build();

            return rule;
        }

        //TODO: For AR
        public FlowRule protocol_dstAddr__set_dstAddr(String name,byte protocol, int dstAddr) throws Exception {

            String deviceID = name_to_deviceID(name);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);

            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();

            Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

            HashMap<String, Integer> NameToIndex = new HashMap<String, Integer>();

            for (int i = 0; i < configNow.tables().size(); i++) {
                NameToIndex.put(configNow.table(i).name(), i);
            }

            Bmv2DefaultConfiguration myConfiguration = Bmv2DefaultConfiguration.parse(json);

            ExtensionSelector extSelector = Bmv2ExtensionSelector.builder()
                    .forConfiguration(myConfiguration)
                    .matchExact("ipv4", "protocol", protocol)
                    .build();

            ExtensionTreatment extTreatment = Bmv2ExtensionTreatment.builder()
                    .forConfiguration(myConfiguration)
                    .setActionName("set_dstAddr")
                    .addParameter("dstAddr", dstAddr)
                    .build();

            TrafficSelector selector = DefaultTrafficSelector.builder()
                    .extension(extSelector, myDeviceId)
                    .build();

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .extension(extTreatment, myDeviceId)
                    .build();

            FlowRule rule = DefaultFlowRule.builder()
                    .forDevice(myDeviceId)
                    .fromApp(myAppId)
                    .forTable(NameToIndex.get("protocol_dstAddr"))
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .makePermanent()
                    .withPriority(100)
                    .build();

            return rule;
        }

        //TODO: For AR
        public FlowRule get_protocol_dstAddr__set_dstAddr(String name,byte protocol, int dstAddr) throws Exception {

            String deviceID = name_to_deviceID(name);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);

            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();

            Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

            HashMap<String, Integer> NameToIndex = new HashMap<String, Integer>();

            for (int i = 0; i < configNow.tables().size(); i++) {
                NameToIndex.put(configNow.table(i).name(), i);
            }

            Bmv2DefaultConfiguration myConfiguration = Bmv2DefaultConfiguration.parse(json);

            ExtensionSelector extSelector = Bmv2ExtensionSelector.builder()
                    .forConfiguration(myConfiguration)
                    .matchExact("ipv4", "protocol", protocol)
                    .build();

            ExtensionTreatment extTreatment = Bmv2ExtensionTreatment.builder()
                    .forConfiguration(myConfiguration)
                    .setActionName("set_dstAddr")
                    .addParameter("dstAddr", dstAddr)
                    .build();

            TrafficSelector selector = DefaultTrafficSelector.builder()
                    .extension(extSelector, myDeviceId)
                    .build();

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .extension(extTreatment, myDeviceId)
                    .build();

            FlowRule rule = DefaultFlowRule.builder()
                    .forDevice(myDeviceId)
                    .fromApp(myAppId)
                    .forTable(NameToIndex.get("get_protocol_dstAddr"))
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .makePermanent()
                    .withPriority(100)
                    .build();

            return rule;
        }

        public FlowRule data_nidC_dstAddr__set_dstAddr(String name,byte[] nidC, int dstAddr) throws Exception {

            String deviceID = name_to_deviceID(name);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);

            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();

            Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

            HashMap<String, Integer> NameToIndex = new HashMap<String, Integer>();

            for (int i = 0; i < configNow.tables().size(); i++) {
                NameToIndex.put(configNow.table(i).name(), i);
            }

            Bmv2DefaultConfiguration myConfiguration = Bmv2DefaultConfiguration.parse(json);

            ExtensionSelector extSelector = Bmv2ExtensionSelector.builder()
                    .forConfiguration(myConfiguration)
                    .matchExact("data", "nid_c", nidC)
                    .build();

            ExtensionTreatment extTreatment = Bmv2ExtensionTreatment.builder()
                    .forConfiguration(myConfiguration)
                    .setActionName("set_dstAddr")
                    .addParameter("dstAddr", dstAddr)
                    .build();

            TrafficSelector selector = DefaultTrafficSelector.builder()
                    .extension(extSelector, myDeviceId)
                    .build();

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .extension(extTreatment, myDeviceId)
                    .build();

            FlowRule rule = DefaultFlowRule.builder()
                    .forDevice(myDeviceId)
                    .fromApp(myAppId)
                    .forTable(NameToIndex.get("data_nidC_dstAddr"))
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .makePermanent()
                    .withPriority(100)
                    .build();

            return rule;
        }
    }


    public class _RM {

        //TODO:给RM的发送的表
        public FlowRule sid_nid__inside_sid_set(String DeviceName,byte[] sid,byte res, int srcAddr, int dstAddr) throws Exception {
            String deviceID = name_to_deviceID(DeviceName);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);

            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();

            Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

            HashMap<String, Integer> NameToIndex = new HashMap<String, Integer>();

            for (int i = 0; i < configNow.tables().size(); i++) {
                NameToIndex.put(configNow.table(i).name(), i);
            }

            Bmv2DefaultConfiguration myConfiguration = Bmv2DefaultConfiguration.parse(json);

            ExtensionSelector extSelector = Bmv2ExtensionSelector.builder()
                    .forConfiguration(myConfiguration)
                    .matchExact("get", "sid", sid)
                    .build();

            ExtensionTreatment extTreatment = Bmv2ExtensionTreatment.builder()
                    .forConfiguration(myConfiguration)
                    .setActionName("inside_sid_set")
                    .addParameter("res",res)
                    .addParameter("srcAddr",srcAddr)
                    .addParameter("dstAddr",dstAddr)
                    .build();

            TrafficSelector selector = DefaultTrafficSelector.builder()
                    .extension(extSelector, myDeviceId)
                    .build();

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .extension(extTreatment, myDeviceId)
                    .build();

            FlowRule rule = DefaultFlowRule.builder()
                    .forDevice(myDeviceId)
                    .fromApp(myAppId)
                    .forTable(NameToIndex.get("sid_nid"))
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .makePermanent()
                    .withPriority(100)
                    .build();

            return rule;
        }


        //TODO:给RM的发送的表
        public FlowRule protocol_to_cpu__send_to_cpu(String name,byte protocol) throws Exception {
            String deviceID = name_to_deviceID(name);
            DeviceId myDeviceId = DeviceId.deviceId(deviceID);
            Bmv2DeviceAgent agent = controller.getAgent(myDeviceId);

            String JsonString = agent.dumpJsonConfig();
            JsonObject json = Json.parse(JsonString).asObject();

            Bmv2DefaultConfiguration configNow = Bmv2DefaultConfiguration.parse(json);

            HashMap<String, Integer> NameToIndex = new HashMap<String, Integer>();

            for (int i = 0; i < configNow.tables().size(); i++) {
                NameToIndex.put(configNow.table(i).name(), i);
            }

            Bmv2DefaultConfiguration myConfiguration = Bmv2DefaultConfiguration.parse(json);

            ExtensionSelector extSelector = Bmv2ExtensionSelector.builder()
                    .forConfiguration(myConfiguration)
                    .matchExact("ipv4", "protocol", protocol)
                    .build();

            ExtensionTreatment extTreatment = Bmv2ExtensionTreatment.builder()
                    .forConfiguration(myConfiguration)
                    .setActionName("send_to_cpu")
                    .build();

            TrafficSelector selector = DefaultTrafficSelector.builder()
                    .extension(extSelector, myDeviceId)
                    .build();

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .extension(extTreatment, myDeviceId)
                    .build();

            FlowRule rule = DefaultFlowRule.builder()
                    .forDevice(myDeviceId)
                    .fromApp(myAppId)
                    .forTable(NameToIndex.get("protocol_to_cpu"))
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .makePermanent()
                    .withPriority(100)
                    .build();

            return rule;
        }

    }

    //TODO:打印函数名字
    public static String _FUNC_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        return traceElement.getMethodName();
    }

}
