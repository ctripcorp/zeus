package com.ctrip.zeus.client;


import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupList;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.ctrip.zeus.auth.util.AuthTokenUtil.getDefaultHeaders;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class GroupClient extends AbstractRestClient {
    public GroupClient(String url) {
        super(url);
    }

    public List<Group> getAll() {
        String res = getTarget().path("/api/groups").queryParam("type", "detail").request().headers(getDefaultHeaders()).get(String.class);
        GroupList result = ObjectJsonParser.parse(res, GroupList.class);
        return result == null ? new ArrayList<Group>() : result.getGroups();
    }

    public Group get(String groupName) {
        String res = getTarget().path("/api/group").queryParam("groupName", groupName).queryParam("type", "detail").request(MediaType.APPLICATION_JSON)
                .headers(getDefaultHeaders()).get(String.class);

        return ObjectJsonParser.parse(res, Group.class);
    }

    public List<Group> getGroupsByVsId(String vsId) {
        String res = getTarget().path("/api/groups").queryParam("vsId", vsId).request().headers(getDefaultHeaders()).get(String.class);
        GroupList result = ObjectJsonParser.parse(res, GroupList.class);
        return result == null ? new ArrayList<Group>() : result.getGroups();
    }

    public Response add(Group group) throws JsonProcessingException {
        return getTarget().path("/api/group/new").request().headers(getDefaultHeaders())
                .post(Entity.entity(
                        ObjectJsonWriter.write(group), MediaType.APPLICATION_JSON));
    }

    public Response update(Group group) throws JsonProcessingException{
        return getTarget().path("/api/group/update").request().headers(getDefaultHeaders())
                .post(Entity.entity(
                        ObjectJsonWriter.write(group), MediaType.APPLICATION_JSON));

    }

    public Response delete(Long groupId) {
        return getTarget().path("/api/group/delete").queryParam("groupId", groupId).request(MediaType.APPLICATION_JSON)
                .headers(getDefaultHeaders()).get();
    }
}
