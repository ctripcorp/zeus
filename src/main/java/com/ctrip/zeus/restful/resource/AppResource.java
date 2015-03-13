package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.AppRepository;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

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
    public Response list(@Context HttpHeaders hh) {
        AppList appList = appRepository.list();
        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(AppList.XML, appList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(AppList.JSON, appList)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/{appName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response get(@Context HttpHeaders hh, @PathParam("appName") String appName) {
        App app = appRepository.get(appName);

        if (app.getName() != null) {
            if (hh.getAcceptableMediaTypes().contains(MediaType.APPLICATION_ATOM_XML_TYPE)) {
                return Response.status(200).entity(String.format(App.XML, app)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(App.JSON, app)).type(MediaType.APPLICATION_JSON).build();
            }
        }

        return Response.status(404).type(hh.getMediaType()).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@Context HttpHeaders hh, String app) throws IOException, SAXException {
        App a = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_ATOM_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(App.class, app);
        }else{
            a = DefaultJsonParser.parse(App.class, app);
        }
        appRepository.add(a);
        return Response.ok().build();
    }
}
