package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.GroupServerList;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupMemberRepository;
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
import java.util.List;

/**
 * Created by zhoumy on 2015/8/6.
 */
@Component
@Path("/")
public class GroupMemberResource {
    @Resource
    private GroupMemberRepository groupMemberRepository;
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
        GroupServerList groupServerList = new GroupServerList().setGroupId(groupId);
        for (GroupServer groupServer : groupMemberRepository.listGroupServersByGroup(groupId)) {
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
        for (GroupServer groupServer : gsl.getGroupServers()) {
            groupMemberRepository.addGroupServer(gsl.getGroupId(), groupServer);
        }
        groupRepository.updateVersion(new Long[] {gsl.getGroupId()});
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
        for (GroupServer groupServer : gsl.getGroupServers()) {
            groupMemberRepository.updateGroupServer(gsl.getGroupId(), groupServer);
        }
        groupRepository.updateVersion(new Long[] {gsl.getGroupId()});
        return responseHandler.handle("Successfully updated group servers to group with id " + gsl.getGroupId() + ".", hh.getMediaType());
    }

    @GET
    @Path("/member/remove")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "removeMember")
    public Response removeMember(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId,
                                 @QueryParam("ip") List<String> ips) throws Exception {
        if (groupId == null)
            throw new ValidationException("Group id parameter is required.");
        for (String ip : ips) {
            groupMemberRepository.removeGroupServer(groupId, ip);
        }
        groupRepository.updateVersion(new Long[] {groupId});
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
                throw new Exception("Group cannot be parsed.");
            }
        }
        return gsl;
    }
}
