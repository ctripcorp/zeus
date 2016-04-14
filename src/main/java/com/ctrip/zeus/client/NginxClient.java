package com.ctrip.zeus.client;

import com.ctrip.zeus.exceptions.NginxTimeoutException;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatusList;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import com.ctrip.zeus.util.IOUtils;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public class NginxClient extends AbstractRestClient {
    private static LoadingCache<String, NginxClient> cache = CacheBuilder.newBuilder().maximumSize(20)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, NginxClient>() {
                       @Override
                       public NginxClient load(String url) throws Exception {
                           return new NginxClient(url);
                       }
                   }
            );
    private String url;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static NginxClient getClient(String url) throws ExecutionException {
        return cache.get(url);
    }

    public NginxClient(String url) {
        super(url);
        this.url = url;
    }

    public TrafficStatusList getTrafficStatus(Long since, int count) throws Exception {
        Response response = getTarget().path("").path("/api/nginx/trafficStatus")
                .queryParam("since", since).queryParam("count", count).request().headers(getDefaultHeaders()).get();
        InputStream is = (InputStream) response.getEntity();
        try {
            return DefaultJsonParser.parse(TrafficStatusList.class, IOUtils.inputStreamStringify(is));
        } catch (Exception ex) {
            throw new Exception("Fail to parse traffic status object.");
        }
    }

    public TrafficStatusList getTrafficStatusByGroup(Long since, String groupName, int count) throws Exception {
        Response response = getTarget().path("/api/nginx/trafficStatus/group")
                .queryParam("since", since).queryParam("groupName", groupName).queryParam("count", count).request().headers(getDefaultHeaders()).get();
        InputStream is = (InputStream) response.getEntity();
        try {
            return DefaultJsonParser.parse(TrafficStatusList.class, IOUtils.inputStreamStringify(is));
        } catch (Exception ex) {
            throw new Exception("Fail to parse traffic status object.");
        }
    }

    public NginxResponse update(boolean fresh) {
        String path = "/api/update/conf";
        WebTarget target = getTarget().path("/api/update/conf").queryParam("refresh", fresh).queryParam("reload", false);
        NginxResponse result = new NginxResponse();
        try {
            logger.info("[NginxClient] Call update api: " + url + path + "?refresh=" + fresh + "&reload=false");
            Response response = target.request().headers(getDefaultHeaders()).get();
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                result = DefaultJsonParser.parse(NginxResponse.class, response.readEntity(String.class));
            } else {
                result.setSucceed(false);
                result.setErrMsg(response.readEntity(String.class));
                result.setServerIp(url);
            }
        } catch (Exception e) {
            result.setSucceed(false);
            result.setOutMsg(e.getMessage());
            result.setServerIp(url);
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                e = new NginxTimeoutException("Update conf timeout.", e.getCause());
            }
            logger.error("[NginxClient] Update api execute failed: " + url + path + "?refresh=" + fresh + "&reload=false", e);
        } finally {
            logger.info("[NginxClient] Update api get succeeded.");
        }
        return result;
    }
}
