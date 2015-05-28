package com.ctrip.zeus.client;


import com.ctrip.zeus.model.entity.Group;
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
public class AppClient extends AbstractRestClient {
    public AppClient(String url) {
        super(url);
    }

    public List<Group> getAll() {
        String res = getTarget().path("/api/group").request().headers(getDefaultHeaders()).get(String.class);
        return null;
    }

    public Response add(Group group) {
        return getTarget().path("/api/group/add").request().headers(getDefaultHeaders())
                .post(Entity.entity(
                        String.format(Group.JSON, group),
                        MediaType.APPLICATION_JSON
                ));

    }

    public Group get(String groupName) {
        String res = getTarget().path("/api/group/get/" + groupName).request(MediaType.APPLICATION_JSON).headers(getDefaultHeaders()).get(String.class);
        try {
            return DefaultJsonParser.parse(Group.class, res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
