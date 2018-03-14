package org.onosproject.fwd;

import com.eclipsesource.json.JsonObject;
import org.apache.commons.lang.ObjectUtils;
import org.onosproject.net.Link;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PathCalculate {

    public class MyLink {
        public int port; //出端口
        public int cost; //链路长度

        MyLink(int port, int cost) {
            this.cost = cost;
            this.port = port;
        }

        @Override
        public String toString() {
            String res = new String();
            res += " port:" + Integer.toString(this.port) + " ";
            res += "cost:" + Integer.toString(this.cost) + " ";
            return res;
        }
    }

    public class Node {
        public HashMap<String, MyLink> childs ; //所有的孩子节点

        Node() {
            childs = new HashMap<String, MyLink>();
        }

        public void AddChild(String DeviceId, MyLink mylink) {
            childs.put(DeviceId, mylink);
        }
    }

    HashMap<String, Node> nodes;


    PathCalculate() {
        nodes = new HashMap<String, Node>();
    }

    public void addNode(String deviceId, Node node) {
        nodes.put(deviceId, node);
    }

    //TODO：从Json对象中解析连接信息
    public void addNodeFromJson(JsonObject json) {

        JsonObject defaultJson = json.get("nameToDeviceID").asObject();
        JsonObject connection = json.get("connection").asObject();

        //TODO:初始化所有的节点
        List<String> deviceStrings = defaultJson.names();
        for (String deviceString : deviceStrings) {
            nodes.put(deviceString, new Node());
            //System.out.println("++" + deviceString);
        }

        //TODO:为每个节点增加孩子节点
        deviceStrings = connection.names();
        for (String deviceString : deviceStrings) {
            JsonObject deviceItem = connection.get(deviceString).asObject();
            List<String> childList = deviceItem.names();
            for (String child : childList) {
                JsonObject link = deviceItem.get(child).asObject();
                int p = Integer.parseInt(link.get("port").asString());
                int c = Integer.parseInt(link.get("cost").asString());
                MyLink mylink = new MyLink(p, c);
                nodes.get(deviceString).AddChild(child, mylink);
                //System.out.println(deviceString + " has a child " + child + mylink.toString());
            }
        }
    }

    private int minCost;
    private LinkedList<String> minPath;
    private void _dfs(int totalCost,LinkedList<String> pathNodes,String dst){
        String last = pathNodes.getLast();
        HashMap<String,MyLink> childs = nodes.get(last).childs;

        for(String child:childs.keySet()){

            MyLink myLink = childs.get(child);
            //孩子节点是终点
            if(child.equals(dst)){

                //是最短路径
                if(myLink.cost+totalCost < minCost){
                    LinkedList<String> possiblePath= new LinkedList<String>(pathNodes);
                    possiblePath.add(child);
                    minPath=possiblePath;
                    minCost=myLink.cost+totalCost;
                }
            }else{
                //没有走过的点
                if(!pathNodes.contains(child)){
                    LinkedList<String> nextPath= new LinkedList<String>(pathNodes);
                    nextPath.add(child);
                    _dfs(totalCost+myLink.cost,nextPath,dst);
                }
            }
        }
    }

    public class Name_port{
        //TODO:ports的数目比names少一个，最后一个没有出端口
        LinkedList<String> names;
        LinkedList<Integer> ports;
        int size;
        Name_port(){
            this.names = new LinkedList<String>();
            this.ports = new LinkedList<Integer>();
            this.size = 0;
        }
        public void addName(String name){
            this.names.add(name);
            this.size++;
        }
        public void addPorts(int port){
            this.ports.add(port);
        }
    }

    public HashMap<String,Integer>  LinkedList_convert_to_HashMap(Name_port name_port){
        HashMap<String,Integer> res = new HashMap<>();
        for(int i=0;i<name_port.size;i++){
            res.put(name_port.names.get(i),name_port.ports.get(i));
        }
        return res;
    }

    public Name_port findMinPath(String src,String dst){

        //初始化空路径和长度值
        if( minPath== null){
            minPath = new LinkedList<String>();
        }else{
            minPath.clear();
        }
        minCost=Integer.MAX_VALUE;

        LinkedList<String> pathNodes = new LinkedList<String>();
        pathNodes.add(src);


        _dfs(0,pathNodes,dst);

        Name_port name_port = new Name_port();

        for(int i=0;i<minPath.size();i++){
            if(i!=minPath.size()-1){
                String node1=minPath.get(i);
                String node2=minPath.get(i+1);
                int port = nodes.get(node1).childs.get(node2).port;
                //System.out.print(minPath.get(i)+"[port:"+ port +"] -> ");
                name_port.addName(minPath.get(i));
                name_port.addPorts(port);
            }else{
                //System.out.print(minPath.get(i));
                name_port.addName(minPath.get(i));
                //TODO:终点没有出端口，表示-1
                name_port.addPorts(-1);
            }
        }
        //System.out.println();

        return name_port;
    }
}
