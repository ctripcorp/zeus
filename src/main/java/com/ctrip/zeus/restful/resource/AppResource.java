package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private GroupRepository groupRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllApps")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("from") long fromId,
                         @QueryParam("maxCount") int maxCount) throws Exception {
        AppList appList = new AppList();

        if (fromId <= 0 && maxCount <= 0) {
            for (App app : groupRepository.list()) {
                appList.addApp(app);
            }
        } else {
            fromId = fromId < 0 ? 0 : fromId;
            maxCount = maxCount <= 0 ? DEFAULT_MAX_COUNT : maxCount;
            for (App app : groupRepository.listLimit(fromId, maxCount)) {
                appList.addApp(app);
            }
        }
        appList.setTotal(appList.getApps().size());
        return responseHandler.handle(appList, hh.getMediaType());
    }

    @GET
    @Path("/get/{appName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getApp")
    public Response getByAppName(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                 @PathParam("appName") String appName) throws Exception {
        App app = groupRepository.get(appName);
        return responseHandler.handle(app, hh.getMediaType());
    }

    @GET
    @Path("/get")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getApp")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("appId") String appId) throws Exception {
        if (appId == null || appId.isEmpty()) {
            throw new Exception("Missing parameter or value.");
        }
        App app = groupRepository.getByAppId(appId);
        return responseHandler.handle(app, hh.getMediaType());
    }

    @POST
    @Path("/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addApp")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String app) throws Exception {
        App a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(App.class, app);
        } else {
            try {
                a = DefaultJsonParser.parse(App.class, app);
            } catch (Exception ex) {
                throw new Exception("Unacceptable type.");
            }
        }
        groupRepository.add(a);
        return Response.ok().build();
    }

    @POST
    @Path("/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateApp")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String app) throws Exception {
        App a;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(App.class, app);
        } else {
            try {
                a = DefaultJsonParser.parse(App.class, app);
            } catch (Exception e) {
                throw new Exception("Unacceptable type.");
            }
        }
        DistLock lock = dbLockFactory.newLock(a.getName() + "_update");
        try {
            lock.lock();
            groupRepository.update(a);
        } finally {
            lock.unlock();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteApp")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("appName") String appName) throws Exception {
        if (appName == null || appName.isEmpty())
            throw new Exception("Missing parameter or value.");
        groupRepository.delete(appName);
        return Response.ok().build();
    }
}
