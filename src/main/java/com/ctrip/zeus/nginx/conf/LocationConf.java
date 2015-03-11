package com.ctrip.zeus.nginx.conf;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class LocationConf {
    public static String generate(Slb slb, VirtualServer vs, App app, String upstreamName) {
        StringBuilder b = new StringBuilder(1024);

        b.append("    ").append("location ").append(getPath(slb, vs, app)).append("{").append("\n");

        b.append("    ").append("    check_status").append(";\n");
        b.append("    ").append("    proxy_pass http://").append(upstreamName).append(";\n");

        //ToDo:health_check
        //b.append("    ").append("    ").append(HealthCheckConf.generate(slb,vs,app)).append(";\n");

        b.append("    ").append("}").append("\n");

        return b.toString();
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
