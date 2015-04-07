package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.util.StringFormat;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class NginxConf {
    private static DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);
    private static final int DEFAULT_WORKERS = 4;

    public static String generate(Slb slb) {
        StringBuilder b = new StringBuilder(1024);

        Integer worker = slb.getNginxWorkerProcesses();

        b.append("worker_processes ").append((worker==null||worker==0)?DEFAULT_WORKERS:worker).append(";\n");

        b.append("events {").append("\n");
        b.append("worker_connections 1024000;\n");
//        b.append("use epoll; \n");//要求linux 内核 2.6+
        b.append("}").append("\n");

        b.append("http {").append("\n");
        b.append("include    mime.types").append(";\n");
        b.append("default_type    application/octet-stream").append(";\n");
        b.append("keepalive_timeout    65").append(";\n");
        b.append(statusConf());
        b.append("include    upstreams/*.conf").append(";\n");
        b.append("include    vhosts/*.conf").append(";\n");
        b.append("}").append("\n");

        return StringFormat.format(b.toString());
    }

    //slb check health
    private static String statusConf()
    {
        StringBuilder b = new StringBuilder(128);
        b.append("server {").append("\n");
        b.append("listen    ").append(String.valueOf(nginxStatusPort.get())).append(";\n");


        b.append("location / {").append("\n");
        b.append("add_header Access-Control-Allow-Origin *").append(";\n");
        b.append("check_status").append(";\n");
        b.append("}").append("\n");


        b.append("location =/status.json {").append("\n");
        b.append("add_header Access-Control-Allow-Origin *").append(";\n");
        b.append("check_status json").append(";\n");
        b.append("}").append("\n");

        b.append("}").append("\n");

        return b.toString();

    }
}
