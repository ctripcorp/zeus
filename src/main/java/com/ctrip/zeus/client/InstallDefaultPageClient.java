package com.ctrip.zeus.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

import javax.ws.rs.client.WebTarget;
import java.util.concurrent.TimeUnit;

/**
 * Created by fanqq on 2016/8/22.
 */
public class InstallDefaultPageClient extends AbstractRestClient {

    private static LoadingCache<String, InstallDefaultPageClient> cache = CacheBuilder.newBuilder().maximumSize(10)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, InstallDefaultPageClient>() {
                       @Override
                       public InstallDefaultPageClient load(String url) throws Exception {
                           return new InstallDefaultPageClient(url);
                       }
                   }
            );

    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    protected InstallDefaultPageClient(String url) {
        super(url);
    }

    protected InstallDefaultPageClient(String url, int readTimeout) {
        super(url, readTimeout);
    }

    public static InstallDefaultPageClient getClient(String url) throws Exception {
        return cache.get(url);
    }

    public static InstallDefaultPageClient getClientByServerIp(String serverIp) throws Exception {
        return getClient("http://" + serverIp + ":" + adminServerPort.get());
    }

    public String errorPage(String code , Long version ) throws Exception {
        WebTarget target = getTarget().path("/api/errorPage/install/local");
        target = target.queryParam("code",code);
        target = target.queryParam("version",version);
        String responseStr = target.request().headers(getDefaultHeaders()).get(String.class);
        return responseStr;
    }
     public String indexPage(Long version ) throws Exception {
        WebTarget target = getTarget().path("/api/indexPage/install/local");
        target = target.queryParam("version",version);
        String responseStr = target.request().headers(getDefaultHeaders()).get(String.class);
        return responseStr;
    }
}
