package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.model.entity.AppServerStatus;
import com.ctrip.zeus.model.entity.AppStatus;
import com.ctrip.zeus.model.entity.AppStatusList;
import com.ctrip.zeus.service.status.AppStatusService;
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
    private AppStatusService appStatusService;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAppStatus")
    public Response allAppStatus(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        List<AppStatus> statusList = appStatusService.getAllAppStatus();
        AppStatusList result = new AppStatusList();
        for (AppStatus appStatus : statusList) {
            result.addAppStatus(appStatus);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/slb/{slbName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAppStatus", uriGroupHint = -1)
    public Response allAppStatusInSlb(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("slbName") String slbName) throws Exception {

        List<AppStatus> statusList = appStatusService.getAllAppStatus(slbName);
        AppStatusList result = new AppStatusList();
        for (AppStatus appStatus : statusList) {
            result.addAppStatus(appStatus);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/app/{appName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAppStatus", uriGroupHint = -1)
    public Response appStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("appName") String appName) throws Exception {
        List<AppStatus> statusList = appStatusService.getAppStatus(appName);
        AppStatusList result = new AppStatusList();
        for (AppStatus appStatus : statusList) {
            result.addAppStatus(appStatus);
        }
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
        }

    }

    @GET
    @Path("/app/{appName:[a-zA-Z0-9_-]+}/slb/{slbName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAppStatus", uriGroupHint = -1)
    public Response appSlbStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("appName") String appName, @PathParam("slbName") String slbName) throws Exception {
        AppStatus appStatus = appStatusService.getAppStatus(appName, slbName);

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatusList.XML, appStatus)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatusList.JSON, appStatus)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/app/{appName:[a-zA-Z0-9_-]+}/slb/{slbName:[a-zA-Z0-9_-]+}/server/{sip}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAppStatus", uriGroupHint = -1)
    public Response appServerStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("appName") String appName, @PathParam("slbName") String slbName, @PathParam("sip") String sip) throws Exception {
        String[] ipPort = sip.split(":");
        if (ipPort.length != 2){
            throw new IllegalArgumentException("server should be ip:port format");
        }
        AppServerStatus appServerStatus = appStatusService.getAppServerStatus(appName, slbName, ipPort[0], Integer.valueOf(ipPort[1]));

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatusList.XML, appServerStatus)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatusList.JSON, appServerStatus)).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
