package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/group")
public class GroupResource {
    private static int DEFAULT_MAX_COUNT = 20;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("from") long fromId,
                         @QueryParam("maxCount") int maxCount) throws Exception {
        GroupList groupList = new GroupList();

        if (fromId <= 0 && maxCount <= 0) {
            for (Group group : groupRepository.list()) {
                groupList.addGroup(group);
            }
        } else {
            fromId = fromId < 0 ? 0 : fromId;
            maxCount = maxCount <= 0 ? DEFAULT_MAX_COUNT : maxCount;
            for (Group group : groupRepository.listLimit(fromId, maxCount)) {
                groupList.addGroup(group);
            }
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(groupList, hh.getMediaType());
    }

    @GET
    @Path("/get/{groupName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroup")
    public Response getByGroupName(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @PathParam("groupName") String groupName) throws Exception {
        Group group = groupRepository.get(groupName);
        return responseHandler.handle(group, hh.getMediaType());
    }

    @GET
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroup")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("id") Long groupId,
                        @QueryParam("appId") String appId) throws Exception {
        Group group;
        if (groupId != null) {
            group = groupRepository.getById(groupId);
        } else if (appId != null) {
            group = groupRepository.getByAppId(appId);
        } else {
            throw new Exception("Missing parameter.");
        }
        return responseHandler.handle(group, hh.getMediaType());
    }

    @POST
    @Path("/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addGroup")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(Group.class, group);
        } else {
            try {
                a = DefaultJsonParser.parse(Group.class, group);
            } catch (Exception ex) {
                throw new Exception("Unacceptable type.");
            }
        }
        groupRepository.add(a);
        return Response.ok().build();
    }

    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateGroup")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(Group.class, group);
        } else {
            try {
                a = DefaultJsonParser.parse(Group.class, group);
            } catch (Exception e) {
                throw new Exception("Unacceptable type.");
            }
        }
        DistLock lock = dbLockFactory.newLock(a.getName() + "_updateGroup");
        try {
            lock.lock();
            groupRepository.update(a);
        } finally {
            lock.unlock();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteGroup")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null)
            throw new Exception("Missing parameter.");
        groupRepository.delete(groupId);
        return Response.ok().build();
    }
}
