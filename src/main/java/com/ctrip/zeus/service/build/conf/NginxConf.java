package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Rule;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.model.common.RulePhase;
import com.ctrip.zeus.service.model.common.RuleSet;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("nginxConf")
public class NginxConf {

    @Resource
    ConfigHandler configHandler;
    @Resource
    ServerConf serverConf;

    private static String ShmZoneName = "proxy_zone";
    private Map<String, RuleGenerate> ruleGenerateRegistry = new HashMap<>();

    public NginxConf() {
        registerHttpRules();
    }

    private void registerHttpRules() {
        try {
            ruleGenerateRegistry.put("init_randomseed", new RuleGenerate() {
                @Override
                public String generateCommandValue(Rule rule) throws Exception {
                    if (RulePhase.HTTP_INIT_BY_LUA.equals(RulePhase.getRulePhase(rule.getPhaseId()))) {
                        return "math.randomseed(os.time())";
                    } else {
                        throw new Exception("Invalid rule phase " + rule.getPhase() + ".");
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    public String generate(Slb slb) throws Exception {
        Long slbId = slb.getId();

        RuleSet<Slb> generationRules = new RuleSet<>(slb);
        for (Rule rule : slb.getRuleSet()) {
            generationRules.addRule(rule);
        }

        ConfWriter confWriter = new ConfWriter(10240, true);
        confWriter.writeCommand("worker_processes", "auto");
        confWriter.writeCommand("user", "nobody");
        confWriter.writeCommand("error_log", "/opt/logs/nginx/error.log" + configHandler.getStringValue("logLevel", slbId, null, null, ""));
        confWriter.writeCommand("worker_rlimit_nofile", "65535");
        confWriter.writeCommand("pid", "logs/nginx.pid");

        confWriter.writeEventsStart();
        confWriter.writeCommand("worker_connections", "30720");
        confWriter.writeCommand("multi_accept", "on");
        confWriter.writeCommand("use", "epoll");
        confWriter.writeEventsEnd();

        confWriter.writeHttpStart();
        confWriter.writeCommand("include", "mime.types");

        boolean luaInitEnabled = true;
        boolean wafInitEnabled = configHandler.getEnable("waf", slbId, null, null, false);
        if (wafInitEnabled) {
            confWriter.writeCommand("include", configHandler.getStringValue("waf.include.conf", slbId, null, null, "/opt/app/nginx/conf/waf/waf.conf"));
            luaInitEnabled = configHandler.getEnable("waf.canary", slbId, null, null, false);
        }
        List<Rule> luaInitRules = generationRules.getRulesByPhase(RulePhase.HTTP_INIT_BY_LUA);
        if (luaInitRules.size() > 0 && !luaInitEnabled) {
            throw new Exception("Fail to generate nginx.conf with init_by_lua directive disabled.");
        }
        if (luaInitRules.size() > 0 || (wafInitEnabled && luaInitEnabled)) {
            confWriter.writeCommand("init_by_lua", generateLuaInitScripts(wafInitEnabled, luaInitRules));
        }

        confWriter.writeCommand("default_type", "application/octet-stream");
        confWriter.writeCommand("keepalive_timeout", configHandler.getStringValue("keepAlive.timeout", slbId, null, null, "65"));
        confWriter.writeCommand("log_format", "main " + LogFormat.getMain());
        confWriter.writeCommand("access_log", "/opt/logs/nginx/access.log main");
        confWriter.writeCommand("server_names_hash_max_size", configHandler.getStringValue("serverNames.maxSize", slbId, null, null, "10000"));
        confWriter.writeCommand("server_names_hash_bucket_size", configHandler.getStringValue("serverNames.bucketSize", slbId, null, null, "128"));
        confWriter.writeCommand("check_shm_size", configHandler.getStringValue("checkShmSize", slbId, null, null, "32") + "M");
        confWriter.writeCommand("client_max_body_size", "2m");
        confWriter.writeCommand("ignore_invalid_headers", "off");
        if (configHandler.getEnable("default.server.http.version.2", slbId, null, null, false)
                || configHandler.getEnable("http.version.2", slbId, null, null, false)) {
            //nothing
        } else if (configHandler.getEnable("proxy.request.buffering.nginx.conf.off", slbId, null, null, false)) {
            confWriter.writeCommand("proxy_request_buffering", "off");
        }

        confWriter.writeCommand("req_status_zone", ShmZoneName + " \"$hostname/$proxy_host\" 20M");

        serverConf.writeCheckStatusServer(confWriter, ShmZoneName, slbId);
        serverConf.writeDyupsServer(confWriter, slbId);
        serverConf.writeDefaultServers(confWriter, slbId);

        confWriter.writeCommand("include", "upstreams/*.conf");
        confWriter.writeCommand("include", "vhosts/*.conf");
        confWriter.writeHttpEnd();

        return confWriter.getValue();
    }


    protected String generateLuaInitScripts(boolean wafEnabled, List<Rule> rules) throws Exception {
        StringBuilder initLuaScripts = new StringBuilder();
        initLuaScripts.append("'\n");
        if (wafEnabled) {
            initLuaScripts.append("  local initwaf = require \"core.init\"\n").append("  initwaf()\n");
        }
        for (Rule rule : rules) {
            RuleGenerate gen = ruleGenerateRegistry.get(rule.getName());
            if (gen != null) {
                initLuaScripts.append("  " + gen.generateCommandValue(rule));
            }
        }
        initLuaScripts.append("\n'");
        return initLuaScripts.toString();
    }
}