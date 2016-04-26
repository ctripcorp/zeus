package com.ctrip.zeus.client;

import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.model.transform.DefaultJsonParser;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

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
        try {
            return DefaultJsonParser.parse(VirtualServer.class, res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
