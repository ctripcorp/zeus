package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.HealthCheck;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class HealthCheckConf {
    public static String generate(Slb slb, VirtualServer vs, App app) throws Exception {
        HealthCheck h = app.getHealthCheck();

        AssertUtils.isNull(h,"App HealthCheck config is null!");
        AssertUtils.isNull(h.getIntervals(),"App HealthCheck Intervals config is null!");
        AssertUtils.isNull(h.getFails(),"App HealthCheck Fails config is null!");
        AssertUtils.isNull(h.getPasses(),"App HealthCheck Passes config is null!");

        StringBuilder b = new StringBuilder(128);
        b.append("check interval=").append(h.getIntervals())
                .append(" rise=").append(h.getPasses())
                .append(" fall=").append(h.getFails())
                .append(" timeout=").append(1000)
                .append(" type=http").append(";\n")
                .append(" check_keepalive_requests 100").append(";\n")
                .append(" check_http_send \"")
                .append("GET ").append(h.getUri()).append(" HTTP/1.0\r")
                .append(" Connection: keep-alive\r")
                .append(" Host: ").append(vs.getDomains().get(0).getName()).append("\r\"").append(";\n")
                .append("check_http_expect_alive http_2xx http_3xx").append(";\n");
        return b.toString();
    }
}
