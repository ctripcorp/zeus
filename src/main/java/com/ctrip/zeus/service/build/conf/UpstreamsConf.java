package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.StringFormat;
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
    ConfService confService;

    public List<ConfFile> generate(Set<Long> vsCandidates, VirtualServer vs, List<Group> groups,
                                          Set<String> downServers, Set<String> upServers,
                                          Set<String> visited) throws Exception {
        List<ConfFile> result = new ArrayList<>();
        Map<String, StringBuilder> map = new HashMap<>();
        for (Group group : groups) {
            String confName = confName(vsCandidates, group);
            if (visited.contains(confName)) continue;

            StringBuilder confBuilder = map.get(confName);
            String groupUpstream = buildUpstreamConf(vs, group, buildUpstreamName(vs, group), downServers, upServers);
            if (confBuilder == null) {
                map.put(confName, new StringBuilder().append(groupUpstream));
            } else {
                confBuilder.append('\n').append(groupUpstream);
            }
        }

        for (Map.Entry<String, StringBuilder> e : map.entrySet()) {
            result.add(new ConfFile().setName(e.getKey()).setContent(e.getValue().toString()));
            visited.add(e.getKey());
        }
        return result;
    }

    private String confName(Set<Long> vsCandidates, Group group) throws ValidationException {
        if (group == null || group.getGroupVirtualServers().size() == 0)
            throw new ValidationException("Invalid group information is found when generating upstream conf file. Group is either null or does not have related vs.");
        if (group.getGroupVirtualServers().size() == 1) {
            return "" + group.getGroupVirtualServers().get(0).getVirtualServer().getId();
        }
        List<Long> vsIds = new ArrayList<>();
        for (int i = 0; i < group.getGroupVirtualServers().size(); i++) {
            VirtualServer vs = group.getGroupVirtualServers().get(i).getVirtualServer();
            if (vsCandidates.contains(vs.getId())) {
                vsIds.add(group.getGroupVirtualServers().get(i).getVirtualServer().getId());
            }
        }
        if (vsIds.size() == 0) {
            throw new ValidationException("Fail to generate upstream conf name according to given building vses - " + Joiner.on(",").join(vsCandidates) + ".");
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

    public String buildUpstreamName(VirtualServer vs, Group group) throws Exception{
        AssertUtils.assertNotNull(vs.getId(), "virtual server id is null!");
        AssertUtils.assertNotNull(group.getId(), "groupId not found!");
        return "backend_" + group.getId();
    }

    public String buildUpstreamConf(VirtualServer vs, Group group, String upstreamName, Set<String> allDownServers, Set<String> allUpGroupServers) throws Exception {
        if (group.isVirtual()){
            return "";
        }
        StringBuilder b = new StringBuilder(1024);
        String body = buildUpstreamConfBody(vs,group,allDownServers,allUpGroupServers);
        if (null == body){
            return "";
        }
        b.append("upstream ").append(upstreamName).append(" {").append("\n");
        b.append(body);

        b.append("}").append("\n");

        return StringFormat.format(b.toString());
    }

    public String buildUpstreamConfBody(VirtualServer vs, Group group, Set<String> allDownServers, Set<String> allUpGroupServers) throws Exception {
        StringBuilder b = new StringBuilder(1024);
        //LBMethod
        b.append(LBConf.generate(group));

        List<GroupServer> groupServers = group.getGroupServers();

        if (groupServers==null||groupServers.size() == 0) {
            return null;
        }

        for (GroupServer as : groupServers) {
            String ip = as.getIp();
            boolean isDown = allDownServers.contains(ip);
            if (!isDown) {
                isDown = !allUpGroupServers.contains(vs.getId() + "_" + group.getId() + "_" + ip);
            }

            AssertUtils.assertNotNull(as.getPort(), "GroupServer Port config is null! virtual server " + vs.getId());
            AssertUtils.assertNotNull(as.getWeight(), "GroupServer Weight config is null! virtual server " + vs.getId());
            AssertUtils.assertNotNull(as.getMaxFails(), "GroupServer MaxFails config is null! virtual server " + vs.getId());
            AssertUtils.assertNotNull(as.getFailTimeout(), "GroupServer FailTimeout config is null! virtual server " + vs.getId());

            b.append("server ").append(ip + ":" + as.getPort())
                    .append(" weight=").append(as.getWeight())
                    .append(" max_fails=").append(as.getMaxFails())
                    .append(" fail_timeout=").append(as.getFailTimeout())
                    .append(isDown?" down":"")
                    .append(";\n");
        }
        addKeepAliveSetting(b,group.getId());
        addKeepAliveTimeoutSetting(b, group.getId());

        //HealthCheck
        b.append(HealthCheckConf.generate(vs, group));
        return b.toString();
    }

    private void addKeepAliveTimeoutSetting(StringBuilder b, Long groupId) throws Exception {
        if (confService.getEnable("upstream.keepAlive.timeout", null, null, groupId, false)) {
            b.append("keepalive_timeout ").append(confService.getIntValue("upstream.keepAlive.timeout", null, null, groupId, 110)).append("s;\n");
        }

    }

    private void addKeepAliveSetting(StringBuilder b, Long groupId) throws Exception {
        if (confService.getEnable("upstream.keepAlive", null, null, groupId, false)) {
            b.append("keepalive ").append(confService.getIntValue("upstream.keepAlive", null, null, groupId, 100)).append(";\n");
        }
    }

}
