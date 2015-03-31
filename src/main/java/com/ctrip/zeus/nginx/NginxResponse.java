package com.ctrip.zeus.nginx;

import com.google.gson.Gson;

/**
 * User: mag
 * Date: 3/24/2015
 * Time: 1:39 PM
 */
public class NginxResponse {
    private boolean succeed = true;
    private String serverIP;
    private String outMessage;
    private String errMessage;

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public String getOutMessage() {
        return outMessage;
    }

    public void setOutMessage(String outMessage) {
        this.outMessage = outMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static NginxResponse fromJson(String jsonStr){
        Gson gson = new Gson();
        return gson.fromJson(jsonStr,NginxResponse.class);
    }
}
