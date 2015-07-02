package com.ctrip.zeus.client;

import com.ctrip.zeus.model.entity.SlbValidateResponse;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by fanqq on 2015/6/26.
 */
public class ValidateClient extends AbstractRestClient {

    protected ValidateClient(String url) {
        super(url);
    }

    private static LoadingCache<String,ValidateClient> cache = CacheBuilder.newBuilder().maximumSize(10)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, ValidateClient>() {
                       @Override
                       public ValidateClient load(String url) throws Exception {
                           return new ValidateClient(url);
                       }
                   }
            );

    public static ValidateClient getClient(String url) throws ExecutionException {
        return cache.get(url);
    }

    public SlbValidateResponse slbValidate(Long slbId)throws Exception{
        String responseStr = getTarget().path("/api/validate/slb").queryParam("slbId", slbId)
                .request().headers(getDefaultHeaders()).get(String.class);
        try{
            return DefaultJsonParser.parse(SlbValidateResponse.class,responseStr);
        }catch (Exception e )
        {
            SlbValidateResponse response = new SlbValidateResponse();
            return response.setSucceed(false).setMsg(responseStr);
        }
    }
}
