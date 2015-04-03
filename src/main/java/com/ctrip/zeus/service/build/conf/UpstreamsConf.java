package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.StringFormat;

import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
public class UpstreamsConf {
    public static String generate(Slb slb, VirtualServer vs, List<App> apps, Set<String> allDownServers, Set<String> allDownAppServers) {
        StringBuilder b = new StringBuilder(1024);

        //add upstreams
        for (App app : apps) {
            b.append(buildUpstreamConf(slb, vs, app, buildUpstreamName(slb, vs, app), allDownServers, allDownAppServers));
        }

        return b.toString();
    }

    public static String buildUpstreamName(Slb slb, VirtualServer vs, App app) {
        return "backend_" + vs.getName() + "_" + app.getName();
    }

    public static String buildUpstreamConf(Slb slb, VirtualServer vs, App app, String upstreamName, Set<String> allDownServers, Set<String> allDownAppServers) {
        StringBuilder b = new StringBuilder(1024);

        b.append("upstream ").append(upstreamName).append(" {").append("\n");

        //LBMethod
        b.append(LBConf.generate(slb, vs, app));

        //ToDo:
        //b.append("    ").append("zone " + upstreamName + " 64K").append(";\n");

        for (AppServer as : app.getAppServers()) {
            String ip = as.getIp();
            boolean isDown = allDownServers.contains(ip);
            if (!isDown) {
                isDown = allDownAppServers.contains(slb.getName() + "_" + vs.getName() + "_" + app.getName() + "_" + ip);
            }
            b.append("server ").append(ip + ":" + as.getPort())
                    .append(" weight=").append(as.getWeight())
                    .append(" max_fails=").append(as.getMaxFails())
                    .append(" fail_timeout=").append(as.getFailTimeout())
                    .append(isDown?" down":"")
                    .append(";\n");
        }

        //HealthCheck
        b.append(HealthCheckConf.generate(slb, vs, app));

        b.append("}").append("\n");

        return StringFormat.format(b.toString());
    }

}
