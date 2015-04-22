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
         .append("error_log /opt/logs/nginx/error.log;\n")
         .append("worker_rlimit_nofile 65535;\n")
         .append("pid logs/nginx.pid;\n");


        b.append("events {\n")
         .append("worker_connections 30720;\n")
         .append("use epoll; \n")
         .append("}\n");

        b.append("http {\n");
        b.append("include    mime.types;\n");
        b.append("default_type    application/octet-stream;\n");
        b.append("keepalive_timeout    65;\n");

        b.append(logFormat.get());
        b.append("access_log /opt/logs/nginx/access.log main;\n");

        b.append(statusConf());
        b.append(dyupstreamConf());

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
}
