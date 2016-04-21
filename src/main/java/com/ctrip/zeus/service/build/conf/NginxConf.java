package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.util.StringFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("nginxConf")
public class NginxConf {

    private static final String LINEBREAK = "\n";
    private static String ZONENAME = "proxy_zone";

    @Resource
    ConfService confService;

    public String generate(Slb slb) throws Exception {
        StringBuilder b = new StringBuilder(1024);
        b.append("worker_processes auto;\n")
                .append("user nobody;\n")
                .append("error_log /opt/logs/nginx/error.log").append(confService.getStringValue("logLevel", null, null, null, "")).append(";\n")
                .append("worker_rlimit_nofile 65535;\n")
                .append("pid logs/nginx.pid;\n");

        b.append("events {\n")
                .append("worker_connections 30720;\n")
                .append("multi_accept on;\n")
                .append("use epoll; \n")
                .append("}\n");

        b.append("http {\n");
        b.append("include   mime.types;\n");
        b.append("default_type    application/octet-stream;\n");
        b.append("keepalive_timeout    65;\n");

        b.append("log_format main " + LogFormat.getMain() + ";\n");
        b.append("access_log /opt/logs/nginx/access.log main;\n");
        b.append("server_names_hash_max_size ").append(confService.getIntValue("serverNames.maxSize", null, null, null, 10000)).append(";\n");
        b.append("server_names_hash_bucket_size ").append(confService.getIntValue("serverNames.bucketSize", null, null, null, 128)).append(";\n");
        b.append("check_shm_size ").append(confService.getIntValue("checkShmSize", null, null, null, 32)).append("M;\n");
        b.append("client_max_body_size 2m;\n");
        b.append("ignore_invalid_headers off;\n");
        
        appendHttpCommand(b);

        b.append(statusConf());
        b.append(dyupstreamConf());
        appendDefaultServer(b);
        b.append("include    upstreams/*.conf;\n");
        b.append("include    vhosts/*.conf;\n");
        b.append("}\n");

        return StringFormat.format(b.toString());
    }

    //slb check health
    private String statusConf() throws Exception {
        StringBuilder b = new StringBuilder(128);
        b.append("server {").append("\n");
        b.append("listen    ").append(confService.getIntValue("status.port", null, null, null, 10001)).append(";\n");


        b.append("location / {").append("\n");
        b.append("add_header Access-Control-Allow-Origin *").append(";\n");
        b.append("check_status").append(";\n");
        b.append("}").append("\n");


        b.append("location =/status.json {").append("\n");
        b.append("add_header Access-Control-Allow-Origin *").append(";\n");
        b.append("check_status json").append(";\n");
        b.append("}").append("\n");

        appendServerCommand(b);
        appendServerConf(b);

        b.append("}").append("\n");

        return b.toString();

    }

    //updown stream
    private String dyupstreamConf() throws Exception {
        int port = confService.getIntValue("dyups.port", null, null, null, 8081);
        StringBuilder b = new StringBuilder(128);

        b.append("dyups_upstream_conf  conf/dyupstream.conf;\n")
                .append("server {\n")
                .append("listen ").append(port).append(";\n")
                .append("location / {\n")
                .append("dyups_interface;\n")
                .append("}\n")
                .append("}\n");
        return b.toString();
    }

    public static void appendHttpCommand(StringBuilder builder) {
        builder.append("req_status_zone " + ZONENAME + " \"$hostname/$proxy_host\" 20M;").append(LINEBREAK);
    }

    public static void appendServerCommand(StringBuilder builder) {
        builder.append("    req_status " + ZONENAME + ";").append(LINEBREAK);
    }

    public static void appendServerConf(StringBuilder builder) {
        builder.append("    location /req_status {").append(LINEBREAK)
                .append("        req_status_show;").append(LINEBREAK)
                .append("    }").append(LINEBREAK);
        builder.append("    location /stub_status {").append(LINEBREAK)
                .append("        stub_status on;").append(LINEBREAK)
                .append("    }").append(LINEBREAK);
    }

    public static void appendDefaultServer(StringBuilder builder) {
        builder.append("server {").append("\n")
                .append("listen *:80 default_server  ;\n");
        appendDefaultLocation(builder);

        builder.append("server {").append("\n")
                .append("listen *:443 default_server;").append("\n")
                .append("ssl on;\n")
                .append("ssl_certificate ").append(ServerConf.SSL_PATH).append("default").append("/ssl.crt;\n")
                .append("ssl_certificate_key ").append(ServerConf.SSL_PATH).append("default").append("/ssl.key;\n");
        appendDefaultLocation(builder);
    }

    private static void appendDefaultLocation(StringBuilder builder) {
        builder.append("location = /domaininfo/OnService.html {\n")
                .append("add_header Content-Type text/html;\n")
                .append("return 200 \"4008206666\";\n")
                .append("}\n")
                .append("location / {\n")
                .append("return 404 \"Not Found!\";\n")
                .append("}\n")
                .append("}\n");
    }
}
