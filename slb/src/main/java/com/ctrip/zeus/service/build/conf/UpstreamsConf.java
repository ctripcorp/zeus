package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.domain.LBMethod;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.snapshot.ModelEntities;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.rule.RuleManager;
import com.ctrip.zeus.service.rule.model.RuleDataContext;
import com.ctrip.zeus.service.rule.model.RuleStages;
import com.ctrip.zeus.service.verify.verifier.PropertyValueUtils;
import com.ctrip.zeus.tag.ItemTypes;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.util.AssertUtils;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@Component("upstreamsConf")
public class UpstreamsConf {
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private PropertyService propertyService;
    @Resource
    private RuleManager ruleManager;

    public static final String UpstreamPrefix = "backend_";
    public static final String CANARY_GATEWAY_UPSTREAM = "canary_gateway_http";
    private static final int MAX_CONNS_DEFAULT = 5000;
    private static final int DEFAULT_UPSTREAM_MAX_CONNS_DEFAULT = 100000;

    public void writeDefaultUpstreams(ConfWriter confWriter, Long mySlbId, ModelSnapshotEntity snapshot) throws Exception {
        List<Slb> slbs;
        if (snapshot == null) {
            Set<IdVersion> idVersionSet = slbCriteriaQuery.queryAll(SelectionMode.ONLINE_EXCLUSIVE);
            slbs = slbRepository.list(idVersionSet.toArray(new IdVersion[idVersionSet.size()]));
        } else {
            ModelEntities modelEntities = snapshot.getModels();
            List<ExtendedView.ExtendedSlb> extendedSlbs = new ArrayList<>(modelEntities.getSlbs().values());
            slbs = extendedSlbs.stream().map(ExtendedView.ExtendedSlb::getInstance).collect(Collectors.toList());
        }
        boolean intranetOnly = configHandler.getEnable("vip.check.task.intranet.only", mySlbId, null, null, true);
        for (Slb slb : slbs) {
            long slbId = slb.getId();
            if (slbId == mySlbId) continue;
            List<String> servers = new ArrayList<>();
            for (Vip vip : slb.getVips()) {
                String ip = vip.getIp();
                if (!intranetOnly || isIntranet(ip)) {
                    servers.add(ip);
                }
            }
            if (servers.size() > 0) {
                writeDefaultUpstreams(confWriter, slbId, servers, 80);
                writeDefaultUpstreams(confWriter, slbId, servers, 443);
            }
        }
        writeCanaryGatewayUpstream(confWriter, mySlbId, snapshot);
    }

    private void writeDefaultUpstreams(ConfWriter confWriter, Long slbId, List<String> servers, int port) throws Exception {
        confWriter.writeUpstreamStart("slb_" + slbId + "_" + port);
        boolean maxConnsEnabled = configHandler.getEnable("upstream.max_conns", slbId, null, null, false);
        int maxConns = configHandler.getIntValue("max.conns.default.slb.upstream.server", slbId, null, null, DEFAULT_UPSTREAM_MAX_CONNS_DEFAULT);
        for (String ip : servers) {
            if (maxConnsEnabled) {
                confWriter.writeUpstreamServer(ip, port, 5, 0, 30, false, maxConns);
            } else {
                confWriter.writeUpstreamServer(ip, port, 5, 0, 30, false);
            }
        }
        if (configHandler.getEnable("dr.upstream.keepAlive", slbId, null, null, true)) {
            confWriter.writeCommand("keepalive", configHandler.getStringValue("dr.upstream.keepAlive", slbId, null, null, "50"));
        }
        if (configHandler.getEnable("dr.upstream.keepAlive.timeout", slbId, null, null, false)) {
            confWriter.writeCommand("keepalive_timeout", configHandler.getStringValue("dr.upstream.keepAlive.timeout", slbId, null, null, "80") + "s");
        }
        confWriter.writeUpstreamEnd();
    }

