package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupList;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.util.AssertUtils;
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
@Path("/")
public class GroupResource {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("slbId") Long slbId,
                         @QueryParam("slbName") String slbName) throws Exception {
        GroupList groupList = new GroupList();
        if (slbId == null && slbName == null) {
            for (Group group : groupRepository.list()) {
                groupList.addGroup(group);
            }
        } else {
            if (slbId == null) {
                Slb slb = slbRepository.get(slbName);
                AssertUtils.assertNotNull(slb, "Slb does not exist.");
                slbId = slbRepository.get(slbName).getId();
            }
            for (Group group : groupRepository.list(slbId)) {
                groupList.addGroup(group);
            }
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(groupList, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroup")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("groupId") Long groupId,
                        @QueryParam("groupName") String groupName,
                        @QueryParam("appId") String appId) throws Exception {
        Group group = null;
        if (groupId == null && groupName == null && appId == null) {
            throw new ValidationException("Missing parameters.");
        }
        if (groupId != null) {
            group = groupRepository.getById(groupId);
        }
        if (group == null && groupName != null) {
            group = groupRepository.get(groupName);
        }
        if (group == null && appId != null) {
            group = groupRepository.getByAppId(appId);
        }
        AssertUtils.assertNotNull(group, "Group cannot be found.");
        return responseHandler.handle(group, hh.getMediaType());
    }

    @POST
    @Path("/group/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addGroup")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = groupRepository.add(parseGroup(hh.getMediaType(), group));
        return responseHandler.handle(g, hh.getMediaType());
    }

    @POST
    @Path("/group/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateGroup")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        DistLock lock = dbLockFactory.newLock(g.getName() + "_updateGroup");
        try {
            lock.lock();
            g = groupRepository.update(g);
        } finally {
            lock.unlock();
        }
        return responseHandler.handle(g, hh.getMediaType());
    }

    @GET
    @Path("/group/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteGroup")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null)
            throw new Exception("Missing parameter.");
        groupRepository.delete(groupId);
        return Response.ok().build();
    }

    private Group parseGroup(MediaType mediaType, String group) throws Exception {
        Group g;
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            g = DefaultSaxParser.parseEntity(Group.class, group);
        } else {
            try {
                g = DefaultJsonParser.parse(Group.class, group);
            } catch (Exception e) {
                throw new Exception("Group cannot be parsed.");
            }
        }
        return g;
    }
}
