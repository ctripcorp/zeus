package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.*;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class UpstreamConf {
    public static String generate(Slb slb, VirtualServer vs, App app, String upstreamName) {
        StringBuilder b = new StringBuilder(1024);

        b.append("    ").append("upstream ").append(upstreamName).append(" {").append("\n");
        b.append("    ").append("    ").append(LBConf.generate(slb, vs, app)).append(";\n");
        b.append("    ").append("    ").append("zone " + upstreamName + " 64K").append(";\n");

        for (AppServer as : app.getAppServers()) {
            Server s = as.getServer();
            b.append("    ").append("    server ").append(s.getIp() + ":" + vs.getPort())
                    .append(" weight=").append(as.getWeight())
                    .append(" max_fails=").append(as.getMaxFails())
                    .append(" fail_timeout=").append(as.getFailTimeout())
                    .append(";\n");
        }

        b.append("    ").append("}").append("\n");

        return b.toString();
    }
}
