package com.ctrip.zeus.client;


import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultJsonParser;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class SlbClient extends AbstractRestClient {
    public SlbClient(String url) {
        super(url);
    }

    public List<Slb> getAll() {
        String res = getTarget().path("/api/slb").request().get(String.class);
        return null;
    }

    public Response add(Slb slb) {
        return getTarget().path("/api/slb/add").request()
                .post(Entity.entity(
                        String.format(Slb.JSON, slb),
                        MediaType.APPLICATION_JSON
                ));
    }

    public Response update(Slb slb) {
        return getTarget().path("/api/slb/update").request()
                .post(Entity.entity(
                        String.format(Slb.JSON, slb),
                        MediaType.APPLICATION_JSON
                ));
    }

    public Slb get(String slbName) {
        String res = getTarget().path("/api/slb/get/" + slbName).request(MediaType.APPLICATION_JSON).get(String.class);
        try {
            return DefaultJsonParser.parse(Slb.class, res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
