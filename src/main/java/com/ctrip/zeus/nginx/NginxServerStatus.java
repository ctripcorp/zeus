package com.ctrip.zeus.nginx;

import com.google.gson.Gson;

/**
 * User: mag
 * Date: 3/24/2015
 * Time: 1:32 PM
 */
public class NginxServerStatus {
    private static enum Status{STARTED,STOPPED}

    private String serverIP;
    private Status status;
    private int activeConnections;


    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static NginxServerStatus fromJson(String jsonStr){
        Gson gson = new Gson();
        return gson.fromJson(jsonStr,NginxServerStatus.class);
    }
}
