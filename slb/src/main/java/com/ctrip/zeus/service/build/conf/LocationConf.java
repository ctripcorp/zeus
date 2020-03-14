package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.domain.GroupType;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.rule.RuleManager;
import com.ctrip.zeus.service.rule.model.RuleDataContext;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.service.verify.verifier.PropertyValueUtils;
import com.ctrip.zeus.tag.ItemTypes;
import com.ctrip.zeus.tag.PropertyService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.ctrip.zeus.service.build.util.RuleEnableKeys.*;

;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("locationConf")
public class LocationConf {
    @Resource
    ConfigHandler configHandler;
    @Resource
    private
    PropertyService propertyService;
    @Resource
    private
    LocalInfoService localInfoService;
    @Resource
    private RuleManager ruleManager;
    @Autowired
    private AppService appService;

    private static final Logger logger = LoggerFactory.getLogger(LocationConf.class);


    private static final String clientMaxBodySizeEnableKey = "location.client.max.body.size";
    private static final String clientBodyBufferSizeKey = "location.client.body.buffer.size";

    private static final String proxyReadTimeoutEnableKey = "location.proxy.readTimeout";

    // config key for enable/disable rewrite by lua
    private static final String accessByLuaEnableKey = "access_by_lua";

    private final String errLuaScripts = new StringBuilder(512).append("'\n")
            .append("local domain = \"domain=\"..ngx.var.host;\n")
            .append("local requestUri = ngx.var.request_uri;\n")
            .append("if requestUri == nil then \n")
            .append("   requestUri = \"/\";\nend\n")
            .append("local uri = \"&uri=\"..string.gsub(requestUri, \"?.*\", \"\");\n")
            .append("ngx.req.set_uri_args(domain..uri);'").toString();
    public static final String TRUE_CLIENT_IP_VAR = "client_ip";
    private final String setHeaderLuaScripts = new StringBuilder(500)
            .append("  local headers = ngx.req.get_headers();\n")
            .append("  if ngx.var.inWhite ~= \"true\" or headers[\"X-Forwarded-For\"] == nil then\n")
            .append("    if (headers[\"True-Client-Ip\"] ~= nil) then\n")
            .append("      ngx.req.set_header(\"X-Forwarded-For\", headers[\"True-Client-IP\"])\n")
            .append("      ngx.var." + TRUE_CLIENT_IP_VAR + " = headers[\"True-Client-IP\"]\n")
            .append("    else\n")
            .append("      ngx.req.set_header(\"X-Forwarded-For\", ngx.var.{{client_addr_var}} )\n")
            .append("      ngx.var." + TRUE_CLIENT_IP_VAR + " = ngx.var.{{client_addr_var}}\n")
            .append("  end\n")
            .append("end\n").toString();

    private final String headerSizeLuaScripts = "local cookie = ngx.var.http_cookie;\n" +
            "ngx.var.cookie_size = 0;\n" +
            "if cookie ~= nil then\n" +
            "         ngx.var.cookie_size = string.len( cookie);\n" +
            "end\n" +
            "local headers = ngx.req.get_headers();\n" +
            "local headersString = \"\";\n" +
            "ngx.var.header_size = 0;\n" +
            "for k,v in pairs(headers) do\n" +
            "   if type(v) ~= \"table\" then\n" +
            "       ngx.var.header_size = ngx.var.header_size + string.len(k);\n" +
            "       ngx.var.header_size = ngx.var.header_size + string.len(v);\n" +
            "       if k ~= \"cookie\" then" +
            "           headersString = headersString..k..\":\"..v..\";\";\n" +
            "       end\n" +
            "   else\n" +
            "       for i,j in pairs(v) do\n" +
            "           ngx.var.header_size = ngx.var.header_size + string.len(i);\n" +
            "           ngx.var.header_size = ngx.var.header_size + string.len(j);\n" +
            "           headersString = headersString..i..\":\"..j..\";\";\n" +
            "       end\n" +
            "   end\n" +
            "end\n";

    private final String logLargeHeadersLuaScripts = "local hs = ngx.var.header_size - ngx.var.cookie_size;\n"
            + " if ngx.var.header_size ~= \"-\" and hs > " + "%d" + " then\n" +
            "     ngx.var.header_value = string.gsub(headersString,\" \", \"\");\n" +
            "end\n";


    public String getErrLuaScripts() {
        return errLuaScripts;
    }

