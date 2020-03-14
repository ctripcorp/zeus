package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.file.SessionTicketService;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.rule.RuleManager;
import com.ctrip.zeus.service.rule.model.RuleDataContext;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.verify.verifier.PropertyValueUtils;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static com.ctrip.zeus.service.build.util.RuleEnableKeys.DEFAULT_DOWNLOAD_IMAGE_RULE_ENABLED_KEY;
import static com.ctrip.zeus.service.build.util.RuleEnableKeys.SSL_SESSION_CACHE_RULE_ENABLED_KEY;

;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("serverConf")
public class ServerConf {
    @Resource
    ConfigHandler configHandler;
    @Resource
    SessionTicketService sessionTicketService;
    @Autowired
    LocationConf locationConf;
    @Resource
    private RuleManager ruleManager;
    @Resource
    private PropertyService propertyService;

    public static final String SSL_PATH = "/data/nginx/ssl/";
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public String generate(Slb slb, VirtualServer vs, List<TrafficPolicy> policies, List<Group> groups,
                           Map<Long, Map<Long, Integer>> drDesSlbByGroupIds, Map<Long, Dr> drByGroupIds,
                           Map<String, Object> objectOnVsReferrer, Map<Long, String> canaryIpMap,
                           List<Rule> defaultRules, ModelSnapshotEntity snapshot) throws Exception {
        Long slbId = slb.getId();
        Long vsId = vs.getId();

        Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap = new HashMap<>();
        if (snapshot != null) {
            List<ExtendedView.ExtendedGroup> extendedGroups = snapshot.getModels().getGroupReferrerOfVses().get(vsId);
            extendedGroups = extendedGroups == null ? new ArrayList<>() : extendedGroups;
            extendedGroups.forEach(e -> extendedGroupMap.put(e.getId(), e));
        }


        ConfWriter confWriter = new ConfWriter(1024, true);
        try {
            Integer.parseInt(vs.getPort());
        } catch (Exception e) {
            throw new ValidationException("virtual server [" + vs.getId() + "] port is illegal!");
        }
        //TODO Stage Of HEAD
        List<Rule> vsRules = vs.getRuleSet();
        List<Rule> slbRules = slb.getRuleSet();
        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, vsRules, slbRules);
        logger.info("[Model Snapshot Test]Start generate Conf:");
        confWriter.writeServerStart();
        boolean proxyProtocolEnabled = configHandler.getEnable("proxy_protocol", slbId, null, null, true);
        confWriter.writeCommand("listen", !proxyProtocolEnabled ? vs.getPort() : vs.getPort() + " proxy_protocol");
        writeSlbClientAddrSetter(confWriter, proxyProtocolEnabled);
        writeSlbDefaultSet(confWriter);
        if (vs.isSsl()) {
            ruleManager.write(ruleDataContext, RuleStages.STAGE_SERVER_HTTP2_CONFIG, confWriter);
        }
        confWriter.writeCommand("server_name", getServerNames(vs));
        logger.info("[Model Snapshot Test]Start generate server access:");
        ruleManager.write(ruleDataContext, RuleStages.STAGE_SERVER_ACCESS, confWriter);
        logger.info("[Model Snapshot Test]finish  generate server access Conf:");
        confWriter.writeCommand("ignore_invalid_headers", "off");
        confWriter.writeCommand("proxy_http_version", "1.1");

        if (vs.isSsl()) {
            confWriter.writeCommand("ssl", "on");
            confWriter.writeCommand("ssl_certificate", SSL_PATH + vsId + "/ssl.crt");
            confWriter.writeCommand("ssl_certificate_key", SSL_PATH + vsId + "/ssl.key");

            ruleManager.write(ruleDataContext, RuleStages.STAGE_SERVER_SSL_CONFIG, confWriter);

            if (!configHandler.getEnable(SSL_SESSION_CACHE_RULE_ENABLED_KEY, slbId, vsId, null, true)) {
                confWriter.writeCommand("ssl_session_cache", configHandler.getStringValue("ssl.session.cache", slbId, vsId, null, "shared:SSL:20m"));
                confWriter.writeCommand("ssl_session_timeout", configHandler.getStringValue("ssl.session.cache.timeout", slbId, vsId, null, "180m"));
            }

            if (configHandler.getEnable("session.ticket", slbId, vsId, null, false) && sessionTicketService.getCurrentSessionTicketFile(slbId) != null) {
                confWriter.writeCommand("ssl_session_tickets", "on");
                String path = configHandler.getStringValue("session.ticket.key.path", "/opt/app/nginx/conf/ticket");
                String fileName = configHandler.getStringValue("session.ticket.key.file", "sessionTicket.key");
                confWriter.writeCommand("ssl_session_ticket_key", path + "/" + fileName);
            }
        }


