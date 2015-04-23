package com.ctrip.zeus.client;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public class NginxClient extends AbstractRestClient {
    private static LoadingCache<String,NginxClient> cache = CacheBuilder.newBuilder().maximumSize(20)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, NginxClient>() {
                       @Override
                       public NginxClient load(String url) throws Exception {
                           return new NginxClient(url);
                       }
                   }
            );

    public static NginxClient GetClient(String url) throws ExecutionException {
        return cache.get(url);
    }

    public NginxClient(String url) {
        super(url);
    }


    public NginxResponse load() throws IOException{
        String responseStr = getTarget().path("/api/nginx/load").request().get(String.class);
        return DefaultJsonParser.parse(NginxResponse.class, responseStr);
    }

    public NginxResponse write()throws IOException{
        String responseStr = getTarget().path("/api/nginx/write").request().get(String.class);
        return DefaultJsonParser.parse(NginxResponse.class, responseStr);
    }

    public NginxResponse dyups(String upsName ,String upsCommands)throws IOException{
        Response responseStr = getTarget().path("/upstream/"+upsName).request().post(Entity.entity(upsCommands,
                MediaType.APPLICATION_JSON
        ));
        if (responseStr.getStatus()==200)
        {
            return new NginxResponse().setSucceed(true).setOutMsg(responseStr.getEntity().toString());
        }else {
            return new NginxResponse().setSucceed(false).setErrMsg(responseStr.getEntity().toString());
        }
    }

    public UpstreamStatus getUpstreamStatus() throws IOException {
        String result = getTarget().path("/status.json").request().get(String.class);
        System.out.println(result);
        return DefaultJsonParser.parse(UpstreamStatus.class, result);
    }

    public NginxServerStatus getNginxServerStatus() throws IOException {
        //TODO
        return new NginxServerStatus();
    }
}
