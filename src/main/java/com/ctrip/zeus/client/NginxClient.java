package com.ctrip.zeus.client;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;

import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
public class NginxClient extends AbstractRestClient {
    public NginxClient(String url) {
        super(url);
    }

    public NginxResponse load() throws IOException{
        String responseStr = getTarget().path("/api/nginx/load").request().get(String.class);
        return DefaultJsonParser.parse(NginxResponse.class, responseStr);
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