    private void writeCanaryGatewayUpstream(ConfWriter confWriter, Long slbId, ModelSnapshotEntity snapshot) throws Exception {
        String canaryGatewayVip = configHandler.getStringValue("canaryGateway.vip.slb." + slbId, null);
        if (canaryGatewayVip == null) {
            String idcCode = null;
            if (snapshot != null && snapshot.getModels() != null) {
                if (snapshot.getModels().getSlbs().containsKey(slbId)) {
                    ExtendedView.ExtendedSlb extendedSlb = snapshot.getModels().getSlbs().get(slbId);
                    idcCode = PropertyValueUtils.findByName(extendedSlb.getProperties(), "idc_code");
                }
            } else {
                idcCode = propertyService.getPropertyValue("idc_code", slbId, ItemTypes.SLB, "");
            }
            if (idcCode != null && !idcCode.isEmpty()) {
                canaryGatewayVip = configHandler.getStringValue("canaryGateway.vip.idc." + idcCode, null);
            }
        }
        if (canaryGatewayVip == null) {
            canaryGatewayVip = configHandler.getStringValue("canaryGateway.vip.default", null);
        }
        if (canaryGatewayVip == null) {
            return;
        }

        confWriter.writeUpstreamStart(CANARY_GATEWAY_UPSTREAM);
        boolean maxConnsEnabled = configHandler.getEnable("upstream.max_conns", slbId, null, null, false);
        int maxConns = configHandler.getIntValue("canaryGateway.upstream.maxConns", slbId, null, null, DEFAULT_UPSTREAM_MAX_CONNS_DEFAULT);
        if (maxConnsEnabled) {
            confWriter.writeUpstreamServer(canaryGatewayVip, 80, 5, 0, 30, false, maxConns);
        } else {
            confWriter.writeUpstreamServer(canaryGatewayVip, 80, 5, 0, 30, false);
        }
        if (configHandler.getEnable("canaryGateway.upstream.keepAlive", true)) {
            String keepAliveConnections = configHandler.getStringValue("canaryGateway.upstream.keepAlive", "50");
            confWriter.writeCommand("keepalive", keepAliveConnections);
        }
        if (configHandler.getEnable("canaryGateway.upstream.keepAlive.timeout", false)) {
            String keepAliveTimeout = configHandler.getStringValue("canaryGateway.upstream.keepAlive.timeout", "80");
            confWriter.writeCommand("keepalive_timeout", keepAliveTimeout + "s");
        }
        confWriter.writeUpstreamEnd();
    }

    private boolean isIntranet(String ip) {
        if (ip == null) return false;
        return ip.startsWith("10.") || ip.startsWith("192.168.") || (ip.compareTo("172.16.") > 0 && ip.compareTo("172.32.") < 0);
    }

    public List<ConfFile> generate(Set<Long> vsLookup, VirtualServer vs, List<Group> groups,
                                   Set<String> downServers, Set<String> upServers,
                                   Set<String> visited, List<Rule> defaultRules, Slb nxOnlineSlb) throws Exception {
        List<ConfFile> result = new ArrayList<>();
        Map<String, ConfWriter> map = new HashMap<>();
        groups = groups == null ? new ArrayList<>() : groups;
        for (Group group : groups) {
            if (group.isVirtual()) continue;

            String confName = confName(vsLookup, group);
            if (visited.contains(confName)) continue;

            ConfWriter confWriter = map.get(confName);
            if (confWriter == null) {
                confWriter = new ConfWriter(1024, true);
                map.put(confName, confWriter);
            } else {
                confWriter.writeLine("");
            }
            writeUpstream(confWriter, nxOnlineSlb, vs, group, downServers, upServers, defaultRules);
        }

        for (Map.Entry<String, ConfWriter> e : map.entrySet()) {
            result.add(new ConfFile().setName(e.getKey()).setContent(e.getValue().toString()));
            visited.add(e.getKey());
        }
        return result;
    }

