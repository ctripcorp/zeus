package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.Slb;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class SlbConfGenerator {

    public String generate(Slb slb) {
        StringBuilder builder = new StringBuilder(1024);

        builder.append("worker_processes ").append(slb.getNginxWorkerProcesses()).append(";\n");

        builder.append("events {").append("\n");
        builder.append("    worker_connections 1024;").append("\n");
        builder.append("}").append("\n");

        builder.append("http {").append("\n");
        builder.append("    include    mime.types;").append("\n");
        builder.append("    default_type    application/octet-stream;").append("\n");
        builder.append("    keepalive_timeout    65;").append("\n");
        builder.append("    include    vhosts/*.conf;").append("\n");
        builder.append("}").append("\n");

        return builder.toString();
    }
}
