package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.Slb;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class NginxConf {

    public static String generate(Slb slb) {
        StringBuilder b = new StringBuilder(1024);

        b.append("worker_processes ").append(slb.getNginxWorkerProcesses()).append(";\n");

        b.append("events {").append("\n");
        b.append("    worker_connections 1024").append(";\n");
        b.append("}").append("\n");

        b.append("http {").append("\n");
        b.append("    include    mime.types").append(";\n");
        b.append("    default_type    application/octet-stream").append(";\n");
        b.append("    keepalive_timeout    65").append(";\n");
        b.append("    include    vhosts/*.conf").append(";\n");
        b.append("}").append("\n");

        return b.toString();
    }
}