    private String confName(Set<Long> vsLookup, Group group) throws ValidationException {
        if (group == null || group.getGroupVirtualServers().size() == 0)
            throw new ValidationException("Invalid group information is found when generating upstream conf file. Group is either null or does not have related vs.");
        if (group.getGroupVirtualServers().size() == 1) {
            return "" + group.getGroupVirtualServers().get(0).getVirtualServer().getId();
        }
        List<Long> vsIds = new ArrayList<>();
        for (int i = 0; i < group.getGroupVirtualServers().size(); i++) {
            VirtualServer vs = group.getGroupVirtualServers().get(i).getVirtualServer();
            if (vsLookup.contains(vs.getId())) {
                vsIds.add(group.getGroupVirtualServers().get(i).getVirtualServer().getId());
            }
        }
        if (vsIds.size() == 0) {
            throw new ValidationException("Fail to generate upstream conf name according to given building vses - " + Joiner.on(",").join(vsLookup) + ".");
        }
        long[] vsArray = new long[vsIds.size()];
        for (int i = 0; i < vsIds.size(); i++) {
            vsArray[i] = vsIds.get(i).longValue();

        }
        Arrays.sort(vsArray);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(vsArray[0]);
        for (int i = 1; i < vsArray.length; i++) {
            stringBuilder.append("_").append(vsArray[i]);
        }
        return stringBuilder.toString();
    }

    public void writeUpstream(ConfWriter confWriter, Slb slb, VirtualServer vs, Group group, Set<String> allDownServers, Set<String> allUpGroupServers, List<Rule> defaultRules) throws Exception {
        Long vsId = vs.getId();
        Long slbId = slb == null ? null : slb.getId();
        Long groupId = group.getId();
        List<GroupServer> groupServers = group.getGroupServers();
        if (groupServers == null || groupServers.size() == 0) {
            return;
        }
        AssertUtils.assertNotNull(group.getId(), "groupId not found!");
        confWriter.writeUpstreamStart(UpstreamPrefix + group.getId());
        String lbMethod = LBMethod.getMethod(group.getLoadBalancingMethod().getType()).getValue();
        if (!lbMethod.isEmpty()) {
            confWriter.writeLine(lbMethod + ";");
        }

        boolean maxConnsEnabled = configHandler.getEnable("upstream.max_conns", slbId, vsId, groupId, false);

        for (GroupServer server : groupServers) {
            validate(server, vs.getId());
            String ip = server.getIp();

            int maxConns = configHandler.getIntValue("max.conns.group.server", slbId, vsId, groupId, MAX_CONNS_DEFAULT);
            if (server.getMaxConns() != null) {
                maxConns = server.getMaxConns();
            }

            boolean down = allDownServers.contains(ip) || !allUpGroupServers.contains(group.getId() + "_" + ip);
            if (maxConnsEnabled) {
                confWriter.writeUpstreamServer(ip, server.getPort(), server.getWeight(), server.getMaxFails(), server.getFailTimeout(), down, maxConns);
            } else {
                confWriter.writeUpstreamServer(ip, server.getPort(), server.getWeight(), server.getMaxFails(), server.getFailTimeout(), down);
            }
        }

        if (configHandler.getEnable("enable.rule.on.models", true)) {
            RuleDataContext ruleDataContext = ruleManager.initializeContext(defaultRules, group.getRuleSet(), vs.getRuleSet(), slb == null ? null : slb.getRuleSet());
            boolean useDefaultRules = configHandler.getEnable("enable.default.rules", slb.getId(), vs.getId(), group.getId(), true);
            ruleManager.write(ruleDataContext, RuleStages.STAGE_LOCATION_UPSTREAM_BOTTOM, confWriter, useDefaultRules);
        }

        confWriter.writeUpstreamEnd();
    }

    private void validate(GroupServer groupServer, Long vsId) throws Exception {
        AssertUtils.assertNotNull(groupServer.getPort(), "GroupServer Port config is null! virtual server " + vsId);
        AssertUtils.assertNotNull(groupServer.getWeight(), "GroupServer Weight config is null! virtual server " + vsId);
        AssertUtils.assertNotNull(groupServer.getMaxFails(), "GroupServer MaxFails config is null! virtual server " + vsId);
        AssertUtils.assertNotNull(groupServer.getFailTimeout(), "GroupServer FailTimeout config is null! virtual server " + vsId);
    }
}