    public void write(ConfWriter confWriter, Slb slb, VirtualServer vs, TrafficPolicy trafficPolicy, PolicyVirtualServer policyOnVs, Map<Long, Group> controlledGroups, List<Rule> defaultRules) throws Exception {
        confWriter.writeLocationStart(policyOnVs.getPath());
        Long gid = 0L;
        if (trafficPolicy.getControls().size() > 0) {
            gid = trafficPolicy.getControls().get(0).getGroup().getId();
        }
        Group group = controlledGroups.get(gid);

        // Rules
        List<Rule> groupRules = group.getRuleSet();
        List<Rule> vsRules = vs == null ? new ArrayList<>() : vs.getRuleSet();
        List<Rule> slbRules = slb.getRuleSet();
        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, groupRules, vsRules, slbRules);
        writeRequestRestrictionConfigs(confWriter, slb, vs, group, ruleDataContext);

        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeCommand("set", LogFormat.VAR_POLICY_NAME + " " + "policy_" + trafficPolicy.getId());
        confWriter.writeCommand("content_by_lua", generateTrafficControlScript(trafficPolicy.getControls()));
        confWriter.writeLocationEnd();
    }

    public String generateTrafficControlScript(List<TrafficControl> controls) {
        if (controls.size() == 0) return "";
        if (controls.size() == 1) {
            return "'\n  ngx.exec(\"@group_" + controls.get(0).getGroup().getId() + "\")\n'";
        }
        double totalWeight = 0.0;
        TreeMap<Double, List<Long>> controlOrder = new TreeMap<>();
        for (TrafficControl c : controls) {
            List<Long> v = controlOrder.get(c.getWeight().doubleValue());
            if (v == null) {
                v = new ArrayList<>();
                controlOrder.put(c.getWeight().doubleValue(), v);
            }
            v.add(c.getGroup().getId());
            totalWeight += c.getWeight().doubleValue();
        }

        StringBuilder controlScript = new StringBuilder();
        controlScript.append("'\n")
                .append("  local r = math.random()\n");

        double prevWeight = 0.0, w;
        boolean firstBlockGenerated = false;
        Map.Entry<Double, List<Long>> curr = controlOrder.pollFirstEntry();

        while (curr != null) {
            if (curr.getKey() > 0.0) {
                for (Long v : curr.getValue()) {
                    w = curr.getKey() + prevWeight;
                    if (!firstBlockGenerated) {
                        controlScript.append("  if (");
                        boolean first = true;
                        for (TrafficControl c : controls) {
                            if (first) {
                                controlScript.append("ngx.var.cookie_slbshardingcookieflag == \"" + c.getGroup().getId() + "\" ");
                                first = false;
                            } else {
                                controlScript.append("or ngx.var.cookie_slbshardingcookieflag == \"" + c.getGroup().getId() + "\" ");
                            }
                        }
                        controlScript.append(") then\n    ngx.exec(\"@group_\"..ngx.var.cookie_slbshardingcookieflag)\n");
                        controlScript.append("  elseif (r >= " + String.format("%.2f", prevWeight / totalWeight) + " and r < " + String.format("%.2f", w / totalWeight) + ") then\n")
                                .append("    ngx.exec(\"@group_" + v + "\")\n");
                        prevWeight += curr.getKey();
                        firstBlockGenerated = true;
                    } else {
                        controlScript.append("  elseif (r >= " + String.format("%.2f", prevWeight / totalWeight) + " and r " + (w == totalWeight ? "<= " : "< ") + String.format("%.2f", w / totalWeight) + ") then\n")
                                .append("    ngx.exec(\"@group_" + v + "\")\n");
                        prevWeight = w;
                    }
                }
            }
            curr = controlOrder.pollFirstEntry();
        }

        controlScript.append("  end'");
        return controlScript.toString();
    }

    /*
        Write groups with the same path with route rules applied.
     */
    public void write(ConfWriter confWriter, List<Group> groups, List<GroupVirtualServer> groupVirtualServers, Slb slb, VirtualServer vs, List<Rule> defaultRules) throws Exception {
        confWriter.writeLocationStart(groupVirtualServers.get(0).getPath());
        // Targets
        Group lastGroup = groups.get(groups.size() - 1);

        // Rules
        List<Rule> groupRules = lastGroup.getRuleSet();
        List<Rule> vsRules = vs.getRuleSet();
        List<Rule> slbRules = slb.getRuleSet();
        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, groupRules, vsRules, slbRules);

        // Request restrictions
        writeRequestRestrictionConfigs(confWriter, slb, vs, lastGroup, ruleDataContext);

        //policy name
        confWriter.writeCommand("set", LogFormat.VAR_POLICY_NAME + " -");
        //add header size and cookie size check
        confWriter.writeCommand("set", LogFormat.HEADER_SIZE + " -");
        confWriter.writeCommand("set", LogFormat.INTERCEPT_STATUS + " -");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeCommand("set", LogFormat.COOKIE_SIZE + " -");
        confWriter.writeCommand("set", LogFormat.HEADER_VALUE + " -");
        confWriter.writeCommand("set", LogFormat.SET_COOKIE_VALUE + " -");
        confWriter.writeCommand("set", LogFormat.PAGE_ID_HEADER + " -");

        StringBuilder luaBuilder = new StringBuilder("'\n");
        //isFirst: whether first if clause has been written
        boolean isFirst = true;
        //needErrPage: whether 404 is needed
        boolean needErrPage = true;
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            GroupVirtualServer groupVirtualServer = groupVirtualServers.get(i);
            String conditionString = null;

            List<RouteRule> rules = groupVirtualServer.getRouteRules();
            if (rules != null && rules.size() != 0) {
                String[] ruleStr = new String[rules.size()];
                int index = 0;
                for (RouteRule rr : rules) {
                    ruleStr[index++] = generateRuleScripts(rr);
                }
                conditionString = Joiner.on(" or ").skipNulls().join(ruleStr);
            }

            if (StringUtils.isEmpty(conditionString)) {
                if (i + 1 == groups.size()) {
                    // A trailing group with no rule is found, handling all remaining requests.
                    luaBuilder.append("  ");
                    if (!isFirst) {
                        luaBuilder.append("else ");
                    }
                    luaBuilder.append("ngx.exec(\"@group_").append(group.getId()).append("\")\n");
                    needErrPage = false;
                    break;
                } else {
                    // A group with no rule is found in the middle, we need a "true" condition here and continue generating remaining unreachable groups.
                    conditionString = "true";
                }
            }
            if (isFirst) {
                luaBuilder.append("  if (");
                isFirst = false;
            } else {
                luaBuilder.append("  elseif (");
            }
            luaBuilder.append(conditionString).append(") then\n").append("    ngx.exec(\"@group_").append(group.getId()).append("\")\n");
        }
        if (needErrPage && isFirst) {
            confWriter.write("  return 404;\n");
        } else {
            if (needErrPage) {
                luaBuilder.append("  else ngx.status = 404\n  ngx.say(\"Not found\")\n  ngx.exit(ngx.HTTP_NOT_FOUND)\n");
            }
            if (!isFirst) {
                luaBuilder.append("  end");
            }
            luaBuilder.append("'");
            confWriter.writeCommand("content_by_lua", luaBuilder.toString());
        }
        confWriter.writeLocationEnd();
    }

    private String generateRuleScripts(RouteRule routeRule) {
        String script = null;
        switch (routeRule.getType()) {
            case "header":
                switch (routeRule.getOp()) {
                    case "regex": {
                        String headerName = routeRule.getKey1();
                        String regex = routeRule.getValue1();
                        if (StringUtils.isBlank(headerName) || StringUtils.isBlank(regex)) {
                            break;
                        }
                        boolean caseSensitive = routeRule.isFlag1();
                        StringBuilder sb = new StringBuilder();
                        String header = "ngx.var.http_" + headerName.toLowerCase().replaceAll("-", "_");


                        sb.append("(").append(header).append(" ~= nil and ")
                                .append("ngx.re.match(").append(header)
                                .append(", [=[")
                                .append(regex.replaceAll("\\\\", "\\\\\\\\"))
                                .append("]=]");
                        if (!caseSensitive) {
                            //case insensitive option
                            sb.append(", \"i\"");
                        }
                        sb.append("))");
                        script = sb.toString();
                        break;
                    }
                }
                break;
        }
        return script;
    }

    public void write(ConfWriter confWriter, Slb slb, VirtualServer vs, Group group, Dr dr, GroupVirtualServer groupOnVs, String canaryIp, Map<Long, Integer> drDesSlbs, List<Rule> defaultRules, boolean isNamedLocation, ModelSnapshotEntity snapshot, Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap) throws Exception {
        Long slbId = slb.getId();
        Long vsId = vs.getId();
        Long groupId = group.getId();
        String upstreamName = "backend_" + groupId;
        // Group context
        List<Rule> groupRules = group.getRuleSet();
        List<Rule> vsRules = vs.getRuleSet();
        List<Rule> slbRules = slb.getRuleSet();
        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, groupRules, vsRules, slbRules);
        ruleDataContext.setGroup(group);
        ruleDataContext.setSlb(slb);
        ruleDataContext.setVs(vs);
        // if group is virtual
        if (GroupType.VGROUP.toString().equalsIgnoreCase(group.getType()) || group.isVirtual()) {
            writeVirtualLocation(confWriter, upstreamName, groupOnVs, group, slb, defaultRules, isNamedLocation);
            return;
        }
        writeLocationStart(confWriter, groupOnVs, group, slb, defaultRules, isNamedLocation);
        // write request timeout configs
        writeRequestRestrictionConfigs(confWriter, slb, vs, group, ruleDataContext);
        writeAccessControlCommand(confWriter, slbId, vsId, groupId, snapshot, extendedGroupMap);
        writeStageAccessConfigs(confWriter, slb, vs, group, ruleDataContext);

        //policy & dr name
        if (!isNamedLocation) {
            confWriter.writeCommand("set", LogFormat.VAR_POLICY_NAME + " -");
        }

        //add header size and cookie size check
        confWriter.writeCommand("set", LogFormat.VAR_DR_NAME + " -");
        confWriter.writeCommand("set", LogFormat.HEADER_SIZE + " -");
        confWriter.writeCommand("set", LogFormat.INTERCEPT_STATUS + " -");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeCommand("set", LogFormat.COOKIE_SIZE + " -");
        confWriter.writeCommand("set", LogFormat.HEADER_VALUE + " -");
        confWriter.writeCommand("set", LogFormat.SET_COOKIE_VALUE + " -");
        confWriter.writeCommand("set", LogFormat.PAGE_ID_HEADER + " -");
        String sbu = "-";
        String sbuStr = getSbu(snapshot, extendedGroupMap, group);
        if (sbuStr != null) {
            Base64 base64 = new Base64(-1);
            sbu = base64.encodeToString(sbuStr.getBytes());
        }
        confWriter.writeCommand("set", LogFormat.SBU + " \"" + sbu + "\"");
        confWriter.writeCommand("set", LogFormat.APP + " \"" + group.getAppId() + "\"");
        confWriter.writeCommand("set", "$upstream_scheme \"" + (group.isSsl() ? "https" : "http") + "\"");

        //access by lua
        if (configHandler.getEnable(accessByLuaEnableKey, slbId, vsId, groupId, true)) {
            confWriter.startCommand("access_by_lua");
            String headerSizeLua = generateHeaderSizeLua(slbId, vsId, groupId);
            if (!configHandler.getEnable(LOG_LARGE_HEADER_SIZE_RULE_ENABLED_KEY, slbId, vsId, groupId, true)) {
                if (!headerSizeLua.isEmpty()) {
                    confWriter.startLuaZone();
                    confWriter.writeLine(headerSizeLua);
                    confWriter.endLuaZone();
                }
            }
            String pageIdLua = generatePageIdLua(slbId, vsId, groupId);
            if (!pageIdLua.isEmpty() && !configHandler.getEnable(PAGE_ID_RULE_ENABLED_KEY, slbId, vsId, groupId, true)) {
                confWriter.startLuaZone();
                confWriter.writeLine(pageIdLua);
                confWriter.endLuaZone();
            }
            ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_LUA_ACCESS, confWriter);

            confWriter.endCommand();
        }
        confWriter.startCommand("header_filter_by_lua");
        if (configHandler.getEnable("log.set-cookie", slbId, vsId, groupId, false)) {
            String setCookieLua = generateSetCookieCollectingLua(slbId, vsId, groupId);
            if (!setCookieLua.isEmpty()) {
                confWriter.startLuaZone();
                confWriter.writeLine(setCookieLua);
                confWriter.endLuaZone();
            }
        }
        ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_LUA_HEADER_FILTER, confWriter);
        confWriter.endCommand();

        confWriter.writeCommand("proxy_set_header", "Host " + configHandler.getStringValue("location.proxy.header.host", null, null, groupId, "$host"));
        confWriter.writeCommand("proxy_set_header", "X-Real-IP " + getClientAddrVar(slbId));
        if (!configHandler.getEnable(HEADER_X_CTRIP_SOURCE_REGION_RULE_ENABLED_KEY, slbId, vsId, groupId, true)) {
            addRegionHeader(confWriter, slbId, vsId, groupId, snapshot);
        }

        if (vs.isSsl()) {
            if (configHandler.getEnable("ssl.header", null, vsId, null, true)) {
                confWriter.writeCommand("proxy_set_header", "X-Ctrip-IsSSL 1");
            }
        }
        confWriter.writeCommand("set", " $conn_value \"\"");
        ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_ENABLE_SOCKET_IO, confWriter);
        confWriter.writeCommand("proxy_set_header", "Connection $conn_value");

        writeSlbUrlCheck(confWriter, slbId, vsId, groupId);

        writeDrSource(confWriter, slbId, vsId, groupId);
        // write rewrite_by_lua block
        StringBuilder rewriteByLua = new StringBuilder();
        // xff and canary lua
        rewriteByLua.append(writeXFFAndCanary(confWriter, slbId, vsId, groupId, group, canaryIp, snapshot, extendedGroupMap));
        //dr lua
        String drScripts = writeDrLuaScripts(slbId, vsId, groupId, dr, drDesSlbs, vs.getPort(), vs.getSsl());
        rewriteByLua.append(drScripts);
        String luaScripts = rewriteByLua.toString();


        // write request intercept rules
        ruleManager.write(ruleDataContext, RuleStages.STAGE_REQUEST_INTERCEPTION, confWriter);

        confWriter.startCommand("rewrite_by_lua");
        if (!luaScripts.isEmpty()) {
            confWriter.startLuaZone();
            confWriter.writeLine(luaScripts);
            confWriter.endLuaZone();
        }
        ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_LUA_REWRITE, confWriter);
        confWriter.endCommand();
        // write error page configuration if defined
        if (configHandler.getEnable("location.errorPage", slbId, vsId, groupId, false)) {
            confWriter.writeCommand("proxy_intercept_errors", "on");
        } else {
            ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_ENABLE_ERROR_PAGE, confWriter);
        }
        confWriter.writeCommand("set", "$upstream " + upstreamName);
        confWriter.writeCommand("set", LogFormat.VAR_UPSTREAM_NAME + " " + upstreamName);
        addBastionCommand(confWriter, upstreamName, slbId, vsId, groupId);
        //rewrite should be after $upstream
        addRewriteCommand(confWriter, vs, group);

        // add cookie for named loaction
        if (isNamedLocation && configHandler.getEnable("add.named.location.cookie", slbId, vsId, groupId, false)) {
            confWriter.writeCommand("add_header", "Set-Cookie \"slbshardingcookieflag=" + groupId + ";\"");
        }

        if (configHandler.getEnable("hide.hsts.response.header", slbId, vsId, groupId, true)) {
            confWriter.writeCommand("proxy_hide_header", "Strict-Transport-Security");
        }

        ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_HSTS_SUPPORT, confWriter);

        if (groupOnVs.getPath() != null && (groupOnVs.getPath().trim().equalsIgnoreCase("/") || groupOnVs.getPath().trim().equalsIgnoreCase("~* ^/"))
                && configHandler.getEnable("named.favicon", slbId, vsId, null, false)) {
            confWriter.writeIfStart("$request_uri = \"/favicon.ico\"");
            confWriter.writeCommand("error_page", "404 = @favicon");
            confWriter.writeIfEnd();
        }

        //todo: set proxy_ssl config for ssl upstream
        /*
        some cofig not supported by tengine 2.1.2 (nginx 1.6.1)
        doc link: http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_ssl_verify
                  https://docs.nginx.com/nginx/admin-guide/security-controls/securing-http-traffic-upstream/
        example:
        proxy_ssl_server_name(sni)   off;  since 1.7.0
        proxy_ssl_session_reuse  on;
        proxy_ssl_verify   off;     since 1.7.0
        etc.
        */
        ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_BOTTOM, confWriter);
        confWriter.writeCommand("proxy_pass", "$upstream_scheme://$upstream");
        confWriter.writeLocationEnd();
    }

    private String getSbu(ModelSnapshotEntity snapshot, Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap, Group group) throws Exception {
        if (extendedGroupMap != null && snapshot != null) {
            ExtendedView.ExtendedGroup extendedGroup = extendedGroupMap.get(group.getId());
            if (extendedGroup == null) {
                return null;
            }
            return PropertyValueUtils.findByName(extendedGroup.getProperties(), "SBU");
        } else {
            App app = appService.getAppByAppid(group.getAppId());
            if (app != null) {
                return app.getSbu();
            }
        }
        return null;
    }


    private String getClientAddrVar(Long slbId) throws Exception {
        if (configHandler.getEnable("unified_client_addr", slbId, null, null, false)) {
            return LogFormat.SLB_CLIENT_ADDR;
        }
        boolean proxyProtocolEnabled = configHandler.getEnable("proxy_protocol", slbId, null, null, true);
        return proxyProtocolEnabled ? "$proxy_protocol_addr" : LogFormat.REMOTE_ADDR;
    }

    private String generatePageIdLua(Long slbId, Long vsId, Long groupId) throws Exception {
        if (!configHandler.getEnable("page.id", slbId, vsId, groupId, false)) {
            return "";
        }
        return "\n local tmpPageId = ngx.req.get_headers()[\"x-ctrip-pageid\"];\n" +
                "if tmpPageId ~= nil and tmpPageId ~= \"\" then\n" +
                "ngx.var.x_ctrip_pageid = tmpPageId;\n" +
                "end\n";
    }

    private String generateSetCookieCollectingLua(Long slbId, Long vsId, Long groupId) throws Exception {
        if (!configHandler.getEnable("log.set-cookie", slbId, vsId, groupId, false)) {
            return "";
        }
        return "local setCookieValue = \"\";\n" +
                "local rawValue = ngx.header[\"Set-Cookie\"];\n" +
                "if type(rawValue) ~= \"table\" then\n" +
                "    setCookieValue = rawValue;\n" +
                "else\n" +
                "    for i,j in pairs(rawValue) do\n" +
                "        setCookieValue = setCookieValue .. j .. \"|||\";\n" +
                "    end\n" +
                "end\n" +
                "ngx.var.set_cookie_value = setCookieValue;";
    }

    private void writeRequestRestrictionConfigs(ConfWriter confWriter, Slb slb, VirtualServer vs, Group group, RuleDataContext ruleDataContext) throws Exception {
        long groupId = group != null ? group.getId() : null;
        long slbId = slb != null ? slb.getId() : null;
        long vsId = vs != null ? vs.getId() : null;

        ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_REQ_RESTRICTIONS, confWriter);

    }


    private void writeStageAccessConfigs(ConfWriter confWriter, Slb slb, VirtualServer vs, Group group, RuleDataContext ruleDataContext) throws Exception {
        long groupId = group.getId();
        long slbId = slb.getId();
        long vsId = vs.getId();

        String timeOutValue = configHandler.getStringValue(proxyReadTimeoutEnableKey, slbId, vsId, groupId, "60");


        confWriter.writeCommand("proxy_next_upstream", "off");
        ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_ACCESS, confWriter);
    }

    private String generateHeaderSizeLua(Long slbId, Long vsId, Long groupId) throws Exception {
        if (!configHandler.getEnable("header.size", slbId, vsId, groupId, false)) {
            return "";
        }
        String result = "local cookie = ngx.var.http_cookie;\n" +
                "ngx.var.cookie_size = 0;\n" +
                "if cookie ~= nil then\n" +
                "         ngx.var.cookie_size = string.len( cookie);\n" +
                "end\n" +
                "local headers = ngx.req.get_headers();\n" +
                "local headersString = \"\";\n" +
                "ngx.var.header_size = 0;\n" +
                "for k,v in pairs(headers) do\n" +
                "   if type(v) ~= \"table\" then\n" +
                "       ngx.var.header_size = ngx.var.header_size + string.len(k);\n" +
                "       ngx.var.header_size = ngx.var.header_size + string.len(v);\n" +
                "       if k ~= \"cookie\" then" +
                "           headersString = headersString..k..\":\"..v..\";\";\n" +
                "       end\n" +
                "   else\n" +
                "       for i,j in pairs(v) do\n" +
                "           ngx.var.header_size = ngx.var.header_size + string.len(i);\n" +
                "           ngx.var.header_size = ngx.var.header_size + string.len(j);\n" +
                "           headersString = headersString..i..\":\"..j..\";\";\n" +
                "       end\n" +
                "   end\n" +
                "end\n";
        if (configHandler.getEnable("header.size.log.large.headers", slbId, vsId, groupId, false)) {
            result += "local hs = ngx.var.header_size - ngx.var.cookie_size;\n"
                    + " if ngx.var.header_size ~= \"-\" and hs > " + configHandler.getIntValue("header.size", slbId, vsId, groupId, 10240) + " then\n" +
                    "     ngx.var.header_value = string.gsub(headersString,\" \", \"\");\n" +
                    "end\n";
        }
        return result;
    }

    private void addRegionHeader(ConfWriter confWriter, Long slbId, Long vsId, Long groupId, ModelSnapshotEntity snapshot) throws Exception {
        if (configHandler.getEnable("header.x.ctrip.source.region", slbId, vsId, groupId, false)) {
            String region = null;
            if (snapshot != null) {
                if (snapshot.getModels().getSlbs().containsKey(slbId)) {
                    ExtendedView.ExtendedSlb slb = snapshot.getModels().getSlbs().get(slbId);
                    region = PropertyValueUtils.findByName(slb.getProperties(), "region");
                }
            } else {
                Property property = propertyService.getProperty("region", slbId, "slb");
                if (property != null) {
                    region = property.getValue();
                }
            }
            if (region != null) {
                confWriter.writeCommand("proxy_set_header", "x-ctrip-source-region " + region.toUpperCase());
            }
        }
    }

    private String writeXFFAndCanary(ConfWriter confWriter, Long slbId, Long vsId, Long groupId, Group group, String canaryIp, ModelSnapshotEntity snapshot, Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap) throws Exception {
        confWriter.writeCommand("set", LogFormat.CANARY_FLAG + " -");
        StringBuilder rewriteByLua = new StringBuilder();
        if (configHandler.getEnable("location.clear.x-forwarded-for", slbId, vsId, groupId, false)) {
            confWriter.writeCommand("proxy_set_header", "X-Forwarded-For \"\"");
        } else if (configHandler.getEnable("location.x-forwarded-for", slbId, vsId, groupId, true)) {
            confWriter.writeIfStart(getClientAddrVar(slbId) + " ~* \"" +
                    configHandler.getStringValue("location.x-forwarded-for.white.list", slbId, vsId, groupId, "172\\..*|192\\.168.*|10\\..*") + "\"")
                    .writeCommand("set", "$inWhite \"true\"")
                    .writeIfEnd();
            confWriter.writeIfStart(getClientAddrVar(slbId) + " ~* \"" +
                    configHandler.getStringValue("location.x-forwarded-for.black.list", slbId, vsId, groupId, "none") + "\"")
                    .writeCommand("set", "$inWhite \"false\"")
                    .writeIfEnd();
            confWriter.writeCommand("set", "$" + TRUE_CLIENT_IP_VAR + " " + getClientAddrVar(slbId));
            rewriteByLua.append(setHeaderLuaScripts.replace("{{client_addr_var}}", getClientAddrVar(slbId).substring(1)));

        } else {
            confWriter.writeCommand("proxy_set_header", "X-Forwarded-For $proxy_add_x_forwarded_for");
        }

        if (configHandler.getEnable("bastion.ip", slbId, vsId, groupId, true)) {
            rewriteByLua.append(getCanaryIpLua(group, canaryIp, slbId, vsId, snapshot, extendedGroupMap));
        }
        return rewriteByLua.toString();
    }

    private String writeDrLuaScripts(Long slbId, Long vsId, Long groupId, Dr dr, Map<Long, Integer> drDesSlbs, String port, Boolean isSsl) throws Exception {
        StringBuilder rewriteByLua = new StringBuilder();
        if (dr == null || drDesSlbs == null || drDesSlbs.size() == 0 || !configHandler.getEnable("app.dr", slbId, vsId, groupId, true)) {
            rewriteByLua.append("\nngx.req.clear_header(\"x-ctrip-drid\")\n");
        } else {
            drDesSlbs = new TreeMap<>(drDesSlbs);
            double totalWeight = 100.0;
            int currentWeight = 0, prevWeight = 0;

            rewriteByLua.append("\nlocal r = math.random()\n");

            boolean first = true;
            for (Map.Entry<Long, Integer> entry : drDesSlbs.entrySet()) {
                currentWeight += entry.getValue();
                if (first) {
                    rewriteByLua.append("if");
                    first = false;
                } else {
                    rewriteByLua.append("elseif");
                }
                rewriteByLua.append(" (r >= " + String.format("%.2f", prevWeight / totalWeight) + " and r < " + String.format("%.2f", currentWeight / totalWeight) + ") then\n")
                        .append("  ngx.var.upstream = \"slb_" + entry.getKey() + "_" + port + "\";\n")
                        .append("  ngx.var.upstream_scheme = " + (Boolean.TRUE.equals(isSsl) ? "\"https\"" : "\"http\"") + ";\n")
                        .append("  ngx.req.set_header(\"x-ctrip-drid\", \"" + dr.getId() + "\")\n")
                        .append("  local uri = string.gsub(ngx.var.request_uri, \"?.*\", \"\")\n")
                        .append("  ngx.req.set_uri(uri, false)\n")
                        //todo deal queryString?
                        .append("  if(ngx.var.dr_name == \"-\") then\n")
                        .append("  ngx.var.dr_name = \"dr_out_" + dr.getId() + "\"\n")
                        .append("  else\n")
                        .append("  ngx.var.dr_name = \"dr_pass_" + dr.getId() + "\"\n")
                        .append("  end\n");
                prevWeight = currentWeight;
            }
            if (!first) {
                rewriteByLua.append("else  ngx.req.clear_header(\"x-ctrip-drid\")\n");
                rewriteByLua.append("end\n");
            } else {
                rewriteByLua.append("ngx.req.clear_header(\"x-ctrip-drid\")\n");
            }
        }
        return rewriteByLua.toString();
    }

    private String getCanaryIpLua(Group group, String canaryIp, Long slbId, Long vsId, ModelSnapshotEntity snapshot, Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap) throws Exception {
        boolean gatewayEnabled = configHandler.getEnable("bastion.gateway", slbId, vsId, group.getId(), true);
        // No matter whether the canary server is correctly configured in SLB or not, always forward canary requests to canary gateway.
        boolean alwaysForwardToGateway = configHandler.getEnable("bastion.gateway.forwardAlways", slbId, vsId, group.getId(), false);

        GroupServer bastionServer = null;
        if (canaryIp != null) {
            for (GroupServer gs : group.getGroupServers()) {
                if (gs.getIp().equalsIgnoreCase(canaryIp)) {
                    bastionServer = gs;
                    break;
                }
            }
        }
        if (bastionServer == null && (!gatewayEnabled || !alwaysForwardToGateway)) {
            return "";
        }

        StringBuilder sb = new StringBuilder(128);
        String bastionClientIpPattern = configHandler.getStringValue("bastion.client-ip.pattern", slbId, vsId,
                group.getId(), null);
        if (bastionClientIpPattern != null) {
            sb.append("\nlocal hasArgsFlag = ngx.req.get_uri_args()[\"isCtripCanaryReq\"] == \"1\";");
            sb.append("\nlocal hasCookieFlag = ngx.var[\"cookie_ctrip-canary-req\"] == \"1\";");
            sb.append("\nlocal hasHeaderFlag = ngx.var.http_x_ctrip_canary_req == \"1\" or ngx.var.http_x_ctx_canaryreq == \"1\";");
            sb.append("\nlocal isCanaryTestIp = ngx.re.match(ngx.var.").append(getClientAddrVar(slbId).substring(1))
                    .append(", [=[")
                    .append(bastionClientIpPattern.replaceAll("\\\\", "\\\\\\\\"))
                    .append("]=]);");
            sb.append("\nif hasArgsFlag or hasCookieFlag or hasHeaderFlag or isCanaryTestIp then");
        } else {
            sb.append("\nlocal ags = ngx.req.get_uri_args();");
            sb.append("\nlocal c = ngx.var[\"cookie_ctrip-canary-req\"];");
            sb.append("\nlocal h = ngx.var.http_x_ctrip_canary_req;");
            sb.append("\nlocal h_new = ngx.var.http_x_ctx_canaryreq;");
            sb.append("\nif ags[\"isCtripCanaryReq\"] == \"1\" or c == \"1\" or h == \"1\" or h_new == \"1\" then");
        }
        sb.append("\nngx.req.set_header(\"x-ctrip-canary-req\", \"1\");");
        sb.append("\nngx.req.set_header(\"x-ctx-CanaryReq\", \"1\");");
        if (gatewayEnabled) {
            sb.append("\nngx.var.upstream = \"").append(UpstreamsConf.CANARY_GATEWAY_UPSTREAM).append("\";");
            sb.append("\nngx.var.upstream_scheme = \"http\";");
            sb.append("\nngx.var.canary_req = \"canary_gateway\";");
            sb.append("\nngx.req.set_header(\"x-ctrip-canary-appid\", \"").append(group.getAppId()).append("\");");
            sb.append("\nngx.req.set_header(\"x-ctrip-canary-slbgroupid\", \"").append(group.getId()).append("\");");
            // According to local test result, it will be quite slow if we enable outputting cmsGroupId due to huge amount of DB queries.
            // So it is disabled by default.
            boolean includeCmsGroupId = configHandler.getEnable("bastion.gateway.headers.cmsGroupId", slbId, vsId, group.getId(), false);
            if (includeCmsGroupId) {
                String cmsGroupId = null;
                if (snapshot != null && !extendedGroupMap.isEmpty()) {
                    ExtendedView.ExtendedGroup extendedGroup = extendedGroupMap.get(group.getId());
                    if (extendedGroup != null) {
                        cmsGroupId = PropertyValueUtils.findByName(extendedGroup.getProperties(), "cmsGroupId");
                    }
                } else {
                    cmsGroupId = propertyService.getPropertyValue("cmsGroupId", group.getId(), ItemTypes.GROUP, "");
                }
                sb.append("\nngx.req.set_header(\"x-ctrip-canary-cmsgroupid\", \"").append(cmsGroupId).append("\");");
            }
        } else {
            sb.append("\nngx.var.upstream = \"").append(bastionServer.getIp()).append(":").append(bastionServer.getPort()).append("\";");
            sb.append("\nngx.var.canary_req = \"canary_").append(bastionServer.getIp()).append(":").append(bastionServer.getPort()).append("\";");
        }
        sb.append("\nend");
        return sb.toString();
    }

    private void writeAccessControlCommand(ConfWriter confWriter, Long slbId, Long vsId, Long groupId, ModelSnapshotEntity snapshot, Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap) throws Exception {
        if (!configHandler.getEnable("access.control.command", slbId, vsId, groupId, false) ||
                configHandler.getEnable(ACCESS_CONTROL_RULE_ENABLED_KEY, slbId, vsId, groupId, true)) {
            return;
        }
        String accessType = null;
        try {
            if (snapshot != null && !extendedGroupMap.isEmpty()) {
                ExtendedView.ExtendedGroup extendedGroup = extendedGroupMap.get(groupId);
                if (extendedGroup != null) {
                    accessType = PropertyValueUtils.findByName(extendedGroup.getProperties(), "accessControl");
                }
            } else {
                Property property = propertyService.getProperty("accessControl", groupId, "group");
                if (property != null) {
                    accessType = property.getValue();
                }
            }
        } catch (Exception e) {
            logger.error("Get Property of group failed. GroupId:" + groupId + ";Pname:accessType");
        }
        if (accessType != null && accessType.equalsIgnoreCase("true")) {
            if (configHandler.getEnable("access.control.white.list", true)) {
                String allowList = configHandler.getStringValue("access.control.allow.list", null);
                if (allowList == null) return;
                String[] list = allowList.split(";");
                for (String d : list) {
                    confWriter.writeCommand("allow", d);
                }
                confWriter.writeCommand("deny", "all");
            } else {
                String denyList = configHandler.getStringValue("access.control.deny.list", null);
                if (denyList == null) return;
                String[] list = denyList.split(";");
                for (String d : list) {
                    confWriter.writeCommand("deny", d);
                }
            }
        }
    }

    private void writeSlbUrlCheck(ConfWriter confWriter, Long slbId, Long vsId, Long groupId) throws Exception {
        if (configHandler.getEnable("slb.url.check.flag", slbId, vsId, groupId, true)) {
            confWriter.writeIfStart("$http_" + configHandler.getStringValue("slb.url.check.header.name", "thisfieldusedforslburlcheck") + " = \"true\"");
            confWriter.writeCommand("return", " 200 \"GroupId=" + groupId + ";env=" + localInfoService.getEnv() + "\"");
            confWriter.writeIfEnd();
        }
    }

    private void writeDrSource(ConfWriter confWriter, Long slbId, Long vsId, Long groupId) throws Exception {
        if (configHandler.getEnable("app.dr.read.source", slbId, vsId, groupId, true)) {
            confWriter.writeIfStart("$http_x_ctrip_drid");
            confWriter.writeCommand("set", LogFormat.VAR_DR_NAME + " \"dr_in_${http_x_ctrip_drid}\"");
            confWriter.writeIfEnd();
        }
    }


    private void writeVirtualLocation(ConfWriter confWriter, String upstreamName, GroupVirtualServer gvs, Group group, Slb slb, List<Rule> defaultRules, boolean isNamedLocation) throws Exception {
        writeLocationStart(confWriter, gvs, group, slb, defaultRules, isNamedLocation);

        confWriter.writeCommand("set", "$upstream " + upstreamName);
        confWriter.writeCommand("set", LogFormat.VAR_UPSTREAM_NAME + " " + upstreamName);
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");

        if (!Strings.isNullOrEmpty(gvs.getCustomConf())) {
            confWriter.write(gvs.getCustomConf());
        } else {
            addRedirectCommand(confWriter, gvs);
        }

        confWriter.writeLocationEnd();
    }

    /**
     * Start a location conf and generate conf to handle multiple location entries (path+name) automatically.
     */
    private void writeLocationStart(ConfWriter confWriter,
                                    GroupVirtualServer gvs,
                                    Group group,
                                    Slb slb,
                                    List<Rule> defaultRules,
                                    boolean isNamedLocation) throws Exception {
        if (isNamedLocation) {
            // Legacy named group handling for traffic policies.
            confWriter.writeLocationStart("@group_" + group.getId());
            return;
        }
        VirtualServer vs = gvs.getVirtualServer();

        // Rules
        List<Rule> groupRules = group.getRuleSet();
        List<Rule> vsRules = vs.getRuleSet();
        List<Rule> slbRules = slb.getRuleSet();
        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, groupRules, vsRules, slbRules);

        // others
        if (gvs.getPath() != null && !gvs.getPath().isEmpty()) {
            confWriter.writeLocationStart(gvs.getPath());
            if (gvs.getName() != null) {
                writeRequestRestrictionConfigs(confWriter, slb, vs, group, ruleDataContext);
                confWriter.writeCommand("content_by_lua", "'ngx.exec(\"" + gvs.getName() + "\")'");
                confWriter.writeLocationEnd();
            }
        }
        if (gvs.getName() != null) {
            confWriter.writeLocationStart(gvs.getName());
        }
    }

    private static String getRewrite(VirtualServer vs, Group group) throws Exception {
        String res = null;
        for (GroupVirtualServer groupSlb : group.getGroupVirtualServers()) {
            if (groupSlb.getVirtualServer().getId().longValue() == vs.getId().longValue()) {
                res = groupSlb.getRewrite();
            }
        }
        return res;
    }

    private static void addRewriteCommand(ConfWriter confWriter, VirtualServer vs, Group group) throws Exception {
        if (confWriter != null) {
            String rewrite = getRewrite(vs, group);
            if (rewrite == null || rewrite.isEmpty()) {
                return;
            }
            List<String> rewriteList = PathRewriteParser.getValues(rewrite);
            for (String tmp : rewriteList) {
                confWriter.writeCommand("rewrite", tmp + " break");
            }
        }
    }

    private static void addRedirectCommand(ConfWriter confWriter, GroupVirtualServer groupVirtualServer) throws Exception {
        if (confWriter != null) {
            String redirect = groupVirtualServer.getRedirect();
            if (redirect.isEmpty())
                return;
            List<String> redirectList = PathRewriteParser.getValues(redirect);
            for (String tmp : redirectList) {
                confWriter.writeCommand("rewrite", tmp + " redirect");
            }
        }
    }

    private void addBastionCommand(ConfWriter confWriter, String upstreamName, Long slbId, Long vsId, Long groupId) throws Exception {
        String whiteList = configHandler.getStringValue("location.bastion.white.list", slbId, vsId, groupId, "10..*|172..*|192..*");

        confWriter.writeIfStart(getClientAddrVar(slbId) + " ~* \"" + whiteList + "\"")
                .writeCommand("set", "$upstream $cookie_bastion")
                .writeIfEnd();
        confWriter.writeIfStart("$upstream = \"\"")
                .writeCommand("set", "$upstream " + upstreamName)
                .writeIfEnd();
        confWriter.writeIfStart("$upstream != " + upstreamName)
                .writeCommand("add_header", "Bastion $cookie_bastion")
                .writeIfEnd();
    }

    public void writeHealthCheckLocation(ConfWriter confWriter, Long slbId, Long vsId) throws Exception {
        confWriter.writeLocationStart("~* ^/do_not_delete/noc.gif$");
        confWriter.writeCommand("add_header", "Accept-Ranges bytes");
        confWriter.writeCommand("content_by_lua", getHcLuaScripts(slbId, vsId));
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeLocationEnd();
        if (configHandler.getEnable("nocbig.jpg", slbId, vsId, null, false)) {
            confWriter.writeLocationStart("~* ^/do_not_delete/nocbig.jpg$");
            confWriter.writeCommand("add_header", "Accept-Ranges bytes");
            confWriter.writeCommand("content_by_lua", getBHcLuaScripts(slbId, vsId));
            confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
            confWriter.writeLocationEnd();
        }
    }

    public void writeDefaultHealthCheckLocation(ConfWriter confWriter) throws Exception {
        confWriter.writeLocationStart("~* ^/do_not_delete/noc.gif$");
        confWriter.writeCommand("add_header", "Accept-Ranges bytes");
        confWriter.writeCommand("content_by_lua", getHcLuaScripts(null, null));
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeLocationEnd();
        confWriter.writeLocationStart("~* ^/do_not_delete/nocbig.jpg$");
        confWriter.writeCommand("add_header", "Accept-Ranges bytes");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeCommand("content_by_lua", getBHcLuaScripts(null, null));
        confWriter.writeLocationEnd();
    }

    private String getHcLuaScripts(Long slbId, Long vsId) throws Exception {
        return new StringBuilder(512).append("'\n")
                //TODO hardcode health check gif
                .append("local res = ngx.decode_base64(\"").append(configHandler.getStringValue("location.vs.health.check.gif.base64", slbId, vsId, null, "")).append("\");\n")
                .append("ngx.print(res);\n")
                .append("return ngx.exit(200);'").toString();
    }

    private String getBHcLuaScripts(Long slbId, Long vsId) throws Exception {
        return new StringBuilder(2048).append("'\n")
                //TODO hardcode Noc health check gif
                .append("local res = ngx.decode_base64(\"").append(configHandler.getStringValue("location.vs.health.check.bigjpg.base64", slbId, vsId, null, "")).append("\");\n")
                .append("ngx.print(res);\n")
                .append("return ngx.exit(200);'").toString();
    }


    public void writeDyupsLocation(ConfWriter confWriter) {
        confWriter.writeLocationStart("/");
        confWriter.writeLine("dyups_interface;");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeLocationEnd();
    }


    public void writeDefaultLocations(ConfWriter confWriter, Long slbId) throws Exception {
        confWriter.writeLocationStart("= /domaininfo/OnService.html");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeLine("return 200 \"4008206666\";");
        confWriter.writeLocationEnd();

        confWriter.writeLocationStart("= /domaininfo/whoami");
        confWriter.writeCommand("default_type", "text/plain");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeLine("return 200 \"" + slbId + "\";");
        confWriter.writeLocationEnd();

        confWriter.writeLocationStart("/");
        confWriter.writeCommand("set", LogFormat.VAR_UPSTREAM_NAME + " 127.0.0.1");
        confWriter.writeCommand("set", LogFormat.VAR_POLICY_NAME + " -");
        confWriter.writeCommand("set", LogFormat.VAR_DR_NAME + " -");
        confWriter.writeCommand("set", LogFormat.CANARY_FLAG + " -");
        confWriter.writeCommand("set", LogFormat.SBU + " -");
        confWriter.writeCommand("set", LogFormat.APP + " -");
        confWriter.writeCommand("set", LogFormat.HEADER_VALUE + " -");
        confWriter.writeCommand("set", LogFormat.PAGE_ID_HEADER + " -");
        confWriter.writeCommand("set", LogFormat.SET_COOKIE_VALUE + " -");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        //add header size and cookie size check
        confWriter.writeCommand("set", LogFormat.HEADER_SIZE + " -");
        confWriter.writeCommand("set", LogFormat.COOKIE_SIZE + " -");
        confWriter.writeCommand("set", LogFormat.INTERCEPT_STATUS + " -");
        confWriter.writeCommand("default_type", "text/html");
        confWriter.writeCommand("charset", "utf-8");
        if (configHandler.getEnable("default.location.intercept.errors", slbId, null, null, false)) {
            confWriter.writeCommand("proxy_intercept_errors", "on");
        }

        confWriter.writeLine("return 404 \"Not Found!\n1、软路由服务无法匹配到该域名，请检查域名指向是否正确；\n2、请检查是否成功申请了域名。\";");
        confWriter.writeLocationEnd();

    }

    public void writeDefaultRootLocation(ConfWriter confWriter) {
        confWriter.writeLocationStart("/");
        confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
        confWriter.writeCommand("return", "404");
        confWriter.writeLocationEnd();
    }

    public void writeDefaultFaviconLocation(ConfWriter confWriter, Long slbId, Long vsId) throws Exception {
        if (configHandler.getEnable("default.favicon", slbId, vsId, null, true)) {
            confWriter.writeLocationStart("/favicon.ico");
            confWriter.writeCommand("add_header", "Accept-Ranges bytes");
            confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
            confWriter.writeCommand("content_by_lua", getFaviconLuaScripts(slbId, vsId));
            confWriter.writeLocationEnd();
        }
    }

    private String getFaviconLuaScripts(Long slbId, Long vsId) throws Exception {
        return new StringBuilder(512).append("'\n")
                //TODO hardcode health check gif
                .append("local res = ngx.decode_base64(\"").append(configHandler.getStringValue("location.vs.favicon.base64", slbId, vsId, null, "")).append("\");\n")
                .append("ngx.print(res);\n")
                .append("return ngx.exit(200);'").toString();
    }

    public void writeDefaultImageDownloadLocation(ConfWriter confWriter, Long slbId, Long vsId) throws Exception {
        if (configHandler.getEnable("default.download.image", slbId, vsId, null, false)) {
            confWriter.writeLocationStart("= /slb_download_test_image.png");
            confWriter.writeCommand("set", LogFormat.METHOD + " $request_method");
            confWriter.writeCommand("root", configHandler.getStringValue("file.root.path", "/opt/app/nginx/conf/file"));
            confWriter.writeLocationEnd();
        }
    }
}
