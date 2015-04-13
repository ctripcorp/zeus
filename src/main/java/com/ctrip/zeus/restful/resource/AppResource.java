package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.AppRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/app")
public class AppResource {
    private static int DEFAULT_MAX_COUNT = 20;
    @Resource
    private AppRepository appRepository;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh,
                         @QueryParam("from") long fromId,
                         @QueryParam("maxCount") int maxCount) throws Exception {
        AppList appList = new AppList();

        if (fromId <= 0 && maxCount <= 0) {
            for (App app : appRepository.list()) {
                appList.addApp(app);
            }
        } else {
            fromId = fromId < 0 ? 0 : fromId;
            maxCount = maxCount <= 0 ? DEFAULT_MAX_COUNT : maxCount;
            for (App app : appRepository.listLimit(fromId, maxCount)) {
                appList.addApp(app);
            }
        }
        appList.setTotal(appList.getApps().size());
        return responseHandler.handle(appList, hh.getMediaType());
    }

    @GET
    @Path("/get/{appName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getByAppName(@Context HttpHeaders hh, @PathParam("appName") String appName) throws Exception {
        App app = appRepository.get(appName);
        return responseHandler.handle(app, hh.getMediaType());
    }

    @GET
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@Context HttpHeaders hh, @QueryParam("appId") String appId) throws Exception {
        if (appId == null || appId.isEmpty()) {
            throw new Exception("Missing parameter or value.");
        }
        App app = appRepository.getByAppId(appId);
        return responseHandler.handle(app, hh.getMediaType());
    }

    @POST
    @Path("/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(@Context HttpHeaders hh, String app) throws Exception {
        App a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(App.class, app);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            a = DefaultJsonParser.parse(App.class, app);
        } else {
            throw new Exception("Unacceptable type.");
        }
        appRepository.add(a);
        return Response.ok().build();
    }

    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response update(@Context HttpHeaders hh, String app) throws Exception {
        App a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(App.class, app);
        } else if (hh.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE)) {
            a = DefaultJsonParser.parse(App.class, app);
        } else {
            throw new Exception("Unacceptable type.");
        }
        appRepository.update(a);
        return Response.ok().build();
    }

    @GET
    @Path("/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(@Context HttpHeaders hh, @QueryParam("appName") String appName) throws Exception {
        if (appName == null || appName.isEmpty())
            throw new Exception("Missing parameter or value.");
        appRepository.delete(appName);
        return Response.ok().build();
    }
}
