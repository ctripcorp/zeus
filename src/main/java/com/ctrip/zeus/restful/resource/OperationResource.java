package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.AppStatus;
import com.ctrip.zeus.model.entity.ServerStatus;
import com.ctrip.zeus.service.nginx.NginxAgentService;
import com.ctrip.zeus.service.status.StatusService;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
@Component
@Path("/op")
public class OperationResource {
    @Resource
    private StatusService statusService;
    @Resource
    private NginxAgentService nginxAgentService;

    @GET
    @Path("/upServer")
    public Response upServer(@Context HttpHeaders hh, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.upServer(ip);
        reloadNginxConf();
        ServerStatus ss = statusService.getServerStatus(ip);
        return Response.ok().build();
    }

    @GET
    @Path("/downServer")
    public Response downServer(@Context HttpHeaders hh, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.downServer(ip);
        reloadNginxConf();
        return Response.ok().build();
    }

    @GET
    @Path("/upMember")
    public Response upMember(@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.upMember(appName, ip);
        reloadNginxConf();
        AppStatus as = statusService.getAppStatus(appName);
        return Response.status(200).entity(String.format(AppStatus.JSON, as)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/downMember")
    public Response downMember(@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.downMember(appName, ip);
        reloadNginxConf();
        AppStatus as = statusService.getAppStatus(appName);
        return Response.status(200).entity(String.format(AppStatus.JSON, as)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private void reloadNginxConf() {
        nginxAgentService.reloadConf("default");
    }

}
