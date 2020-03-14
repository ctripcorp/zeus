package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.model.AppList;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.app.AppQueryEngine;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.app.impl.AppCriteriaNodeQuery;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.tag.TagBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * Created by fanqq on 2016/9/14.
 */
@Component
@Path("/")
public class AppResource {
    @Autowired
    AppService appService;
    @Resource
    AppQueryEngine appQueryEngine;
    @Resource
    ResponseHandler responseHandler;
    @Resource
    AppCriteriaNodeQuery appCriteriaNodeQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private TagBox tagBox;
    @Autowired
    private GroupRepository groupRepository;


    @GET
    @Path("/app/fillAll")
    public Response appFillAll(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        appService.refreshAllRelationTable();
        return responseHandler.handle("success", hh.getMediaType());
    }

    @GET
    @Path("/app/fill")
    public Response appFill(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("appId") List<String> appIds) throws Exception {
        if (appIds != null) {
            for (String appId : appIds) {
                appService.refreshByAppId(appId);
            }
        }
        return responseHandler.handle("success", hh.getMediaType());
    }


    @GET
    @Path("/apps")
    public Response list(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request,
                         @Context UriInfo uriInfo) throws Exception {
        Set<String> apps = appQueryEngine.executeQuery(appCriteriaNodeQuery, appQueryEngine.parseQueryNode(uriInfo));
        AppList result = new AppList();
        result.getApps().addAll(appService.getAllAppsByAppIds(apps));
        result.setTotal(result.getApps().size());

        return responseHandler.handle(result, hh.getMediaType());
    }
    @GET
    @Path("/app/updateApps")
    public Response updateApps(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("appId") List<String> appIds) throws Exception {
        if (appIds != null && appIds.size() > 0) {
            for (String appId : appIds) {
                appService.updateApp(appId);
            }
        } else {
            appService.updateAllApp();
        }
        return responseHandler.handle("success", hh.getMediaType());
    }

    @GET
    @Path("/sbu")
    public Response getSbuCode(@Context HttpHeaders hh,
                               @Context final HttpServletRequest request,
                               @QueryParam("sbu") String sbu) throws Exception {
        if (sbu == null) {
            throw new ValidationException("Need Parameter sbu.");
        }
        App app = appService.getAppBySbu(sbu);

        return responseHandler.handle(app, hh.getMediaType());
    }

    @GET
    @Path("/group/owner/fill")
    public Response fillOwner(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") List<Long> groupId) throws Exception {
        Set<Long> gids = new HashSet<>();
        if (groupId == null || groupId.size() == 0) {
            gids = groupCriteriaQuery.queryAll();
        } else {
            gids.addAll(groupId);
        }
        Set<Long> updateFailed = new HashSet<>();
        List<Group> groups = groupRepository.list(gids.toArray(new Long[gids.size()]));
        List<App> apps = appService.getAllApps();
        Map<String, App> map = new HashMap<>();
        for (App a : apps) {
            map.put(a.getAppId(), a);
        }
        for (Group g : groups) {
            try {
                String appId = g.getAppId();
                App app = map.get(appId);
                if (app == null || app.getOwner() == null) {
                    tagBox.tagging("owner_unknown", "group", new Long[]{g.getId()});
                } else {
                    tagBox.tagging("owner_" + app.getOwner(), "group", new Long[]{g.getId()});
                }
            } catch (Exception e) {
                updateFailed.add(g.getId());
            }
        }
        if (updateFailed.size() == 0) {
            return responseHandler.handle("Fill All Success.", hh.getMediaType());
        } else {
            return responseHandler.handle("Failed to update " + updateFailed.toString(), hh.getMediaType());
        }
    }
}
