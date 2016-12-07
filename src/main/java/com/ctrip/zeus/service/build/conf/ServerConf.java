package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.util.AssertUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("serverConf")
public class ServerConf {
    @Resource
    ConfigHandler configHandler;
    @Resource
    LocationConf locationConf;

    public static final String SSL_PATH = "/data/nginx/ssl/";
    private static final String ZONENAME = "proxy_zone";

    public String generate(Slb slb, VirtualServer vs, List<Group> groups) throws Exception {
        Long slbId = slb.getId();
        Long vsId = vs.getId();

        ConfWriter confWriter = new ConfWriter(1024, true);
        try {
            Integer.parseInt(vs.getPort());
        } catch (Exception e) {
            throw new ValidationException("virtual server [" + vs.getId() + "] port is illegal!");
        }

        confWriter.writeServerStart();
        if (vs.isSsl() && configHandler.getEnable("http.version.2", null, null, null, false)) {
            writeHttp2Configs(confWriter, slbId, vs);
        } else {
            confWriter.writeCommand("listen", vs.getPort());
        }
        confWriter.writeCommand("server_name", getServerNames(vs));
        confWriter.writeCommand("ignore_invalid_headers", "off");
        confWriter.writeCommand("proxy_http_version", "1.1");

        if (configHandler.getEnable("server.proxy.buffer.size", slbId, vsId, null, false)) {
            confWriter.writeCommand("proxy_buffer_size", configHandler.getStringValue("server.proxy.buffer.size", slbId, vsId, null, "8k"));
            confWriter.writeCommand("proxy_buffers", configHandler.getStringValue("server.proxy.buffers", slbId, vsId, null, "8 8k"));
            confWriter.writeCommand("proxy_busy_buffers_size", configHandler.getStringValue("server.proxy.busy.buffers.size", slbId, vsId, null, "8k"));
        }

        if (configHandler.getEnable("large.client.header.buffers", slbId, vsId, null, false)) {
            confWriter.writeCommand("large_client_header_buffers", configHandler.getStringValue("large.client.header.buffers", slbId, vsId, null, "4 8k"));
        }

        if (vs.isSsl()) {
            confWriter.writeCommand("ssl", "on");
            confWriter.writeCommand("ssl_certificate", SSL_PATH + vsId + "/ssl.crt");
            confWriter.writeCommand("ssl_certificate_key", SSL_PATH + vsId + "/ssl.key");
            confWriter.writeCommand("ssl_protocols", getProtocols(slbId, vsId));
        }

        if (configHandler.getEnable("server.vs.health.check", slbId, vsId, null, false)) {
            locationConf.writeHealthCheckLocation(confWriter, slbId, vsId);
        }

        confWriter.writeCommand("req_status", ZONENAME);

        //add locations
        for (Group group : groups) {
            locationConf.write(confWriter, slb, vs, group);
        }

        if (configHandler.getEnable("server.errorPage", slbId, vsId, null, false)) {
            boolean useNew = configHandler.getEnable("server.errorPage.use.new", slbId, vsId, null, true);
            for (int sc = 400; sc <= 425; sc++) {
                locationConf.writeErrorPageLocation(confWriter, useNew, sc, slbId, vsId);
            }
            for (int sc = 500; sc <= 510; sc++) {
                locationConf.writeErrorPageLocation(confWriter, useNew, sc, slbId, vsId);
            }
        }

        confWriter.writeServerEnd();
        return confWriter.getValue();
    }

    private void writeHttp2Configs(ConfWriter confWriter, Long slbId, VirtualServer vs) throws Exception {
        confWriter.writeCommand("listen", vs.getPort() + " http2");
        String config = configHandler.getStringValue("http2.http2_chunk_size", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_chunk_size", config);
        }
        config = configHandler.getStringValue("http2.http2_body_preread_size", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_body_preread_size", config);
        }
        config = configHandler.getStringValue("http2.http2_idle_timeout", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_idle_timeout", config);
        }
        config = configHandler.getStringValue("http2.http2_max_concurrent_streams", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_max_concurrent_streams", config);
        }
        config = configHandler.getStringValue("http2.http2_max_field_size", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_max_field_size", config);
        }
        config = configHandler.getStringValue("http2.http2_max_header_size", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_max_header_size", config);
        }
        config = configHandler.getStringValue("http2.http2_max_requests", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_max_requests", config);
        }
        config = configHandler.getStringValue("http2.http2_recv_buffer_size", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_recv_buffer_size", config);
        }
        config = configHandler.getStringValue("http2.http2_recv_timeout", slbId, vs.getId(), null, null);
        if (config != null) {
            confWriter.writeCommand("http2_recv_timeout", config);
        }
    }

    private String getProtocols(Long slbId, Long vsId) throws Exception {
        String result = "TLSv1 TLSv1.1 TLSv1.2";
        if (configHandler.getEnable("ssl.protocol.sslv2", slbId, vsId, null, false)) {
            result += " SSLv2";
        }
        if (configHandler.getEnable("ssl.protocol.sslv3", slbId, vsId, null, false)) {
            result += " SSLv3";
        }
        return result;
    }


    private String getServerNames(VirtualServer vs) throws Exception {
        StringBuilder b = new StringBuilder(128);
        for (Domain domain : vs.getDomains()) {
            b.append(" ").append(domain.getName());
        }
        String res = b.toString();
        AssertUtils.assertNotEquals("", res.trim(), "virtual server [" + vs.getId() + "] domain is null or illegal!");
        return res;
    }

    public void writeDyupsServer(ConfWriter confWriter, Long slbId) throws Exception {
        confWriter.writeCommand("dyups_upstream_conf", "conf/dyupstream.conf");
        confWriter.writeServerStart();
        confWriter.writeCommand("listen", configHandler.getStringValue("server.dyups.port", slbId, null, null, "8081"));

        locationConf.writeDyupsLocation(confWriter);

        confWriter.writeServerEnd();
    }

    public void writeCheckStatusServer(ConfWriter confWriter, String shmZoneName, Long slbId) throws Exception {
        confWriter.writeServerStart();
        confWriter.writeCommand("listen", configHandler.getStringValue("server.status.port", slbId, null, null, "10001"));
        confWriter.writeCommand("req_status", shmZoneName);
        locationConf.writeCheckStatusLocations(confWriter);
        confWriter.writeServerEnd();
    }

    public void writeDefaultServers(ConfWriter confWriter) {
        confWriter.writeServerStart();
        confWriter.writeCommand("listen", "*:80 default_server");
        locationConf.writeDefaultLocations(confWriter);
        confWriter.writeServerEnd();

        confWriter.writeServerStart();
        confWriter.writeCommand("listen", "*:443 default_server");
        confWriter.writeCommand("ssl", "on");
        confWriter.writeCommand("ssl_certificate", SSL_PATH + "default/ssl.crt");
        confWriter.writeCommand("ssl_certificate_key", SSL_PATH + "default/ssl.key");
        locationConf.writeDefaultLocations(confWriter);
        confWriter.writeServerEnd();
    }
}
