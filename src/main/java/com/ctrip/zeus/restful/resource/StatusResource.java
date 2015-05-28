package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.status.GroupStatusService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus")
    public Response allGroupStatus(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        List<GroupStatus> statusList = groupStatusService.getAllGroupStatus();
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
    @Path("/slb/{slbId:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response allGroupStatusInSlb(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("slbId") Long slbId) throws Exception {

        List<GroupStatus> statusList = groupStatusService.getAllGroupStatus(slbId);
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

//    @GET
//    @Path("/slb/{slbName:[a-zA-Z0-9_-]+}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
//    public Response allGroupStatusInSlb(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("slbName") String slbName) throws Exception {
//
//        List<AppStatus> statusList = appStatusService.getAllAppStatus(slbName);
//        AppStatusList result = new AppStatusList();
//        for (AppStatus appStatus : statusList) {
//            result.addAppStatus(appStatus);
//        }
//        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
//            return Response.status(200).entity(String.format(AppStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
//        } else {
//            return Response.status(200).entity(String.format(AppStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
//        }
//    }

    @GET
    @Path("/group/{groupId:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("groupId") Long groupId) throws Exception {
        List<GroupStatus> statusList = groupStatusService.getGroupStatus(groupId);
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

//    @GET
//    @Path("/app/{appName:[a-zA-Z0-9_-]+}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
//    public Response groupStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("appName") String appName) throws Exception {
//        List<AppStatus> statusList = appStatusService.getAppStatus(appName);
//        AppStatusList result = new AppStatusList();
//        for (AppStatus appStatus : statusList) {
//            result.addAppStatus(appStatus);
//        }
//        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
//            return Response.status(200).entity(String.format(AppStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
//        } else {
//            return Response.status(200).entity(String.format(AppStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
//        }
//
//    }

    @GET
    @Path("/group/{groupId:[0-9]+}/slb/{slbId:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupSlbStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("groupId") Long groupId, @PathParam("slbId") Long slbId) throws Exception {
        GroupStatus groupStatus = groupStatusService.getGroupStatus(groupId, slbId);

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupStatus.XML, groupStatus)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupStatus.JSON, groupStatus)).type(MediaType.APPLICATION_JSON).build();
        }
    }


    @GET
    @Path("/group/{groupId:[0-9]+}/slb/{slbId:[0-9]+}/server/{sip}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAppStatus", uriGroupHint = -1)
    public Response appServerStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("groupId") Long groupId, @PathParam("slbId") Long slbId, @PathParam("sip") String sip) throws Exception {
        String[] ipPort = sip.split(":");
        if (ipPort.length != 2){
            throw new IllegalArgumentException("server should be ip:port format");
        }
        GroupServerStatus groupServerStatus = groupStatusService.getGroupServerStatus(groupId, slbId, ipPort[0], Integer.valueOf(ipPort[1]));

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupServerStatus.XML, groupServerStatus)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupServerStatus.JSON, groupServerStatus)).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
