package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.status.GroupStatusService;
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
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/status")
public class StatusResource {

    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;


    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response allGroupStatusInSlb(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("slbId") Long slbId, @QueryParam("slbName") String slbName ) throws Exception {
        Long _slbId = null ; //slbRepository.get(slbName).getId();
        List<GroupStatus> statusList = null;
        if (slbId != null)
        {
            _slbId = slbId;
        }else if (slbName != null){
            _slbId = slbRepository.get(slbName).getId();
        }
        if (null == _slbId)
        {
            statusList = groupStatusService.getAllGroupStatus();
        }else
        {
            statusList = groupStatusService.getAllGroupStatus(_slbId);
        }

        GroupStatusList result = new GroupStatusList();
        for (GroupStatus groupStatus : statusList) {
            result.addGroupStatus(groupStatus);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("groupId") Long groupId, @QueryParam("groupName") String groupName ) throws Exception {
        Long _groupId = null;
        if (groupId != null)
        {
            _groupId = groupId;
        }else if (groupName != null){
            _groupId = groupRepository.get(groupName).getId();
        }
        if (null == _groupId)
        {
            throw new Exception("Group Id or Name not found!");
        }

        List<GroupStatus> statusList = groupStatusService.getGroupStatus(_groupId);
        GroupStatusList result = new GroupStatusList();
        for (GroupStatus groupStatus : statusList) {
            result.addGroupStatus(groupStatus);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }

    }

}
