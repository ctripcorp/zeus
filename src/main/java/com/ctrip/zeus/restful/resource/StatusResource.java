package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.AppStatusList;
import com.ctrip.zeus.service.status.StatusService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/status")
public class StatusResource {

    @Resource
    private StatusService statusService;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response status(@Context HttpHeaders hh) {
        AppStatusList appStatusList = statusService.getAllAppStatus("default");

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppStatusList.XML, appStatusList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppStatusList.JSON, appStatusList)).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
