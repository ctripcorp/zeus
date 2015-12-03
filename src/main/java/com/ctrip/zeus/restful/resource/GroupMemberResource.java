package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupServerList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupRepository;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/8/6.
 */
@Component
@Path("/")
public class GroupMemberResource {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/members")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllByGroup")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("groupId") Long groupId) throws Exception {
        Group group = groupRepository.getById(groupId);
        if (group == null)
            throw new ValidationException("Group with id " + groupId + " does not exist.");
        GroupServerList groupServerList = new GroupServerList().setGroupId(groupId);
        for (GroupServer groupServer : group.getGroupServers()) {
            groupServerList.addGroupServer(groupServer);
        }
        return responseHandler.handle(groupServerList, hh.getMediaType());
    }

    @POST
    @Path("/member/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "addMember")
    public Response addMember(@Context HttpHeaders hh, @Context HttpServletRequest request, String groupServerList) throws Exception {
        GroupServerList gsl = parseGroupServer(hh.getMediaType(), groupServerList);
        if (gsl.getGroupId() == null)
            throw new ValidationException("Group id is required.");
        Group group = groupRepository.getById(gsl.getGroupId());
        if (group == null)
            throw new ValidationException("Group with id " + gsl.getGroupId() + " does not exist.");
        for (GroupServer groupServer : gsl.getGroupServers()) {
            group.getGroupServers().add(groupServer);
        }
        groupRepository.update(group);
        return responseHandler.handle("Successfully added group servers to group with id " + gsl.getGroupId() + ".", hh.getMediaType());
    }

    @POST
    @Path("/member/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "updateMember")
    public Response updateMember(@Context HttpHeaders hh, @Context HttpServletRequest request, String groupServerList) throws Exception {
        GroupServerList gsl = parseGroupServer(hh.getMediaType(), groupServerList);
        if (gsl.getGroupId() == null)
            throw new ValidationException("Group id is required.");
        Group group = groupRepository.getById(gsl.getGroupId());
        if (group == null)
            throw new ValidationException("Group with id " + gsl.getGroupId() + " does not exist.");
        Map<String, GroupServer> groupServers = new HashMap<>();
        for (GroupServer gs : group.getGroupServers()) {
            groupServers.put(gs.getIp() + ":" + gs.getPort(), gs);
        }
        // replace old values with new ones
        for (GroupServer gs : gsl.getGroupServers()) {
            String key = gs.getIp() + ":" + gs.getPort();
            if (groupServers.containsKey(key))
                groupServers.put(key, gs);
        }
        group.getGroupServers().clear();
        for (GroupServer gs : groupServers.values()) {
            group.getGroupServers().add(gs);
        }
        groupRepository.update(group);
        return responseHandler.handle("Successfully updated group servers to group with id " + gsl.getGroupId() + ".", hh.getMediaType());
    }

    @GET
    @Path("/member/remove")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "removeMember")
    public Response removeMember(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @QueryParam("groupId") Long groupId,
                                 @QueryParam("ip") List<String> ips) throws Exception {
        if (groupId == null)
            throw new ValidationException("Group id parameter is required.");
        Group group = groupRepository.getById(groupId);
        if (group == null)
            throw new ValidationException("Group with id " + groupId + " does not exist.");
        Map<String, GroupServer> groupServers = new HashMap<>();
        for (GroupServer gs : group.getGroupServers()) {
            groupServers.put(gs.getIp(), gs);
        }
        for (String ip : ips) {
            groupServers.remove(ip);
        }
        group.getGroupServers().clear();
        for (GroupServer gs : groupServers.values()) {
            group.getGroupServers().add(gs);
        }
        groupRepository.update(group);
        return responseHandler.handle("Successfully removed " + Joiner.on(",").join(ips) + " from group with id " + groupId + ".", hh.getMediaType());
    }

    private GroupServerList parseGroupServer(MediaType mediaType, String groupServerList) throws Exception {
        GroupServerList gsl;
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            gsl = DefaultSaxParser.parseEntity(GroupServerList.class, groupServerList);
        } else {
            try {
                gsl = DefaultJsonParser.parse(GroupServerList.class, groupServerList);
            } catch (Exception e) {
                throw new Exception("Group member list cannot be parsed.");
            }
        }
        return gsl;
    }
}
