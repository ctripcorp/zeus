package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.nginx.SlbConfResponse;

/**
 * @Discription
 **/
public class LocalSlbConfResponse {
    private String ip;
    private SlbConfResponse slbConfResponse;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public SlbConfResponse getSlbConfResponse() {
        return slbConfResponse;
    }

    public void setSlbConfResponse(SlbConfResponse slbConfResponse) {
        this.slbConfResponse = slbConfResponse;
    }
}
