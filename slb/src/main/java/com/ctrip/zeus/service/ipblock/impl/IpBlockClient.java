package com.ctrip.zeus.service.ipblock.impl;

import com.ctrip.zeus.client.AbstractRestClient;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class IpBlockClient extends AbstractRestClient {

    private static DynamicStringProperty port = DynamicPropertyFactory.getInstance().getStringProperty("server.black.list.server.port", "10003");


    private static LoadingCache<String, IpBlockClient> cache = CacheBuilder.newBuilder().maximumSize(10)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, IpBlockClient>() {
                       @Override
                       public IpBlockClient load(String url) throws Exception {
                           return new IpBlockClient(url);
                       }
                   }
            );

    private IpBlockClient(String url) {
        super(url);
    }

    public static IpBlockClient getClient(String url) throws ExecutionException {
        return cache.get(url);
    }

    public static IpBlockClient getLocalClient() throws ExecutionException {
        String url = "http://127.0.0.1:" + port.get();
        return cache.get(url);
    }

    public Boolean pushIpList(String post) {
        try {
            this.getTarget().path("/api/ip/blacklist/localUpdate").request().header("Content-Type", MediaType.APPLICATION_JSON)
                    .post(Entity.entity(post, MediaType.APPLICATION_JSON), String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void pushIpListToNginx(String post) {
        this.getTarget().path("/addIpList").request().header("Content-Type", MediaType.APPLICATION_JSON)
                .post(Entity.entity(post, MediaType.APPLICATION_JSON), String.class);
    }
}
