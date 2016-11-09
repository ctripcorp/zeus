package com.ctrip.zeus.client;

import com.ctrip.zeus.model.entity.SlbValidateResponse;
import com.ctrip.zeus.support.ObjectJsonParser;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by fanqq on 2015/6/26.
 */
public class ValidateClient extends AbstractRestClient {

    protected ValidateClient(String url) {
        super(url);
    }

    private static LoadingCache<String, ValidateClient> cache = CacheBuilder.newBuilder().maximumSize(10)
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

    public SlbValidateResponse slbValidate(Long slbId) throws Exception {
        Response response = getTarget().path("/api/validate/slb").queryParam("slbId", slbId)
                .request().headers(getDefaultHeaders()).get();
        InputStream in = (InputStream) response.getEntity();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] bytes = new byte[256];
        int i;
        while ((i = in.read(bytes)) != -1) {
            bs.write(bytes, 0, i);
        }
        if (response.getStatus() / 100 == 2) {
            return ObjectJsonParser.parse(bs.toString(), SlbValidateResponse.class);
        } else {
            SlbValidateResponse res = new SlbValidateResponse();
            res.setSlbId(slbId)
                    .setSucceed(false)
                    .setMsg(bs.toString());
            return res;
        }
    }
}
