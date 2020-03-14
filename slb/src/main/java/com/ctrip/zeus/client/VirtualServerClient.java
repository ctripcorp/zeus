package com.ctrip.zeus.client;

import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.support.ObjectJsonParser;

import javax.ws.rs.core.MediaType;

import static com.ctrip.zeus.auth.util.AuthTokenUtil.getDefaultHeaders;

/**
 * Created by lu.wang on 2016/4/15.
 */
public class VirtualServerClient extends AbstractRestClient {
    public VirtualServerClient(String url) {
        super(url);
    }

    public VirtualServer get(String vsId) {
        String res = getTarget().path("/api/vs").queryParam("vsId", vsId).request(MediaType.APPLICATION_JSON)
                .headers(getDefaultHeaders()).get(String.class);
        return ObjectJsonParser.parse(res, VirtualServer.class);
    }
}
