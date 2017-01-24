package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.util.AssertUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public String generate(Slb slb, VirtualServer vs, List<TrafficPolicy> policies, List<Group> groups,
                           Map<String, Object> objectOnVsReferrer) throws Exception {
        Long slbId = slb.getId();
        Long vsId = vs.getId();

        ConfWriter confWriter = new ConfWriter(1024, true);
        try {
            Integer.parseInt(vs.getPort());
        } catch (Exception e) {
            throw new ValidationException("virtual server [" + vs.getId() + "] port is illegal!");
        }

        confWriter.writeServerStart();
        if (vs.isSsl() && configHandler.getEnable("http.version.2", slbId, null, null, false)) {
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
            confWriter.writeCommand("ssl_prefer_server_ciphers", configHandler.getStringValue("ssl.prefer.server.ciphers", slbId, vsId, null, "on"));
            confWriter.writeCommand("ssl_ciphers", configHandler.getStringValue("ssl.ciphers", slbId, vsId, null, "EECDH+CHACHA20:EECDH+CHACHA20-draft:EECDH+AES128:RSA+AES128:EECDH+AES256:RSA+AES256:EECDH+3DES:RSA+3DES:!MD5"));

            confWriter.writeCommand("ssl_session_cache", configHandler.getStringValue("ssl.session.cache", slbId, vsId, null, "shared:SSL:20m"));
            confWriter.writeCommand("ssl_session_timeout", configHandler.getStringValue("ssl.session.cache.timeout", slbId, vsId, null, "180m"));

        }

        if (configHandler.getEnable("server.vs.health.check", slbId, vsId, null, false)) {
            locationConf.writeHealthCheckLocation(confWriter, slbId, vsId);
        }

        confWriter.writeCommand("req_status", ZONENAME);

        generateLocations(slb, vs, objectOnVsReferrer, policies, groups, vsId, confWriter);

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

    private void generateLocations(Slb slb, VirtualServer vs, Map<String, Object> objectOnVsReferrer, List<TrafficPolicy> policies, List<Group> groups, Long vsId, ConfWriter confWriter) throws Exception {
        //add locations
        Set<Long> namedLocations = new HashSet<>();

        int gIdx = 0;
        int pIdx = 0;
        int _firstGroup = 0, _firstPolicy = 1, _firstGvs = 2, _firstPvs = 3;
        Object[] first = new Object[4];
        while (gIdx < groups.size() && pIdx < policies.size()) {
            Group g;
            TrafficPolicy p;
            GroupVirtualServer gvs;
            PolicyVirtualServer pvs;
            if (first[_firstGroup] == null) {
                g = groups.get(gIdx);
                gvs = (GroupVirtualServer) objectOnVsReferrer.get("gvs-" + groups.get(gIdx).getId());
            } else {
                g = (Group) first[_firstGroup];
                gvs = (GroupVirtualServer) first[_firstGvs];
            }
            if (first[_firstPolicy] == null) {
                p = policies.get(pIdx);
                pvs = (PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + groups.get(pIdx).getId());
            } else {
                p = (TrafficPolicy) first[_firstPolicy];
                pvs = (PolicyVirtualServer) first[_firstPvs];
            }
            if (pvs.getPriority() - gvs.getPriority() >= 0) {
                locationConf.write(confWriter, slb, vs, p, pvs);
                for (TrafficControl c : p.getControls()) {
                    namedLocations.add(c.getGroup().getId());
                }
                pIdx++;
                p = null;
                pvs = null;
            } else {
                locationConf.write(confWriter, slb, vs, g, gvs, namedLocations.contains(g.getId()));
                gIdx++;
                g = null;
                gvs = null;
            }
            first[_firstGroup] = g;
            first[_firstGvs] = gvs;
            first[_firstPolicy] = p;
            first[_firstPvs] = pvs;
        }
        while (pIdx < policies.size()) {
            TrafficPolicy p = policies.get(pIdx);
            PolicyVirtualServer pvs = (PolicyVirtualServer) objectOnVsReferrer.get("pvs-" + groups.get(pIdx).getId());
            locationConf.write(confWriter, slb, vs, p, pvs);
            for (TrafficControl c : p.getControls()) {
                namedLocations.add(c.getGroup().getId());
            }
            pIdx++;
        }
        while (gIdx < groups.size()) {
            Group g = groups.get(gIdx);
            GroupVirtualServer gvs = (GroupVirtualServer) objectOnVsReferrer.get("gvs-" + groups.get(gIdx).getId());
            locationConf.write(confWriter, slb, vs, g, gvs, namedLocations.contains(g.getId()));
            gIdx++;
        }
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

    public void writeDefaultServers(ConfWriter confWriter, Long slbId) throws Exception {
        confWriter.writeServerStart();
        confWriter.writeCommand("listen", "*:80 default_server");
        locationConf.writeDefaultLocations(confWriter);
        confWriter.writeServerEnd();

        confWriter.writeServerStart();
        if (configHandler.getEnable("default.server.http.version.2", slbId, null, null, false)) {
            confWriter.writeCommand("listen", "*:443 http2 default_server");
        } else {
            confWriter.writeCommand("listen", "*:443 default_server");
        }
        confWriter.writeCommand("ssl", "on");
        confWriter.writeCommand("ssl_certificate", SSL_PATH + "default/ssl.crt");
        confWriter.writeCommand("ssl_certificate_key", SSL_PATH + "default/ssl.key");
        confWriter.writeCommand("ssl_protocols", getDefaultServerProtocols(slbId));
        locationConf.writeDefaultLocations(confWriter);
        confWriter.writeServerEnd();
    }

    private String getDefaultServerProtocols(Long slbId) throws Exception {
        String result = "TLSv1 TLSv1.1 TLSv1.2";
        if (configHandler.getEnable("default.server.ssl.protocol.sslv2", slbId, null, null, false)) {
            result += " SSLv2";
        }
        if (configHandler.getEnable("default.server.ssl.protocol.sslv3", slbId, null, null, false)) {
            result += " SSLv3";
        }
        return result;
    }
}
