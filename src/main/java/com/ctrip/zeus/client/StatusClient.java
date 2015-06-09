package com.ctrip.zeus.client;

import com.ctrip.zeus.model.entity.GroupServerStatus;
import com.ctrip.zeus.model.entity.GroupStatus;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import javax.ws.rs.core.Response;
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

    public GroupServerStatus getGroupServerStatus(Long groupId , Long slbId , String sip)throws Exception
    {
        String responseStr = getTarget().path("/api/status/group/"+groupId+"/slb/"+slbId+"/server/"+sip)
                .request().headers(getDefaultHeaders()).get(String.class);
        return DefaultJsonParser.parse(GroupServerStatus.class, responseStr);
    }

    public GroupStatus getGroupStatus(Long groupId , Long slbId)throws Exception
    {
//        String res = getTarget().path("/api/status/group").queryParam("groupId",groupId).queryParam("slbId", slbId)
//                .request().get(String.class);
        String responseStr = getTarget().path("/api/status/group").queryParam("groupId",groupId).queryParam("slbId", slbId)
                .request().headers(getDefaultHeaders()).get(String.class);
        return DefaultJsonParser.parse(GroupStatus.class, responseStr);
    }
}