        if (configHandler.getEnable("server.vs.health.check", slbId, vsId, null, false)) {
            locationConf.writeHealthCheckLocation(confWriter, slbId, vsId);
        }

        if (configHandler.getEnable("conf.variable.idc", slbId, null, null, true)) {
            String idcCode = null;
            if (snapshot != null) {
                if (snapshot.getModels().getSlbs().containsKey(slbId)) {
                    ExtendedView.ExtendedSlb extendedSlb = snapshot.getModels().getSlbs().get(slbId);
                    idcCode = PropertyValueUtils.findByName(extendedSlb.getProperties(), "idc_code");
                }
            } else {
                Property property = propertyService.getProperty("idc_code", slbId, "slb");
                idcCode = property != null ? property.getValue() : "-";
            }
            confWriter.writeCommand("set", "$idc \"" + idcCode + "\"");
        }
        logger.info("[Model Snapshot Test]start  generate location Conf: " + vsId);
        generateLocations(slb, vs, objectOnVsReferrer, policies, groups, drDesSlbByGroupIds, drByGroupIds, confWriter, canaryIpMap, defaultRules, snapshot, extendedGroupMap);
        logger.info("[Model Snapshot Test]finish  generate location Conf:" + vsId);
        ruleManager.write(ruleDataContext, RuleStages.STAGE_SERVER_ERROR_PAGE, confWriter);

        addDefaultRootLocation(slbId, vsId, groups, confWriter);

        // Favicon rule
        ruleManager.write(ruleDataContext, RuleStages.STAGE_FAVOR_ICON, confWriter);

        if (!configHandler.getEnable(DEFAULT_DOWNLOAD_IMAGE_RULE_ENABLED_KEY, slbId, vsId, null, true)) {
            locationConf.writeDefaultImageDownloadLocation(confWriter, slbId, vsId);
        }

        //TODO RULE Stage BOTTOM
        ruleManager.write(ruleDataContext, RuleStages.STAGE_SERVER_BOTTOM, confWriter);
        logger.info("[Model Snapshot Test]finish  generate server Conf:");

