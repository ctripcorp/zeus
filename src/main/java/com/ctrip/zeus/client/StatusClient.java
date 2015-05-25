package com.ctrip.zeus.client;

import com.ctrip.zeus.model.entity.AppServerStatus;
import com.ctrip.zeus.model.entity.AppStatus;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
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

    public AppServerStatus getAppServerStatus(String appName , String slbName , String sip)throws Exception
    {
        String responseStr = getTarget().path("/api/status/app/"+appName+"/slb/"+slbName+"/server/"+sip)
                .request().headers(getDefaultHeaders()).get(String.class);
        return DefaultJsonParser.parse(AppServerStatus.class, responseStr);
    }

    public AppStatus getAppStatus(String appName , String slbName)throws Exception
    {
        String responseStr = getTarget().path("/api/status/app/"+appName+"/slb/"+slbName)
                .request().headers(getDefaultHeaders()).get(String.class);
        return DefaultJsonParser.parse(AppStatus.class, responseStr);
    }
}
