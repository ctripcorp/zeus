package com.ctrip.zeus.client;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.support.ObjectJsonParser;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
        String res = getTarget().path("/api/slbs").queryParam("type", "detail").request().headers(getDefaultHeaders()).get(String.class);
        SlbList result = ObjectJsonParser.parse(res, SlbList.class);
        return result == null ? new ArrayList<Slb>() : result.getSlbs();
    }

    public Slb get(String slbName) {
        String res = getTarget().path("/api/slb").queryParam("slbName", slbName).queryParam("type", "detail").request(MediaType.APPLICATION_JSON)
                .headers(getDefaultHeaders()).get(String.class);
        return ObjectJsonParser.parse(res, Slb.class);
    }

    public Response add(Slb slb) {
        return getTarget().path("/api/slb/new").request().headers(getDefaultHeaders())
                .post(Entity.entity(
                        GenericSerializer.writeJson(slb), MediaType.APPLICATION_JSON));
    }

    public Response update(Slb slb) {
        return getTarget().path("/api/slb/update").request().headers(getDefaultHeaders())
                .post(Entity.entity(
                        GenericSerializer.writeJson(slb), MediaType.APPLICATION_JSON));
    }

    public Response delete(Long slbId) {
        return getTarget().path("/api/slb/delete").queryParam("slbId", slbId).request(MediaType.APPLICATION_JSON)
                .headers(getDefaultHeaders()).get();
    }
}
