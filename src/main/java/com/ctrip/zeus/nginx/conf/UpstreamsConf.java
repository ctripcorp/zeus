package com.ctrip.zeus.nginx.conf;

import com.ctrip.zeus.model.entity.*;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
public class UpstreamsConf {
    public static String generate(Slb slb, VirtualServer vs, List<App> apps) {
        StringBuilder b = new StringBuilder(1024);

        //add upstreams
        for (App app : apps) {
            b.append(buildUpstreamConf(slb, vs, app, buildUpstreamName(slb, vs, app)));
        }

        return b.toString();
    }

    public static String buildUpstreamName(Slb slb, VirtualServer vs, App app) {
        return "backend_" + vs.getName() + "_" + app.getName();
    }

    public static String buildUpstreamConf(Slb slb, VirtualServer vs, App app, String upstreamName) {
        StringBuilder b = new StringBuilder(1024);

        b.append("upstream ").append(upstreamName).append(" {").append("\n");

        //LBMethod
        b.append("    ").append(LBConf.generate(slb, vs, app));

        b.append("    ").append("zone " + upstreamName + " 64K").append(";\n");

        for (AppServer as : app.getAppServers()) {
            Server s = as.getServer();
            b.append("    server ").append(s.getIp() + ":" + vs.getPort())
                    .append(" weight=").append(as.getWeight())
                    .append(" max_fails=").append(as.getMaxFails())
                    .append(" fail_timeout=").append(as.getFailTimeout())
                    .append(";\n");
        }

        b.append("}").append("\n");

        return b.toString();
    }
}
