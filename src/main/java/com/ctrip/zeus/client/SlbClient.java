package com.ctrip.zeus.client;


import com.ctrip.zeus.model.entity.Slb;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        System.out.println(res);
        return null;
    }

    public void add(Slb slbCluster) {
        Response res = getTarget().path("/api/slb").request()
                .post(Entity.entity(
                        String.format(Slb.JSON, slbCluster),
                        MediaType.APPLICATION_JSON
                ));
        if (res.getStatus() != 200) {
            throw new RuntimeException(String.valueOf(res.getStatus()));
        }
    }
}
