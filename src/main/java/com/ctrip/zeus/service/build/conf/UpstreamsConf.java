package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.StringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
public class UpstreamsConf {
    public static String generate(Slb slb, VirtualServer vs, List<Group> groups, Set<String> allDownServers, Set<String> allUpGroupServers)throws Exception {
        StringBuilder b = new StringBuilder(10240);

        //add upstreams
        for (Group group : groups) {
            b.append(buildUpstreamConf(slb, vs, group, buildUpstreamName(slb, vs, group), allDownServers, allUpGroupServers));
        }

        return b.toString();
    }

    public static String buildUpstreamName(Slb slb, VirtualServer vs, Group group) throws Exception{
        AssertUtils.assertNotNull(vs.getId(), "virtual server name is null!");
        AssertUtils.assertNotNull(group.getName(), "app name is null!");
        return "backend_" + group.getName();
    }

    public static String buildUpstreamConf(Slb slb, VirtualServer vs, Group group, String upstreamName, Set<String> allDownServers, Set<String> allUpGroupServers) throws Exception {
        StringBuilder b = new StringBuilder(1024);
        String body = buildUpstreamConfBody(slb,vs,group,allDownServers,allUpGroupServers);
        if (null == body){
            return "";
        }
        b.append("upstream ").append(upstreamName).append(" {").append("\n");
        b.append(body);

        b.append("}").append("\n");

        return StringFormat.format(b.toString());
    }

    public static String buildUpstreamConfBody(Slb slb, VirtualServer vs, Group group, Set<String> allDownServers, Set<String> allUpGroupServers) throws Exception {
        StringBuilder b = new StringBuilder(1024);
        //LBMethod
        b.append(LBConf.generate(slb, vs, group));

        List<GroupServer> groupServers= group.getGroupServers();

        if (groupServers==null||groupServers.size()==0)
        {
//            groupServers = new ArrayList<>();
            return null;
        }

        for (GroupServer as : groupServers) {
            String ip = as.getIp();
            boolean isDown = allDownServers.contains(ip);
            if (!isDown) {
                isDown = !allUpGroupServers.contains(slb.getId() + "_" + vs.getId() + "_" + group.getId() + "_" + ip);
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

        //HealthCheck
        b.append(HealthCheckConf.generate(slb, vs, group));

        return b.toString();
    }

}
