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

        StringBuilder b = new StringBuilder(128);
        b.append("    ").append("check interval=").append(h.getIntervals())
                .append(" rise").append(h.getPasses())
                .append(" fall").append(h.getFails())
                .append(" timeout=").append(1000)
                .append(" type=http").append(";\n");
        b.append("    ").append("check_keepalive_requests 100").append(";\n");
        b.append("    ").append("check_http_send \"GET ").append(h.getUri()).append(" HTTP/1.1\\r\\nConnection: keep-alive\\r\\n\\r\\n\"").append(";\n");
        b.append("    ").append("check_http_expect_alive http_2xx http_3xx").append(";\n");
        return b.toString();
    }
}
