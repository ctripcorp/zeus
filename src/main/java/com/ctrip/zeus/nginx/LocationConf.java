package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class LocationConf {
    public static String generate(Slb slb, VirtualServer vs, App app) {
        String upstreamName = getUpstreamName(slb, vs, app);

        StringBuilder b = new StringBuilder(1024);
        b.append(UpstreamConf.generate(slb, vs, app, upstreamName));

        b.append("    ").append("location ").append(getPath(slb, vs, app)).append("{").append("\n");
        b.append("    ").append("    proxy_pass http://").append(upstreamName).append(";\n");
        b.append("    ").append("    ").append(HealthCheckConf.generate(slb,vs,app)).append(";\n");
        b.append("    ").append("}").append("\n");

        return b.toString();
    }

    private static String getUpstreamName(Slb slb, VirtualServer vs, App app) {
        return "backend_" + vs.getName() + "_" + app.getName();
    }

    private static String getPath(Slb slb, VirtualServer vs, App app) {
        for (AppSlb appSlb : app.getAppSlbs()) {
            if (slb.getName().equals(appSlb.getSlbName()) && vs.getName().equals(appSlb.getVirtualServer().getName())) {
                return appSlb.getPath();
            }
        }

        throw new RuntimeException("IllegalState");
    }
}
