package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.service.AppRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/app")
public class AppResource {
    @Resource
    private AppRepository appRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response list() {
        return Response.ok("hello").build();
    }

    @GET
    @Path("/{appName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response get(@Context HttpHeaders hh, @PathParam("appName") String appName) {
        List<App> list = appRepository.list();
        for (App app : list) {
            if (appName.equals(app.getName())) {
                if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
                    return Response.status(200).entity(String.format(App.XML, app)).type(MediaType.APPLICATION_XML).build();
                }else{
                    return Response.status(200).entity(String.format(App.JSON, app)).type(MediaType.APPLICATION_JSON).build();
                }
            }
        }
        return Response.status(404).type(hh.getMediaType()).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(String app) {
        try {
            App a = DefaultJsonParser.parse(App.class, app);
            appRepository.add(a);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }
}
