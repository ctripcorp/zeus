package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.AppRepository;
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
    private static int DEFAULT_MAX_COUNT = 20;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh, @QueryParam("from") long fromId, @QueryParam("maxCount") int maxCount) {
        AppList appList = null;
        try {
            if (fromId <= 0 && maxCount <= 0) {
                appList = appRepository.list();
            } else {
                fromId = fromId < 0 ? 0 : fromId;
                maxCount = maxCount <= 0 ? DEFAULT_MAX_COUNT : maxCount;
                appList = appRepository.listLimit(fromId, maxCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appList != null) {
            if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
                return Response.status(200).entity(String.format(AppList.XML, appList)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(AppList.JSON, appList)).type(MediaType.APPLICATION_JSON).build();
            }
        }
        return Response.status(404).type(hh.getMediaType()).build();
    }

    @GET
    @Path("/get/{appName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getByAppName(@Context HttpHeaders hh, @PathParam("appName") String appName) {
        App app = null;
        try {
            app = appRepository.get(appName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (app != null && app.getName() != null) {
            if (hh.getAcceptableMediaTypes().contains(MediaType.APPLICATION_ATOM_XML_TYPE)) {
                return Response.status(200).entity(String.format(App.XML, app)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(App.JSON, app)).type(MediaType.APPLICATION_JSON).build();
            }
        }
        return Response.status(404).type(hh.getMediaType()).build();
    }

    @GET
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@Context HttpHeaders hh, @PathParam("appId") String appId) {
        App app = null;
        if (!appId.isEmpty()) {
            try {
                app = appRepository.getByAppId(appId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (app != null && app.getName() != null) {
            if (hh.getAcceptableMediaTypes().contains(MediaType.APPLICATION_ATOM_XML_TYPE)) {
                return Response.status(200).entity(String.format(App.XML, app)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(App.JSON, app)).type(MediaType.APPLICATION_JSON).build();
            }
        }
        return Response.status(404).type(hh.getMediaType()).build();
    }

    @POST
    @Path("/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@Context HttpHeaders hh, String app) throws IOException, SAXException {
        App a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_ATOM_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(App.class, app);
        } else {
            a = DefaultJsonParser.parse(App.class, app);
        }
        try {
            appRepository.add(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response update(@Context HttpHeaders hh, String app) throws IOException, SAXException {
        App a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_ATOM_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(App.class, app);
        } else {
            a = DefaultJsonParser.parse(App.class, app);
        }
        try {
            appRepository.update(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Context HttpHeaders hh, @PathParam("appName") String appName) {
        try {
            appRepository.delete(appName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }
}
