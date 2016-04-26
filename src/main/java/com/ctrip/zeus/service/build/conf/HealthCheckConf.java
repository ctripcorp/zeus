package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.HealthCheck;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.util.AssertUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("healthCheckConf")
public class HealthCheckConf {

    @Resource
    ConfService confService;

    public String generate(VirtualServer vs, Group group) throws Exception {
        Long vsId = vs.getId();
        Long groupId = group.getId();

        if (!confService.getEnable("upstream.healthCheck", null, vsId, groupId, true)) {
            return "";
        }

        HealthCheck h = group.getHealthCheck();
        if (h == null) {
            return "";
        }
        AssertUtils.assertNotNull(h.getIntervals(), "Group HealthCheck Intervals config is null!");
        AssertUtils.assertNotNull(h.getFails(), "Group HealthCheck Fails config is null!");
        AssertUtils.assertNotNull(h.getPasses(), "Group HealthCheck Passes config is null!");
        AssertUtils.assertNotNull(h.getUri(), "Group HealthCheck Uri config is null!");

        StringBuilder b = new StringBuilder(128);

        String healthCheckTimeout = confService.getStringValue("upstream.healthCheck.timeOut", null, vsId, groupId, "2000");
        String sslAspx = confService.getStringValue("upstream.healthCheck.ssl.aspx", null, vsId, groupId, "/SlbHealthCheck.aspx");

        if (group.getSsl() && confService.getEnable("upstream.healthCheck.sslHello", null, vsId, groupId, false)) {
            b.append("    check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(h.getTimeout() == null ? healthCheckTimeout : h.getTimeout())
                    .append(" type=ssl_hello").append(";\n");
        } else if (group.getSsl() && h.getUri().equalsIgnoreCase(sslAspx)) {
            b.append("    check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(h.getTimeout() == null ? healthCheckTimeout : h.getTimeout());
            b.append(" port=").append(80);
            b.append(" type=http default_down=false").append(";\n")
                    .append("    check_keepalive_requests 100").append(";\n")
                    .append("    check_http_send \"")
                    .append("GET ").append(h.getUri()).append(" HTTP/1.0\\r\\n")
                    .append("Connection:keep-alive\\r\\n");
            b.append("UserAgent:SLB_HealthCheck").append("\\r\\n\\r\\n\"").append(";\n")
                    .append("    check_http_expect_alive http_2xx http_3xx").append(";\n");
        } else if (group.getSsl()) {
            b.append("    check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(h.getTimeout() == null ? healthCheckTimeout : h.getTimeout());
            b.append(" port=").append(80);
            b.append(" type=http default_down=false").append(";\n")
                    .append("    check_keepalive_requests 100").append(";\n")
                    .append("    check_http_send \"")
                    .append("GET ").append(h.getUri()).append(" HTTP/1.1\\r\\n")
                    .append("Connection:keep-alive\\r\\n")
                    .append("Host:").append(vs.getDomains().get(0).getName().trim()).append("\\r\\n");
            b.append("UserAgent:SLB_HealthCheck").append("\\r\\n\\r\\n\"").append(";\n")
                    .append("    check_http_expect_alive http_2xx http_3xx").append(";\n");
        }else {
            b.append("    check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(h.getTimeout() == null ? healthCheckTimeout : h.getTimeout());
            b.append(" type=http default_down=false").append(";\n")
                    .append("    check_keepalive_requests 100").append(";\n")
                    .append("    check_http_send \"")
                    .append("GET ").append(h.getUri()).append(" HTTP/1.1\\r\\n")
                    .append("Connection:keep-alive\\r\\n")
                    .append("Host:").append(vs.getDomains().get(0).getName().trim()).append("\\r\\n");
            b.append("UserAgent:SLB_HealthCheck").append("\\r\\n\\r\\n\"").append(";\n")
                    .append("    check_http_expect_alive http_2xx http_3xx").append(";\n");
        }
        return b.toString();
    }
}
