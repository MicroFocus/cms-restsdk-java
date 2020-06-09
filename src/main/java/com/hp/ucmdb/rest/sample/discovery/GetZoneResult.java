package com.hp.ucmdb.rest.sample.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.ucmdb.rest.sample.utils.RestApiConnectionUtils;

import java.io.IOException;
import java.net.URLEncoder;

public class GetZoneResult {
    private String rootURL;

    public GetZoneResult(String rootURL) {
        this.rootURL = rootURL;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Parameters: hostname username password zonename");
            System.exit(0);
        }

        String hostname = args[0];
        String username = args[1];
        String password = args[2];
        String zonename = args[3];
        String port = "8443";

        // authenticate
        String token = null;
        try {
            token = RestApiConnectionUtils.loginServer(hostname, username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (token == null || token.length() == 0) {
            System.out.println("Can not log in to the UCMDB server. Check your serverIp, userName or password!");
            System.exit(0);
            ;
        }
        System.out.println(token);

        // start the task
        GetZoneResult task = new GetZoneResult("https://" + hostname + ":" + port + "/rest-api/");
        task.execute(token, zonename);
    }

    private void execute(String token, String zonename) {
        String response = "";
        // check if new UI backend enabled
        try {
            response = RestApiConnectionUtils.doGet(rootURL + "infrasetting?name=appilog.collectors.enableZoneBasedDiscovery", token );
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            if(!"true".equals(node.get("value").asText())){
                System.out.println("New Discovery backend is not enabled.");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }


        // run the zone
        try {
            RestApiConnectionUtils.doPatch(rootURL + "discovery/managementzones/" + zonename + "?operation=activate", token, null );
        } catch (IOException e) {
            e.printStackTrace();
        }


        // check zone status until finish
        boolean finished = false;
        while(!finished){
            int total = 1;
            int count = 0;
            int start = 0;
            int bulkSize = 500;
            try {
                finished = true;
                for(; total > count + start;){
                    String c = rootURL + "discovery/triggers?start=" + start + "&count=" + bulkSize
                            + "&sortField=ciLabel&orderByAsc=true&filter=mzoneIds"+ URLEncoder.encode("=[" + zonename + "]","UTF-8");
                    response = RestApiConnectionUtils.doGet( c, token );
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode triggers = objectMapper.readTree(response);
                    total = triggers.get("total").asInt();
                    count = triggers.get("count").asInt();
                    start = triggers.get("start").asInt();
                    for(JsonNode item : triggers.get("items")){
                        if(!("SUCCESS".equals(item.get("status").asText()) || "ERROR".equals(item.get("status").asText()) || "WARNING".equals(item.get("status").asText()))){
                            finished = false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // get zone result and print
        try {
            response = RestApiConnectionUtils.doGet(rootURL + "discovery/results?zoneId=" + zonename + "&ciType=ip_address&start=0&count=100", token );
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            int total = node.get("total").asInt();
            int count = node.get("count").asInt();
            if(total > count){
                System.out.println("Total CI number: " + total + ". Print the top " + count);
            }
            for(int i = 0 ; i < node.get("resultColumnDataList").size(); i++){
                StringBuilder output = new StringBuilder();
                JsonNode item = node.get("resultColumnDataList").get(i);
                output.append("Item " + i + 1 + ":");
                for(JsonNode attr : item.get("apiDiscoveryResultDataList")){
                    output.append(attr.get("attributeName").asText() + "=" + attr.get("attributeValue").asText() + ",");
                }
                output.deleteCharAt(output.length() - 1);
                System.out.println(output.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }
}
