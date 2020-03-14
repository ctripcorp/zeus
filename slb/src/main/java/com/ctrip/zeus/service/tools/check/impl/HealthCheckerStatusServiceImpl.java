package com.ctrip.zeus.service.tools.check.impl;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.service.tools.check.HealthCheckerStatusService;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Service;

@Service("healthCheckerStatusServiceImpl")
public class HealthCheckerStatusServiceImpl implements HealthCheckerStatusService {
    @Override
    public String getGroupStatus(String ip, String groupId) {
        return RestClient.getGroupStatusFromChecker(ip, groupId);
    }

    @Override
    public String getCheckerIP(String name) {
        String ip = null;
        try {
            String result = RestClient.getConfig(name);
            int start = result.indexOf("health-checker.members=");
            if (start != -1) {
                int end = result.indexOf("\r\n", start);
                if (end != -1) {
                    String members = result.substring(start + 23, end);
                    ip = members.split(",")[0];
                }
            }
        } catch (Exception e) {
            //ignore
        }
        if (ip == null) {
            ip = DynamicPropertyFactory.getInstance().getStringProperty("slb.health.checker.ip." + name, null).get();
        }
        return ip;
    }

    private static class RestClient extends AbstractRestClient {
        private static DynamicStringProperty configBaseUrl = DynamicPropertyFactory.getInstance().getStringProperty("slb.config.base.url", "http://localhost");
        private static RestClient CONFIG_CLIENT = new RestClient(configBaseUrl.get());

        private RestClient(String url) {
            super(url);
        }

        static String getConfig(String name) {
            return CONFIG_CLIENT.getTarget().path("/api/HealthChecker/" + name).request().get(String.class);
        }

        static String getGroupStatusFromChecker(String ip, String groupId) {
            return new RestClient("http://" + ip + ":8080").getTarget().path("/slb").queryParam("groupId", groupId).request().get(String.class);
        }
    }
}
