package com.ctrip.zeus.service.build.conf;

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

        b.append("location ").append(getPath(slb, vs, app)).append("{\n");


        b.append("proxy_set_header Host $host").append(";\n");
        b.append("proxy_pass http://").append(upstreamName).append(";\n");
//        b.append("proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
//        b.append("proxy_set_header X-Real-IP $remote_addr;");

        b.append("}").append("\n");

        return b.toString();
    }

    private static String getPath(Slb slb, VirtualServer vs, App app) {
        for (AppSlb appSlb : app.getAppSlbs()) {
            if (slb.getName().equals(appSlb.getSlbName()) && vs.getName().equals(appSlb.getVirtualServer().getName())) {
                return appSlb.getPath();
            }
        }
        //ToDo:
        throw new RuntimeException("IllegalState");
    }
}
