package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.util.StringFormat;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class NginxConf {
    private static DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);
    private static final String LINEBREAK = "\n";
    private static String ZONENAME = "proxy_zone";
    private static DynamicIntProperty serverNamesHashMaxSize = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.serverNames-maxSize", 10000);
    private static DynamicIntProperty serverNamesHashBucketSize = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.serverNames-bucketSize", 128);
    private static DynamicIntProperty checkShmSize = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.checkShmSize", 32);
    private static DynamicStringProperty logLevel = DynamicPropertyFactory.getInstance().getStringProperty("slb.nginx.logLevel", "");

    private static DynamicStringProperty logFormat = DynamicPropertyFactory.getInstance().getStringProperty("slb.nginx.log-format",
            "log_format main '[$time_local] $host $hostname $server_addr $request_method $uri '\n" +
                    "'\"$query_string\" $server_port $remote_user $remote_addr $http_x_forwarded_for '\n" +
                    "'$server_protocol \"$http_user_agent\" \"$cookie_COOKIE\" \"$http_referer\" '\n" +
                    "'$host $status $body_bytes_sent $request_time $upstream_response_time '\n" +
                    "'$upstream_addr $upstream_status';\n"
    );


    private static DynamicIntProperty dyupsPort = DynamicPropertyFactory.getInstance().getIntProperty("dyups.port", 8081);

    public static String generate(Slb slb) {
        StringBuilder b = new StringBuilder(1024);

//        Integer worker = slb.getNginxWorkerProcesses();

//        b.append("worker_processes ").append((worker==null||worker==0)?DEFAULT_WORKERS:worker).append(";\n");
        b.append("worker_processes auto;\n")
         .append("user nobody;\n")
         .append("error_log /opt/logs/nginx/error.log").append(logLevel.get()).append(";\n")
         .append("worker_rlimit_nofile 65535;\n")
         .append("pid logs/nginx.pid;\n");


        b.append("events {\n")
         .append("worker_connections 30720;\n")
         .append("multi_accept on;\n")
         .append("use epoll; \n")
         .append("}\n");

        b.append("http {\n");
        b.append("include    mime.types;\n");
        b.append("default_type    application/octet-stream;\n");
        b.append("keepalive_timeout    65;\n");

        b.append(logFormat.get());
        b.append("access_log /opt/logs/nginx/access.log main;\n");
        b.append("server_names_hash_max_size ").append(serverNamesHashMaxSize.get()).append(";\n");
        b.append("server_names_hash_bucket_size ").append(serverNamesHashBucketSize.get()).append(";\n");
        b.append("check_shm_size ").append(checkShmSize.get()).append("M;\n");
        b.append("client_max_body_size 2m;\n");

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

        appendServerCommand(b);
        appendServerConf(b);

        b.append("}").append("\n");

        return b.toString();

    }
    //updown stream
    private static String dyupstreamConf()
    {
        int port = dyupsPort.getValue();
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
    public static void appendDefaultServer(StringBuilder builder)
    {
        builder.append("server {").append("\n")
                .append("listen  80 default_server  ;\n")
//                .append("listen  443 default_server  ;\n")
                .append("location = /domaininfo/OnService.html {\n")
                .append("add_header Content-Type text/html;\n")
                .append("return 200 \"4008206666\";\n")
                .append("}\n")
                .append("location / {\n")
                .append("return 404;\n")
                .append("}\n")
                .append("}\n")
                ;


    }
}
