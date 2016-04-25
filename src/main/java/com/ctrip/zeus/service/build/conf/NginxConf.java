package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.build.ConfService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("nginxConf")
public class NginxConf {

    private static String ShmZoneName = "proxy_zone";
    private Long slbId = null;

    @Resource
    ConfService confService;
    @Resource
    ServerConf serverConf;

    public String generate(Slb slb) throws Exception {
        slbId = slb.getId();

        ConfWriter confWriter = new ConfWriter(10240, true);
        confWriter.writeCommand("worker_processes", "auto");
        confWriter.writeCommand("user", "nobody");
        confWriter.writeCommand("error_log", "/opt/logs/nginx/error.log" + confService.getStringValue("logLevel", slbId, null, null, ""));
        confWriter.writeCommand("worker_rlimit_nofile", "65535");
        confWriter.writeCommand("pid", "logs/nginx.pid");

        confWriter.writeEventsStart();
        confWriter.writeCommand("worker_connections", "30720");
        confWriter.writeCommand("multi_accept", "on");
        confWriter.writeCommand("use", "epoll");
        confWriter.writeEventsEnd();

        confWriter.writeHttpStart();
        confWriter.writeCommand("include", "mime.types");
        confWriter.writeCommand("default_type", "application/octet-stream");
        confWriter.writeCommand("keepalive_timeout", confService.getStringValue("keepAlive.timeout", slbId, null, null, "65"));
        confWriter.writeCommand("log_format", "main " + LogFormat.getMain());
        confWriter.writeCommand("access_log", "/opt/logs/nginx/access.log main");
        confWriter.writeCommand("server_names_hash_max_size", confService.getStringValue("serverNames.maxSize", slbId, null, null, "10000"));
        confWriter.writeCommand("server_names_hash_bucket_size", confService.getStringValue("serverNames.bucketSize", slbId, null, null, "128"));
        confWriter.writeCommand("check_shm_size", confService.getStringValue("checkShmSize", slbId, null, null, "32") + "M");
        confWriter.writeCommand("client_max_body_size", "2m");
        confWriter.writeCommand("ignore_invalid_headers", "off");

        confWriter.writeCommand("req_status_zone", ShmZoneName + " \"$hostname/$proxy_host\" 20M");

        serverConf.writeCheckStatusServer(confWriter, ShmZoneName, slbId);
        serverConf.writeDyupsServer(confWriter, slbId);
        serverConf.writeDefaultServers(confWriter);

        confWriter.writeCommand("include", "upstreams/*.conf");
        confWriter.writeCommand("include", "vhosts/*.conf");
        confWriter.writeHttpEnd();

        return confWriter.getValue();
    }
}
