package com.ctrip.zeus.client;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public class NginxClient extends AbstractRestClient {
    public NginxClient(String url) {
        super(url);
    }

    public void load(){
        getTarget().path("/api/nginx/load").request().get(String.class);
    }
}
