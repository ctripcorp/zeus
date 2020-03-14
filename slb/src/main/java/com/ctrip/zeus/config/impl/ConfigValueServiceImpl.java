package com.ctrip.zeus.config.impl;

import com.ctrip.zeus.config.ConfigValueService;
import com.google.common.base.Strings;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

@Service("configValueServiceImpl")
public class ConfigValueServiceImpl implements ConfigValueService {
    @Override
    public String getAppDefaultOwnerMail() {
        return DynamicPropertyFactory.getInstance().getStringProperty("default.app.mail", "UNKNOWN").get();
    }

    @Override
    public String getSlbPortalUrl() {
        return DynamicPropertyFactory.getInstance().getStringProperty("slb.portal.page", "http://localhost:8099").get();
    }

    @Override
    public String getTeamMail() {
        return DynamicPropertyFactory.getInstance().getStringProperty("slb.team.mail", "UNKOWN").get();
    }

    @Override
    public String getAgentApi() {
        String result;
        String host = System.getProperty("agent.api.host");
        if (Strings.isNullOrEmpty(host)) {
            result = "http://127.0.0.1:8099/";
        } else {
            host = host.trim();
            result = host.endsWith("/") ? host : host + "/";
        }
        return result;
    }
}
