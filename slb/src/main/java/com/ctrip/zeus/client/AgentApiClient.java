package com.ctrip.zeus.client;

import com.ctrip.zeus.model.commit.Commit;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;
import com.ctrip.zeus.service.tools.local.impl.LocalInfoServiceImpl;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.IOUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.eclipse.jetty.servlets.GzipFilter;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ctrip.zeus.auth.util.AuthTokenUtil.getDefaultHeaders;

/**
 * @Discription
 **/
public class AgentApiClient extends AbstractRestClient {

    private static DynamicIntProperty readTimeout = DynamicPropertyFactory.getInstance().getIntProperty("agent.api.client.read.timeout", 10000);


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static AgentApiClient instance = new AgentApiClient(ConfigurationManager.getConfigInstance().getString("slb.host", ""));

    private AgentApiClient(String url) {
        super(url, readTimeout.get());
    }

    public static AgentApiClient getClient() {
        return instance;
    }

    public static AgentApiClient getClient(String baseUrl) {
        return new AgentApiClient(baseUrl);
    }

    public Long getSlbId() {
        String ip = LocalInfoPack.INSTANCE.getIp();
        try {
            String response = getTarget().path("/api/slb")
                    .queryParam("ip", ip)
                    .queryParam("type", "info").request().headers(getDefaultHeaders()).get(String.class);
            Slb slb = ObjectJsonParser.parse(response, Slb.class);
            return slb == null ? null : slb.getId();
        } catch (Exception e) {
            logger.warn("Get SlbId From Api Cluster Failed.Ip:" + ip, e);
            return null;
        }
    }


    public void updateServerVersion(Long version) throws Exception {
        Long slbId = LocalInfoServiceImpl.getLocalSlbIdStatic();
        String ip = LocalInfoPack.INSTANCE.getIp();

        Response response = getTarget().path("/api/model/snapshot/version/set")
                .queryParam("slbId", slbId)
                .queryParam("ip", ip)
                .queryParam("version", version).request().headers(getDefaultHeaders()).get();

        InputStream inputStream = (InputStream) response.getEntity();
        if ("success".equalsIgnoreCase(IOUtils.inputStreamStringify(inputStream))) {
            logger.info("Version update succeed. Ip: " + ip + ", slbId: " + slbId + ", version: " + version);
        } else {
            logger.info("Version update failed. Ip: " + ip + ", slbId: " + slbId + ", version: " + version);
        }
    }

    public ModelSnapshotEntity getModelSnapshotEntity(Long slbVersion) throws Exception {
        WebTarget target = getTarget().register(GZipEncoder.class).register(GzipFilter.class).register(EncodingFilter.class);

        MultivaluedMap<String, Object> map = getDefaultHeaders();
        map.putSingle("Accept-Encoding", "gzip");
        map.putSingle("Accept", "*/*");
        map.putSingle("Content-Type", MediaType.APPLICATION_JSON);
        try {
            String response = target.path("/api/model/snapshot/get").
                    queryParam("ip", LocalInfoPack.INSTANCE.getIp()).
                    queryParam("version", slbVersion).
                    request().headers(map).get(String.class);
            return ObjectJsonParser.parse(response, ModelSnapshotEntity.class);
        } catch (Exception e) {
            logger.error("Parsing ModelSnapshotEntity failed with message: " + e.getMessage(), e);
            return null;
        }
    }

    public ModelSnapshotEntity getModelSnapshotEntity(Long slbVersion, Long serverVersion) throws Exception {
        WebTarget target = getTarget().register(GZipEncoder.class).register(GzipFilter.class).register(EncodingFilter.class);

        MultivaluedMap<String, Object> map = getDefaultHeaders();
        map.putSingle("Accept-Encoding", "gzip");
        map.putSingle("Accept", "*/*");
        map.putSingle("Content-Type", MediaType.APPLICATION_JSON);
        try {
            String response = target.path("/api/model/snapshot/incremental/get").
                    queryParam("ip", LocalInfoPack.INSTANCE.getIp()).
                    queryParam("slbVersion", slbVersion).
                    queryParam("serverVersion", serverVersion).
                    request().headers(map).get(String.class);
            return ObjectJsonParser.parse(response, ModelSnapshotEntity.class);
        } catch (Exception e) {
            logger.error("Parsing ModelSnapshotEntity failed with message: " + e.getMessage(), e);
            return null;
        }
    }

    /*
     * @Description get slb conf version and slb server conf version from api cluster
     * @return
     **/
    public Map<String, Long> getConfVersions() throws Exception {
        String ip = LocalInfoPack.INSTANCE.getIp();

        Response response = getTarget().path("/api/model/snapshot/version")
                .queryParam("ip", ip)
                .request().headers(getDefaultHeaders()).get();
        InputStream inputStream = (InputStream) response.getEntity();
        return ObjectJsonParser.parse(IOUtils.inputStreamStringify(inputStream), new TypeReference<HashMap<String, Long>>() {
        });
    }

    public List<Commit> queryCommits(Long slbId, Long startVersion, Long endVersion) {
        List<Commit> commits = new ArrayList<>();

        Response response = getTarget().path("/api/model/snapshot/commit/query")
                .queryParam("slbId", slbId)
                .queryParam("fromVersion", startVersion)
                .queryParam("toVersion", endVersion)
                .request().headers(getDefaultHeaders()).get();
        InputStream inputStream = (InputStream) response.getEntity();
        try {
            commits = ObjectJsonParser.parseArray(IOUtils.inputStreamStringify(inputStream), Commit.class);
        } catch (IOException e) {
            logger.warn("Failed to parse commits from api cluster. Message: " + e.getMessage());
        }
        return commits;
    }

    private Long parseGetVersionResponse(Response response) throws Exception {
        InputStream inputStream = (InputStream) response.getEntity();
        Map<String, Long> content = ObjectJsonParser.parse(IOUtils.inputStreamStringify(inputStream), new TypeReference<HashMap<String, Long>>() {
        });

        if (content != null && content.containsKey("version")) {
            return content.get("version");
        }
        throw new Exception("Get slb server version failed");
    }
}
