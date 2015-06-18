package com.ctrip.zeus.service.build.conf;


import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.HealthCheck;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class HealthCheckConf {

    private static DynamicBooleanProperty disableHealthCheck = DynamicPropertyFactory.getInstance().getBooleanProperty("build.disable.healthCheck", false);


    public static String generate(Slb slb, VirtualServer vs, Group group) throws Exception {
        if (disableHealthCheck.get())
        {
            return "";
        }
        HealthCheck h = group.getHealthCheck();
        if (h == null)
        {
            return "";
        }
        AssertUtils.assertNotNull(h.getIntervals(), "Group HealthCheck Intervals config is null!");
        AssertUtils.assertNotNull(h.getFails(), "Group HealthCheck Fails config is null!");
        AssertUtils.assertNotNull(h.getPasses(), "Group HealthCheck Passes config is null!");


        StringBuilder b = new StringBuilder(128);

        if (group.getSsl())
        {
            b.append("check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(1000)
                    .append(" type=ssl_hello").append(";\n");
        }else {
            b.append("check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(1000)
                    .append(" type=http").append(";\n")
                    .append("check_keepalive_requests 100").append(";\n")
                    .append("check_http_send \"")
                    .append("GET ").append(h.getUri()).append(" HTTP/1.0\\r\\n")
                    .append("Connection:keep-alive\\r\\n")
                    .append("Host:").append(vs.getDomains().get(0).getName().trim()).append("\\r\\n")
                    .append("UserAgent:SLB_HealthCheck").append("\\r\\n\\r\\n\"").append(";\n")
                    .append("check_http_expect_alive http_2xx http_3xx").append(";\n");
        }

        return b.toString();
    }
}