        confWriter.writeServerEnd();
        return confWriter.getValue();
    }

    private void generateLocations(Slb slb, VirtualServer vs, Map<String, Object> objectOnVsReferrer,
                                   List<TrafficPolicy> policies, List<Group> groups,
                                   Map<Long, Map<Long, Integer>> drDesSlbByGroupIds, Map<Long, Dr> drByGroupIds,
                                   ConfWriter confWriter, Map<Long, String> canaryIpMap, List<Rule> defaultRules, ModelSnapshotEntity snapshot, Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap) throws Exception {
        LinkedHashMap<String, List<Group>> ruleGroups = new LinkedHashMap<>();

        //put groups of same path in the same list,list sorted by priority desc
        for (int i = 0; i < groups.size(); i++) {
            Group currentGroup = groups.get(i);
            String currentPath = ((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + currentGroup.getId())).getPath();
            if (currentPath == null) {
                // Use an empty string as the default value with those name-only GVSes.
                currentPath = "";
            }
            List<Group> currentGroupList = ruleGroups.get(currentPath);
            if (currentGroupList == null) {
                currentGroupList = new ArrayList<>();
                currentGroupList.add(currentGroup);
                ruleGroups.put(currentPath, currentGroupList);
            } else {
                currentGroupList.add(currentGroup);
            }
        }

        // list of groupLists of the same path, groupList is sorted by priority desc
        List<List<Group>> sortedGroups = new ArrayList<>(ruleGroups.values());

        List<Group> processingGroups = null;
        TrafficPolicy policy = null;
        List<GroupVirtualServer> groupVirtualServers = null;
        PolicyVirtualServer policyVirtualServer = null;

        // Record IDs of those groups for which we shall generate a named group location (@group_{id}).
        Set<Long> namedGroups = new HashSet<>();
        int gIdx = 0;
        int pIdx = 0;
        while (gIdx < sortedGroups.size() && pIdx < policies.size()) {
            if (processingGroups == null) {
                processingGroups = sortedGroups.get(gIdx);
                groupVirtualServers = new ArrayList<>();
                for (Group group : processingGroups) {
                    groupVirtualServers.add((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + group.getId()));
                }
            }

            if (policy == null) {
                policy = policies.get(pIdx);
                policyVirtualServer = (PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + policy.getId());
            }

            if (policyVirtualServer.getPriority() - groupVirtualServers.get(0).getPriority() >= 0) {
                writeTrafficPolicy(slb, vs, confWriter, policy, policyVirtualServer, namedGroups, groups, defaultRules);
                pIdx++;
                policy = null;
                policyVirtualServer = null;
            } else {
                writeGroupsWithSamePaths(slb, vs, confWriter, canaryIpMap, processingGroups, drDesSlbByGroupIds, drByGroupIds, groupVirtualServers, namedGroups, defaultRules, snapshot, extendedGroupMap);
                gIdx++;
                processingGroups = null;
                groupVirtualServers = null;
            }
        }
        while (pIdx < policies.size()) {
            policy = policies.get(pIdx);
            policyVirtualServer = (PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + policy.getId());
            writeTrafficPolicy(slb, vs, confWriter, policy, policyVirtualServer, namedGroups, groups, defaultRules);
            pIdx++;
        }
        while (gIdx < sortedGroups.size()) {
            processingGroups = sortedGroups.get(gIdx);
            groupVirtualServers = new ArrayList<>();
            for (Group group : processingGroups) {
                groupVirtualServers.add((GroupVirtualServer) objectOnVsReferrer.get("gvs-" + group.getId()));
            }
            writeGroupsWithSamePaths(slb, vs, confWriter, canaryIpMap, processingGroups, drDesSlbByGroupIds, drByGroupIds, groupVirtualServers, namedGroups, defaultRules, snapshot, extendedGroupMap);
            gIdx++;
        }
        for (Group group : groups) {
            long id = group.getId();
            if (!namedGroups.contains(id)) {
                continue;
            }
            if (drDesSlbByGroupIds == null) {
                locationConf.write(confWriter, slb, vs, group, null, (GroupVirtualServer) objectOnVsReferrer.get("gvs-" + group.getId()), canaryIpMap.get(id), null, defaultRules, true, snapshot, extendedGroupMap);
            } else {
                locationConf.write(confWriter, slb, vs, group, drByGroupIds.get(group.getId()), (GroupVirtualServer) objectOnVsReferrer.get("gvs-" + group.getId()), canaryIpMap.get(id), drDesSlbByGroupIds.get(group.getId()), defaultRules, true, snapshot, extendedGroupMap);
            }
        }
    }

    private void writeTrafficPolicy(Slb slb, VirtualServer vs, ConfWriter confWriter, TrafficPolicy policy, PolicyVirtualServer policyVirtualServer,
                                    Set<Long> namedGroups, List<Group> groups, List<Rule> defaultRules) throws Exception {
        Map<Long, Group> controlledGroups = new HashMap<>();
        for (TrafficControl c : policy.getControls()) {
            namedGroups.add(c.getGroup().getId());
            for (Group group : groups) {
                if (group.getId().equals(c.getGroup().getId())) {
                    controlledGroups.put(group.getId(), group);
                    break;
                }
            }
        }
        locationConf.write(confWriter, slb, vs, policy, policyVirtualServer, controlledGroups, defaultRules);
    }

    private void writeGroupsWithSamePaths(Slb slb, VirtualServer vs, ConfWriter confWriter, Map<Long, String> canaryIpMap, List<Group> groups,
                                          Map<Long, Map<Long, Integer>> drDesSlbByGroupIds, Map<Long, Dr> drByGroupIds,
                                          List<GroupVirtualServer> groupVirtualServers, Set<Long> namedGroups, List<Rule> defaultRules, ModelSnapshotEntity snapshot, Map<Long, ExtendedView.ExtendedGroup> extendedGroupMap) throws Exception {
        boolean hasRouteRules = false;
        for (GroupVirtualServer groupVirtualServer : groupVirtualServers) {
            List<RouteRule> rules = groupVirtualServer.getRouteRules();
            if (!rules.isEmpty()) {
                hasRouteRules = true;
                break;
            }
        }

        if (hasRouteRules) {
            locationConf.write(confWriter, groups, groupVirtualServers, slb, vs, defaultRules);
            for (Group processingGroup : groups) {
                namedGroups.add(processingGroup.getId());
            }
        }

        for (int i = 0; i < groupVirtualServers.size(); ++i) {
            Group group = groups.get(i);
            GroupVirtualServer groupVirtualServer = groupVirtualServers.get(i);
            if (drDesSlbByGroupIds == null) {
                locationConf.write(confWriter, slb, vs, group, null, groupVirtualServer, canaryIpMap.get(group.getId()), null, defaultRules, false, snapshot, extendedGroupMap);
            } else {
                locationConf.write(confWriter, slb, vs, group, drByGroupIds.get(group.getId()), groupVirtualServer, canaryIpMap.get(group.getId()), drDesSlbByGroupIds.get(group.getId()), defaultRules, false, snapshot, extendedGroupMap);
            }
        }
    }


    private String getServerNames(VirtualServer vs) throws Exception {
        StringBuilder b = new StringBuilder(128);
        for (Domain domain : vs.getDomains()) {
            b.append(" ").append(domain.getName());
        }
        String res = b.toString();
        AssertUtils.assertNotEquals("", res.trim(), "virtual server [" + vs.getId() + "] domain is null or illegal!");
        return res;
    }


    public void writeDyupsServer(ConfWriter confWriter, Long slbId) throws Exception {
        confWriter.writeServerStart();
        confWriter.writeCommand("listen", configHandler.getStringValue("server.dyups.port", slbId, null, null, "8081"));

        locationConf.writeDyupsLocation(confWriter);

        confWriter.writeServerEnd();
    }

    public void writeBlackListServer(ConfWriter confWriter, Long slbId) throws Exception {
        confWriter.writeServerStart();
        confWriter.writeCommand("listen", configHandler.getStringValue("server.black.list.server.port", "10003"));

        confWriter.writeLocationStart("~* ^/getIp$");
        confWriter.writeCommand("content_by_lua", "'\n" +
                "           if nil == ipList[ngx.var.arg_appId] then\n" +
                "           ngx.status = 404\n" +
                "           ngx.say(\"Not Found Ip Black List By AppId:\"..ngx.var.arg_appId)\n" +
                "           return ngx.exit(404)\n" +
                "           elseif  ipList[ngx.var.arg_appId][ngx.var.arg_ip] == nil then\n" +
                "           ngx.status = 404\n" +
                "           ngx.say(\"Not In Black List.\")\n" +
                "           return ngx.exit(404)\n" +
                "           else \n" +
                "           ngx.say(\"In Black List.\")\n" +
                "           ngx.exit(200)\n" +
                "           end'");
        confWriter.writeLocationEnd();

        confWriter.writeLocationStart("~* ^/addIpList");
        confWriter.writeCommand("content_by_lua", "'\n" +
                "                ngx.req.read_body()\n" +
                "                local body_data = ngx.req.get_body_data()\n" +
                "                local lines = explodeToArray(\"\\\\\\n\",tostring(body_data))\n" +
                "                for i,v in pairs(lines) do\n" +
                "                        local line = explodeToArray(\"=\",v)\n" +
                "                        local len = (line and #line ) or 0\n" +
                "                        if len == 2 then\n" +
                "                             ngx.shared.ipList:set(line[1],line[2])\n" +
                "                        end\n" +
                "                end\n" +
                "                ngx.say(\"update success.\")\n" +
                "                ngx.exit(200)\n'");
        confWriter.writeLocationEnd();
        confWriter.writeServerEnd();
    }

    public void writeDefaultServers(ConfWriter confWriter, Slb slb, List<Rule> defaultRules) throws Exception {
        Long slbId = slb.getId();
        boolean proxyProtocolEnabled = configHandler.getEnable("proxy_protocol", slbId, null, null, true);

        confWriter.writeServerStart();

        RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, slb.getRuleSet());
        ruleManager.write(ruleDataContext, RuleStages.STAGE_DEFAULT_SERVER_LISTEN_80, confWriter);

        writeSlbClientAddrSetter(confWriter, proxyProtocolEnabled);
        writeSlbDefaultSet(confWriter);
        if (configHandler.getEnable("default.server.health.check", slbId, null, null, true)) {
            locationConf.writeDefaultHealthCheckLocation(confWriter);
        }
        locationConf.writeDefaultLocations(confWriter, slbId);

        ruleManager.write(ruleDataContext, RuleStages.STAGE_DEFAULT_SERVER_ERROR_PAGE, confWriter);

        confWriter.writeServerEnd();

        confWriter.writeServerStart();

        ruleManager.write(ruleDataContext, RuleStages.STAGE_DEFAULT_SERVER_LISTEN_443, confWriter);

        writeSlbClientAddrSetter(confWriter, proxyProtocolEnabled);
        writeSlbDefaultSet(confWriter);
        confWriter.writeCommand("ssl", "on");
        confWriter.writeCommand("ssl_certificate", SSL_PATH + "default/ssl.crt");
        confWriter.writeCommand("ssl_certificate_key", SSL_PATH + "default/ssl.key");


        ruleManager.write(ruleDataContext, RuleStages.STAGE_DEFAULT_SERVER_SSL_CONFIG, confWriter);

        if (configHandler.getEnable("default.server.session.ticket", slbId, null, null, false) && sessionTicketService.getCurrentSessionTicketFile(slbId) != null) {
            confWriter.writeCommand("ssl_session_tickets", "on");
            String path = configHandler.getStringValue("session.ticket.key.path", "/opt/app/nginx/conf/ticket");
            String fileName = configHandler.getStringValue("session.ticket.key.file", "sessionTicket.key");
            confWriter.writeCommand("ssl_session_ticket_key", path + "/" + fileName);
        }

        if (configHandler.getEnable("default.server.health.check", slbId, null, null, true)) {
            locationConf.writeDefaultHealthCheckLocation(confWriter);
        }
        locationConf.writeDefaultLocations(confWriter, slbId);

        ruleManager.write(ruleDataContext, RuleStages.STAGE_DEFAULT_SERVER_ERROR_PAGE, confWriter);
        confWriter.writeServerEnd();
    }

    public void writeWafDefaultServer(ConfWriter confWriter, Long slbId) throws Exception {
        if (!configHandler.getEnable("waf", slbId, null, null, false)
                && !configHandler.getEnable("waf.canary.build", slbId, null, null, false)) {
            return;
        }
        if (!configHandler.getEnable("waf.default.server", true)) {
            return;
        }
        confWriter.writeServerStart();
        confWriter.writeCommand("listen", "10002");
        confWriter.writeLocationStart("= /waf");
        confWriter.writeCommand("add_header", "Content-Type text/html");
        confWriter.writeLine("return 200 \"4008206666\";");
        confWriter.writeLocationEnd();
        confWriter.writeServerEnd();
    }

    private void addDefaultRootLocation(Long slbId, Long vsId, List<Group> groups, ConfWriter confWriter) throws Exception {
        // 0. enable flag
        if (!configHandler.getEnable("default.root.location", slbId, vsId, null, true)) {
            return;
        }
        // 1. return while already have root location .
        for (Group group : groups) {
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (gvs.getVirtualServer().getId().equals(vsId)) {
                    if (gvs.getPath() == null || gvs.getPath().isEmpty()) {
                        continue;
                    }
                    if (gvs.getPath().trim().equals("/") || gvs.getPath().trim().equals("~* ^/")) {
                        return;
                    }
                }
            }
        }
        // 2. add default location instead while not found root location.
        locationConf.writeDefaultFaviconLocation(confWriter, slbId, vsId);
        locationConf.writeDefaultRootLocation(confWriter);
    }

    private void writeSlbClientAddrSetter(ConfWriter confWriter, boolean proxyProtocolEnabled) {
        confWriter.writeCommand("set", LogFormat.SLB_CLIENT_ADDR + (proxyProtocolEnabled ? " $proxy_protocol_addr" : " $remote_addr"));
    }

    private void writeSlbDefaultSet(ConfWriter confWriter) {
        confWriter.writeCommand("set", LogFormat.WAF_COST + " -");
    }
}
