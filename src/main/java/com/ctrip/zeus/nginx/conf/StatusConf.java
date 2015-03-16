package com.ctrip.zeus.nginx.conf;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

/**
 * @author:xingchaowang
 * @date: 3/11/2015.
 */
public class StatusConf {
    private static DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);


    public static String generate() {
        StringBuilder b = new StringBuilder(128);
        b.append("server {").append("\n");
        b.append("    listen    ").append(String.valueOf(nginxStatusPort.get())).append(";\n");

        b.append("    location / {").append("\n");
        b.append("        add_header Access-Control-Allow-Origin *").append(";\n");
        b.append("        check_status").append(";\n");
        b.append("    }").append("\n");

        b.append("    location =/status.json {").append("\n");
        b.append("        add_header Access-Control-Allow-Origin *").append(";\n");
        b.append("        check_status json").append(";\n");
        b.append("    }").append("\n");

        b.append("}").append("\n");



        return b.toString();
    }
}
