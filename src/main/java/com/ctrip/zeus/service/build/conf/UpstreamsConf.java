package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.StringFormat;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
public class UpstreamsConf {
    private static DynamicStringProperty upstreamKeepAlive = DynamicPropertyFactory.getInstance().getStringProperty("upstream.keep-alive", null);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicIntProperty upstreamKeepAliveDefault = DynamicPropertyFactory.getInstance().getIntProperty("upstream.keep-alive.default", 100);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicIntProperty upstreamKeepAliveTimeout = DynamicPropertyFactory.getInstance().getIntProperty("upstream.keep-alive.timeout", 110);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicStringProperty upstreamKeepAliveTimeoutList = DynamicPropertyFactory.getInstance().getStringProperty("upstream.keep-alive.timeout.whitelist", null);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicBooleanProperty upstreamKeepAliveTimeoutEnableAll = DynamicPropertyFactory.getInstance().getBooleanProperty("upstream.keep-alive.timeout.enableAll", false);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");

    public static List<ConfFile> generate(Slb slb, VirtualServer vs, List<Group> groups,
                                          Set<String> downServers, Set<String> upServers,
                                          Set<String> visited) throws Exception {
        List<ConfFile> result = new ArrayList<>();
        Map<String, StringBuilder> map = new HashMap<>();
        for (Group group : groups) {
            String confName = confName(group);
            if (visited.contains(confName)) continue;

            StringBuilder confBuilder = map.get(confName);
            String groupUpstream = buildUpstreamConf(slb, vs, group, buildUpstreamName(slb, vs, group), downServers, upServers);
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

    private static String confName(Group group) throws ValidationException {
        if (group == null || group.getGroupVirtualServers().size() == 0)
            throw new ValidationException("Invalid group information is found when generating upstream conf file. Group is either null or does not have related vs.");
        if (group.getGroupVirtualServers().size() == 1) {
            return "" + group.getGroupVirtualServers().get(0).getVirtualServer().getId();
        }
        long[] vsIds = new long[group.getGroupVirtualServers().size()];
        for (int i = 0; i < group.getGroupVirtualServers().size(); i++) {
            vsIds[i] = group.getGroupVirtualServers().get(i).getVirtualServer().getId();
        }
        Arrays.sort(vsIds);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(vsIds[0]);
        for (int i = 1; i < vsIds.length; i++) {
            stringBuilder.append("_").append(vsIds[i]);
        }
        return stringBuilder.toString();
    }

    public static String buildUpstreamName(Slb slb, VirtualServer vs, Group group) throws Exception{
        AssertUtils.assertNotNull(vs.getId(), "virtual server id is null!");
        AssertUtils.assertNotNull(group.getId(), "groupId not found!");
        return "backend_" + group.getId();
    }

    public static String buildUpstreamConf(Slb slb, VirtualServer vs, Group group, String upstreamName, Set<String> allDownServers, Set<String> allUpGroupServers) throws Exception {
        if (group.isVirtual()){
            return "";
        }
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
        b.append(HealthCheckConf.generate(slb, vs, group));

        return b.toString();
    }

    private static void addKeepAliveTimeoutSetting(StringBuilder b, Long groupId) {
        if (upstreamKeepAliveTimeoutEnableAll.get()){
            b.append("keepalive_timeout ").append(upstreamKeepAliveTimeout.get()).append("s;\n");
            return;
        }
        if (upstreamKeepAliveTimeoutList.get() == null){
            return;
        }
        String[] whiteList = upstreamKeepAliveTimeoutList.get().split(";");
        for (String w : whiteList){
            if (w.trim().equals(String.valueOf(groupId))){
                b.append("keepalive_timeout ").append(upstreamKeepAliveTimeout.get()).append("s;\n");
                return;
            }
        }
    }

    private static void addKeepAliveSetting(StringBuilder b, Long gid) {
        String tmp = upstreamKeepAlive.get();
        if (tmp==null||tmp.trim().equals("")){
            return;
        }else if (tmp.equals("All")){
            b.append("keepalive ").append(upstreamKeepAliveDefault.get()).append(";\n");
        }else {
            String[] pairs = tmp.split(";");
            for (String pair : pairs){
                String[] t = pair.split("=");
                if (t.length==2&&t[0].trim().equals(String.valueOf(gid))){
                    b.append("keepalive ").append(t[1]).append(";\n");
                }
            }
        }
    }

}
