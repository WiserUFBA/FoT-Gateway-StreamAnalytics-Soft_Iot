/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fotstream.soft_iot.gateway.edgent.controller;

import br.ufba.dcc.wiser.fotstream.soft_iot.gateway.model.FoTDeviceStream;
import br.ufba.dcc.wiser.fotstream.soft_iot.gateway.model.FoTSensorStream;
import br.ufba.dcc.wiser.fotstream.soft_iot.gateway.util.UtilDebug;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.edgent.connectors.mqtt.MqttConfig;
import org.apache.edgent.topology.Topology;

/**
 *
 * @author Brenno 
 */
public class StreamControllerImpl {

    
    private String serverHost;
    private String serverId;
    private String port;
    private MqttConfig mqttConfig;
    private String username;
    private String password;
    private boolean debugModeValue;
    private String jsonDevices;
    private Topology topology;
    private List<FoTDeviceStream> listFoTDeviceStream;
    private int defaultCollectionTime;
    private int defaultPublishingTime;
    private Path pathLog;
    private String bootstrapServers = "localhost:9092";
    private ControllerEdgent controllerEdgent;
    private String gatewayID;
    
    public StreamControllerImpl(){
        
    }
    
    public void init(){
       
        try{
            
            UtilDebug.printDebugConsole("Init FoT-StreamGateway Controller");
            UtilDebug.printDebugConsole(this.serverHost + " " + this.port);
            UtilDebug.printDebugConsole(this.jsonDevices);
            
            this.mqttConfig = new MqttConfig(this.serverHost + this.port, this.serverId);
            if(!this.username.isEmpty())
                this.mqttConfig.setUserName(username);
            if(!this.password.isEmpty())
                this.mqttConfig.setPassword(password.toCharArray());
            
            this.listFoTDeviceStream = new ArrayList<>();
            
            this.controllerEdgent = new ControllerEdgent();
            this.topology = controllerEdgent.createTopology();
            loadFoTDeviceStream();
            
            
            controllerEdgent.deployTopology(this.topology);
            
        }catch(Exception e){
            UtilDebug.printDebugConsole("Error init StreamController: " + e.getMessage());
            UtilDebug.printError(e);
        }
    }
    
    public void disconnect(){
       System.out.println("Disconnect StreamControllerImpl FoT-StreamGateway");
       
       
       //Thread.currentThread().getThreadGroup().interrupt();
       //Thread.currentThread().getThreadGroup().destroy();
       //Thread.currentThread().destroy();
    }
    
    public void loadFoTDeviceStream(){
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(this.jsonDevices);
        JsonArray jarray = element.getAsJsonArray();
        //Gson gson = new Gson();
        //JsonElement tree  = gson.toJsonTree(this.jsonDevices);
        //UtilDebug.printDebugConsole(String.valueOf(tree.getAsJsonPrimitive().getAsJsonArray()), this.debugModeValue);
        //UtilDebug.printDebugConsole(tree.getAsJsonObject().toString(), this.debugModeValue);
        //JsonArray jarray = tree.getAsJsonArray();
        UtilDebug.printDebugConsole("Tamanho do array: " + jarray.size());
        
        for (JsonElement jsonElement : jarray) {
            if(jsonElement.isJsonObject()){
                
                System.out.println("Loop 1");
                FoTDeviceStream fotDeviceStream = new FoTDeviceStream(this.topology, this.mqttConfig, bootstrapServers, this.getGatewayID());
                JsonObject fotElement = jsonElement.getAsJsonObject();
                fotDeviceStream.setDeviceId(fotElement.get("id").getAsString());
                fotDeviceStream.setLatitude(fotElement.get("latitude").getAsFloat());
                fotDeviceStream.setLongitude(fotElement.get("latitude").getAsFloat());
                
                UtilDebug.printDebugConsole(fotDeviceStream.getDeviceId());
                
                
                JsonArray jsonArraySensors = fotElement.getAsJsonArray("sensors");
                List<FoTSensorStream> listFoTSensorStream = new ArrayList<FoTSensorStream>();
                
               
                
                for (JsonElement jsonElementSensor : jsonArraySensors) {
                    if(jsonElementSensor.isJsonObject()){
                        JsonObject fotSensor = jsonElementSensor.getAsJsonObject();
                        String sensorID = fotSensor.get("id").getAsString();
                        
                        FoTSensorStream fotSensorStream = new FoTSensorStream(this.topology, this.mqttConfig, 
                                sensorID, fotDeviceStream, this.pathLog);
                        
                        fotSensorStream.setType(fotSensor.get("type").getAsString());
                        fotSensorStream.setCollectionTime(fotSensor.get("collection_time").getAsInt());
                        fotSensorStream.setPublishingTime(fotSensor.get("publishing_time").getAsInt());
                        
                        //fotSensorStream.sendTatuFlow();
                        
                        System.out.println("Loop 2");
                        
                        UtilDebug.printDebugConsole(fotSensorStream.getSensorid());
                        UtilDebug.printDebugConsole(String.valueOf(fotSensorStream.getCollectionTime()));
                        UtilDebug.printDebugConsole(String.valueOf(fotSensorStream.getPublishingTime()));
                                
                        listFoTSensorStream.add(fotSensorStream);
                    }   
                }
                
                   
               fotDeviceStream.setListFoTSensorStream(listFoTSensorStream);
               this.listFoTDeviceStream.add(fotDeviceStream);
            }
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public MqttConfig getMqttConfig() {
        return mqttConfig;
    }

    public void setMqttConfig(MqttConfig mqttConfig) {
        this.mqttConfig = mqttConfig;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDebugModeValue() {
        return debugModeValue;
    }

    public void setDebugModeValue(boolean debugModeValue) {
        this.debugModeValue = debugModeValue;
    }

    public String getJsonDevices() {
        return jsonDevices;
    }

    public void setJsonDevices(String jsonDevices) {
        this.jsonDevices = jsonDevices;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    public List<FoTDeviceStream> getListFoTDeviceStream() {
        return listFoTDeviceStream;
    }

    public void setListFoTDeviceStream(List<FoTDeviceStream> listFoTDeviceStream) {
        this.listFoTDeviceStream = listFoTDeviceStream;
    }

    public int getDefaultCollectionTime() {
        return defaultCollectionTime;
    }

    public void setDefaultCollectionTime(int defaultCollectionTime) {
        this.defaultCollectionTime = defaultCollectionTime;
    }

    public int getDefaultPublishingTime() {
        return defaultPublishingTime;
    }

    public void setDefaultPublishingTime(int defaultPublishingTime) {
        this.defaultPublishingTime = defaultPublishingTime;
    }

    /**
     * @return the pathLog
     */
    public String getPathLog() {
        return pathLog.toString();
    }

    /**
     * @param pathLog the pathLog to set
     */
    public void setPathLog(String pathLog) {
        this.pathLog = Paths.get(pathLog);
    }
   
    /**
     * @return the gatewayID
     */
    public String getGatewayID() {
        return gatewayID;
    }

    /**
     * @param gatewayID the gatewayID to set
     */
    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }
    
}
