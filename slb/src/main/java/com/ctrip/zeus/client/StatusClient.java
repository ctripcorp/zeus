package com.ctrip.zeus.client;

import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by fanqq on 2015/5/20.
 */
public class StatusClient extends AbstractRestClient {
    private static LoadingCache<String,StatusClient> cache = CacheBuilder.newBuilder().maximumSize(20)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, StatusClient>() {
                       @Override
                       public StatusClient load(String url) throws Exception {
                           return new StatusClient(url);
                       }
                   }
            );

    protected StatusClient(String url) {
        super(url);
    }

    public static StatusClient getClient(String url) throws ExecutionException {
        return cache.get(url);
    }

}
