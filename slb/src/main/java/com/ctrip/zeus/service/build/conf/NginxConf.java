package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.model.Rule;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.util.ResolverUtils;
import com.ctrip.zeus.service.lua.LuaService;
import com.ctrip.zeus.service.model.common.RulePhase;
import com.ctrip.zeus.service.model.common.RuleSet;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.rule.RuleManager;
import com.ctrip.zeus.service.rule.model.RuleDataContext;
import com.ctrip.zeus.service.rule.model.RuleStages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ctrip.zeus.service.build.util.RuleEnableKeys.SERVER_NAME_HASH_RULE_ENABLED_KEY;

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
    @Resource
    UpstreamsConf upstreamsConf;
    @Resource
    LuaService luaService;
    @Resource
    private RuleManager ruleManager;

    private Map<String, RuleGenerate> ruleGenerateRegistry = new HashMap<>();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public NginxConf() {
        registerHttpRules();
    }

    private void registerHttpRules() {
        try {
            ruleGenerateRegistry.put("init_randomseed", new RuleGenerate() {
                @Override
                public String generateCommandValue(Rule rule) throws Exception {
                    if (RulePhase.HTTP_INIT_BY_LUA.equals(RulePhase.getRulePhase(rule.getPhaseId()))) {
                        return "\nmath.randomseed(os.time())\n";
                    } else {
                        throw new Exception("Invalid rule phase " + rule.getPhase() + ".");
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    public String generate(Slb slb, List<Rule> defaultRules, ModelSnapshotEntity snapshot) throws Exception {
        Long slbId = slb.getId();

        RuleSet<Slb> generationRules = new RuleSet<>(slb);
        for (Rule rule : slb.getRuleSet()) {
            generationRules.addRule(rule);
        }

        ConfWriter confWriter = new ConfWriter(10240, true);
        confWriter.writeCommand("worker_processes", "auto");
        confWriter.writeCommand("user", "nobody");
        String logLevel = configHandler.getStringValue("logLevel", slbId, null, null, "");
        confWriter.writeCommand("error_log", "/opt/logs/nginx/error.log" + (logLevel.isEmpty() ? logLevel : " " + logLevel));
        confWriter.writeCommand("worker_rlimit_nofile", "65535");
        confWriter.writeCommand("pid", "logs/nginx.pid");

        confWriter.writeEventsStart();
        confWriter.writeCommand("worker_connections", "30720");
        confWriter.writeCommand("multi_accept", "on");
        confWriter.writeCommand("use", "epoll");
        confWriter.writeEventsEnd();

        confWriter.writeHttpStart();
        confWriter.writeCommand("include", "mime.types");

        if (configHandler.getEnable("auto.resolver", slbId, null, null, true)) {
            confWriter.writeLine(ResolverUtils.RESOLVER_SPACE);
        }
        confWriter.writeLine("lua_shared_dict ipList " + configHandler.getStringValue("ipList.shared.mem.size", "100M") + ";");

        if (configHandler.getEnable("new.lua.package", slbId, null, null, true)) {
            confWriter.writeCommand("include", configHandler.getStringValue("lua.root.path", "/opt/app/nginx/conf/lua") + "/lua.conf");
            boolean wafInitEnabled = configHandler.getEnable("waf", slbId, null, null, false);
            buildLuaInitConfig(confWriter, slb, wafInitEnabled, generationRules, defaultRules);
        }

        confWriter.writeCommand("default_type", "application/octet-stream");
        if (configHandler.getStringValue("keepAlive.timeout", slbId, null, null, null) != null) {
            confWriter.writeCommand("keepalive_timeout", configHandler.getStringValue("keepAlive.timeout", slbId, null, null, "65"));
        }
        String logFormat = LogFormat.getMain();
        if (configHandler.getEnable("unified_client_addr", slbId, null, null, false)) {
            logFormat = logFormat.replace(LogFormat.REMOTE_ADDR, LogFormat.SLB_CLIENT_ADDR);
        } else if (configHandler.getEnable("proxy_protocol", slbId, null, null, true)) {
            logFormat = logFormat.replace(LogFormat.REMOTE_ADDR, "$proxy_protocol_addr");
        }
        confWriter.writeCommand("log_format", "main " + logFormat);
        confWriter.writeCommand("access_log", "/opt/logs/nginx/access.log main");
        if (!configHandler.getEnable(SERVER_NAME_HASH_RULE_ENABLED_KEY, slbId, null, null, true)) {
            confWriter.writeCommand("server_names_hash_max_size", configHandler.getStringValue("serverNames.maxSize", slbId, null, null, "10000"));
            confWriter.writeCommand("server_names_hash_bucket_size", configHandler.getStringValue("serverNames.bucketSize", slbId, null, null, "128"));
        }
        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, slb.getRuleSet());
        ruleManager.write(ruleDataContext, RuleStages.STAGE_NGINX_CONF, confWriter);
        confWriter.writeCommand("client_max_body_size", "2m");
        confWriter.writeCommand("ignore_invalid_headers", "off");
        if (configHandler.getEnable("http.large.client.header.buffers", slbId, null, null, true)) {
            confWriter.writeCommand("large_client_header_buffers", configHandler.getStringValue("http.large.client.header.buffers", slbId, null, null, "4 16k"));
        }

        if (configHandler.getEnable("default.server.http.version.2", slbId, null, null, false)
                || configHandler.getEnable("http.version.2", slbId, null, null, false)) {
            //nothing
            logger.info("http2.0 is disable");
        } else if (configHandler.getEnable("proxy.request.buffering.nginx.conf.off", slbId, null, null, false)) {
            confWriter.writeCommand("proxy_request_buffering", "off");
        }

        serverConf.writeDyupsServer(confWriter, slbId);
        serverConf.writeBlackListServer(confWriter, slbId);
        serverConf.writeDefaultServers(confWriter, slb, defaultRules);
        serverConf.writeWafDefaultServer(confWriter, slbId);

        confWriter.writeCommand("include", "upstreams/*.conf");
        confWriter.writeCommand("include", "vhosts/*.conf");
        upstreamsConf.writeDefaultUpstreams(confWriter, slbId, snapshot);
        confWriter.writeHttpEnd();
        return confWriter.getValue();
    }

    protected String generateLuaInitScripts(Long slbId, boolean wafEnabled, List<Rule> rules) throws Exception {
        StringBuilder initLuaScripts = new StringBuilder();

        for (Rule rule : rules) {
            RuleGenerate gen = ruleGenerateRegistry.get(rule.getName());
            if (gen != null) {
                initLuaScripts.append("  " + gen.generateCommandValue(rule));
            }
        }
        if (configHandler.getEnable("resty.random.lua", slbId, null, null, false) && luaService.isLuaModuleExist("resty.random")) {
            initLuaScripts.append("\nrandom = require \"resty.random\"");
        }
        if (configHandler.getEnable("resty.string.lua", slbId, null, null, false) && luaService.isLuaModuleExist("resty.string")) {
            initLuaScripts.append("\nstr = require \"resty.string\"");
        }
        if (configHandler.getEnable("mmh2.lua", slbId, null, null, false) && luaService.isLuaModuleExist("murmurhash2")) {
            initLuaScripts.append("\nmmh2 = require \"murmurhash2\"");
        }
        initLuaScripts.append("\n");
        return initLuaScripts.toString();
    }

    private void buildLuaInitConfig(ConfWriter confWriter, Slb slb, boolean wafInitEnabled, RuleSet<Slb> generationRules, List<Rule> defaultRules) throws Exception {
        List<Rule> luaInitRules = generationRules.getRulesByPhase(RulePhase.HTTP_INIT_BY_LUA);

        confWriter.startCommand("init_by_lua");

        //Write GetTimeOfDay Function
        LuaConf.writeGetTimeFunction(confWriter);

        //Write Init By Lua For Ip Black List Server.
        confWriter.writeLine("        function explodeToTable(delimeter, str)\n" +
                "    local res = {}\n" +
                "        if delimeter == nil or str == nil then\n" +
                "            return nil\n" +
                "        end\n" +
                "    local start, start_pos, end_pos = 1, 1, 1\n" +
                "    while true do\n" +
                "        start_pos, end_pos = string.find(str, delimeter, start, true)\n" +
                "        if not start_pos then\n" +
                "            break\n" +
                "        end\n" +
                "        res[string.sub(str, start, start_pos - 1)] = true\n" +
                "        start = end_pos + 1\n" +
                "    end\n" +
                "    res[string.sub(str,start)] = true\n" +
                "    return res\n" +
                "end\n" +
                "\n" +
                "function explodeToArray(delimeter, str)\n" +
                "    local res = {}\n" +
                "        if delimeter == nil or str == nil then\n" +
                "            return nil\n" +
                "        end\n" +
                "    local start, start_pos, end_pos = 1, 1, 1\n" +
                "        local index = 1\n" +
                "    while true do\n" +
                "        start_pos, end_pos = string.find(str, delimeter, start, true)\n" +
                "        if not start_pos then\n" +
                "            break\n" +
                "        end\n" +
                "        res[index] = string.sub(str, start, start_pos - 1)\n" +
                "                index = index + 1\n" +
                "        start = end_pos + 1\n" +
                "    end\n" +
                "    res[index] = string.sub(str,start)\n" +
                "    return res\n" +
                "end\n");

        LuaConf.writeCookieLenFunction(confWriter);
        LuaConf.writeTableLenFunction(confWriter);

        String initLua = generateLuaInitScripts(slb.getId(), wafInitEnabled, luaInitRules);
        if (initLua != null && !initLua.isEmpty()) {
            confWriter.startLuaZone();
            confWriter.writeLine(initLua);
            confWriter.endLuaZone();
        }
        // Init By Lua
        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, slb.getRuleSet());
        ruleManager.write(ruleDataContext, RuleStages.STAGE_HTTP_INIT_BY_LUA, confWriter);
        confWriter.endCommand();
    }

}