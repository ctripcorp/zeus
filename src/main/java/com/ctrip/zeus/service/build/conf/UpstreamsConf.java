package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.domain.LBMethod;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.util.AssertUtils;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
@Component("upstreamsConf")
public class UpstreamsConf {
    @Resource
    ConfigHandler configHandler;

    public static final String UpstreamPrefix = "backend_";

    public List<ConfFile> generate(Set<Long> vsLookup, VirtualServer vs, List<Group> groups,
                                   Set<String> downServers, Set<String> upServers,
                                   Set<String> visited) throws Exception {
        List<ConfFile> result = new ArrayList<>();
        Map<String, ConfWriter> map = new HashMap<>();
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
            writeUpstream(confWriter, null, vs, group, downServers, upServers);
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

    public void writeUpstream(ConfWriter confWriter, Long slbId, VirtualServer vs, Group group, Set<String> allDownServers, Set<String> allUpGroupServers) throws Exception {
        Long vsId = vs.getId();
        Long groupId = group.getId();
        List<GroupServer> groupServers = group.getGroupServers();
        if (groupServers == null || groupServers.size() == 0) {
            return;
        }
        AssertUtils.assertNotNull(group.getId(), "groupId not found!");

        if (configHandler.getEnable("socket.io.group", null, null, groupId, false)) {
            writeSocketIOUpstream(confWriter, group, groupServers, vs, allDownServers, allUpGroupServers);
            return;
        }

        confWriter.writeUpstreamStart(UpstreamPrefix + group.getId());

        String lbMethod = LBMethod.getMethod(group.getLoadBalancingMethod().getType()).getValue();
        if (!lbMethod.isEmpty()) {
            confWriter.writeLine(lbMethod + ";");
        }

        for (GroupServer server : groupServers) {
            validate(server, vs.getId());
            String ip = server.getIp();

            boolean down = allDownServers.contains(ip) || !allUpGroupServers.contains(group.getId() + "_" + ip);
            confWriter.writeUpstreamServer(ip, server.getPort(), server.getWeight(), server.getMaxFails(), server.getFailTimeout(), down);
        }

        if (configHandler.getEnable("upstream.keepAlive", slbId, vsId, groupId, false)) {
            confWriter.writeCommand("keepalive", configHandler.getStringValue("upstream.keepAlive", slbId, vsId, groupId, "100"));
        }
        if (configHandler.getEnable("upstream.keepAlive.timeout", slbId, vsId, groupId, false)) {
            confWriter.writeCommand("keepalive_timeout", configHandler.getStringValue("upstream.keepAlive.timeout", slbId, vsId, groupId, "110") + "s");
        }

        confWriter.writeUpstreamEnd();
    }

    private void writeSocketIOUpstream(ConfWriter confWriter, Group group, List<GroupServer> groupServers, VirtualServer vs, Set<String> allDownServers, Set<String> allUpGroupServers) throws Exception {
        confWriter.writeUpstreamStart(UpstreamPrefix + group.getId());
        confWriter.writeLine("ip_hash;");
        for (GroupServer server : groupServers) {
            validate(server, vs.getId());
            String ip = server.getIp();

            boolean down = allDownServers.contains(ip) || !allUpGroupServers.contains(group.getId() + "_" + ip);
            confWriter.writeUpstreamServer(ip, server.getPort(), server.getWeight(), server.getMaxFails(), server.getFailTimeout(), down);
        }
        confWriter.writeUpstreamEnd();
    }

    private void validate(GroupServer groupServer, Long vsId) throws Exception {
        AssertUtils.assertNotNull(groupServer.getPort(), "GroupServer Port config is null! virtual server " + vsId);
        AssertUtils.assertNotNull(groupServer.getWeight(), "GroupServer Weight config is null! virtual server " + vsId);
        AssertUtils.assertNotNull(groupServer.getMaxFails(), "GroupServer MaxFails config is null! virtual server " + vsId);
        AssertUtils.assertNotNull(groupServer.getFailTimeout(), "GroupServer FailTimeout config is null! virtual server " + vsId);
    }

    public static String getUpstreamName(Long groupId) {
        return UpstreamPrefix + groupId;
    }

}
