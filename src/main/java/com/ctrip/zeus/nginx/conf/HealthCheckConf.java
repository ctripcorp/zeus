package com.ctrip.zeus.nginx.conf;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.HealthCheck;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class HealthCheckConf {
    public static String generate(Slb slb, VirtualServer vs, App app) {
        HealthCheck h = app.getHealthCheck();
        return "health_check " +
                " match=server_ok " +
                " uri=" + h.getUri() +
                " interval=" + h.getIntervals() +
                " fails=" + h.getFails() +
                " passes=" + h.getPasses();
    }
}
