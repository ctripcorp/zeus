package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxResponseList;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.NginxServerStatusList;
import com.ctrip.zeus.service.nginx.NginxService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
@Path("/nginx")
public class NginxResource {

    @Resource
    private NginxService nginxService;

    @GET
    @Path("/load")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response load(@Context HttpHeaders hh) {
        try {
            NginxResponse result = nginxService.load();
            if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
                return Response.status(200).entity(String.format(NginxResponse.XML, result)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(NginxResponse.JSON, result)).type(MediaType.APPLICATION_JSON).build();
            }
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response status(@Context HttpHeaders hh) {
        try {
            NginxServerStatus status = nginxService.getStatus();
            if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
                return Response.status(200).entity(String.format(NginxServerStatus.XML, status)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(NginxServerStatus.JSON, status)).type(MediaType.APPLICATION_JSON).build();
            }
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/loadAll/slb/{slbName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response loadAll(@Context HttpHeaders hh, @PathParam("slbName") String slbName) {
        try {
            List<NginxResponse> nginxResponseList = nginxService.loadAll(slbName);
            NginxResponseList result = new NginxResponseList();
            for (NginxResponse nginxResponse : nginxResponseList) {
                result.addNginxResponse(nginxResponse);
            }
            if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
                return Response.status(200).entity(String.format(NginxResponseList.XML, result)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(NginxResponseList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
            }
        }catch (Exception e){
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/allStatus/slb/{slbName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response allStatus(@Context HttpHeaders hh, @PathParam("slbName") String slbName) {
        try {
            List<NginxServerStatus> nginxServerStatusList = nginxService.getStatusAll(slbName);
            NginxServerStatusList result = new NginxServerStatusList();
            for (NginxServerStatus nginxServerStatus : nginxServerStatusList) {
                result.addNginxServerStatus(nginxServerStatus);
            }
            if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
                return Response.status(200).entity(String.format(NginxServerStatusList.XML, result)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(NginxServerStatusList.JSON, result)).type(MediaType.APPLICATION_JSON).build();
            }
        }catch (Exception e){
            return Response.serverError().build();
        }
    }
}
