package com.ctrip.zeus.client;

import com.ctrip.zeus.model.model.SlbValidateResponse;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.google.common.base.Strings;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.ctrip.zeus.auth.util.AuthTokenUtil.getDefaultHeaders;

/**
 * Created by fanqq on 2015/6/26.
 */
public class ValidateClient extends AbstractRestClient {

    protected ValidateClient(String url) {
        super(url);
    }

    private static LoadingCache<String, ValidateClient> cache = CacheBuilder.newBuilder().maximumSize(10)
            .expireAfterAccess(1, TimeUnit.DAYS)
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

    public SlbValidateResponse javaTest() throws Exception {
        Response response = getTarget().path("/api/validate/local/java/test")
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
            res.setSucceed(false)
                    .setMsg(bs.toString());
            return res;
        }
    }

    public SlbValidateResponse javaVersionValidate(Boolean fill, Boolean force) throws Exception {
        WebTarget target = getTarget().path("/api/validate/local/java/version");
        target = target.queryParam("fill", (null != fill && fill));
        target = target.queryParam("force", (null != force && force));

        Response response = target.request().headers(getDefaultHeaders()).get();
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
            res.setSucceed(false)
                    .setMsg(bs.toString());
            return res;
        }
    }

    public Future<Response> nginxVersionValidateAsync(Boolean details) {
        WebTarget target = getTarget().path("/api/validate/local/nginx/version");
        target = target.queryParam("details", (null != details && details));

        return target.request().headers(getDefaultHeaders()).async().get();
    }

    public SlbValidateResponse nginxVersionValidateCallback(ValidateClientResponse validateClientResponse) {
        SlbValidateResponse res;
        try {
            Response response = validateClientResponse.getResponseFuture().get(3000, TimeUnit.MILLISECONDS);
            InputStream in = (InputStream) response.getEntity();
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            byte[] bytes = new byte[256];
            int i;
            while ((i = in.read(bytes)) != -1) {
                bs.write(bytes, 0, i);
            }
            if (response.getStatus() / 100 == 2) {
                res = ObjectJsonParser.parse(bs.toString(), SlbValidateResponse.class);
            } else {
                res = new SlbValidateResponse();
                res.setSucceed(false)
                        .setMsg(bs.toString());
            }
        } catch (Exception exception) {
            res = new SlbValidateResponse();
            res.setSucceed(false)
                    .setMsg(exception.getMessage());
        }
        if (res == null) {
            res = new SlbValidateResponse().setSucceed(false);
        }
        if (Strings.isNullOrEmpty(res.getIp())) {
            res.setIp(validateClientResponse.getIp());
        }
        if (res.getSlbId() == null) {
            res.setSlbId(validateClientResponse.getSlbId());
        }
        return res;
    }

    public static class ValidateClientResponse {

        private Long slbId;

        private String ip;

        private ValidateClient validateClient;

        private Future<Response> responseFuture;

        public Long getSlbId() {
            return slbId;
        }

        public ValidateClientResponse setSlbId(Long slbId) {
            this.slbId = slbId;
            return this;
        }

        public String getIp() {
            return ip;
        }

        public ValidateClientResponse setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public ValidateClient getValidateClient() {
            return validateClient;
        }

        public ValidateClientResponse setValidateClient(ValidateClient validateClient) {
            this.validateClient = validateClient;
            return this;
        }

        public Future<Response> getResponseFuture() {
            return responseFuture;
        }

        public ValidateClientResponse setResponseFuture(Future<Response> responseFuture) {
            this.responseFuture = responseFuture;
            return this;
        }
    }
}
