package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.metainfo.StatisticsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by fanqq on 2016/7/29.
 */
@Component
@Path("/statistics")
public class StatisticsResource {

    @Autowired
    StatisticsInfo statisticsInfo;

    @Resource
    private ResponseHandler responseHandler;
    //TODO
    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSlbMeta(@Context final HttpHeaders hh,
                               @Context HttpServletRequest request,
                               @QueryParam("slbId") List<Long> slbId) throws Exception {
        SlbMetaList res = new SlbMetaList();
        if (slbId != null && slbId.size() > 0) {
            res.getSlbMetas().addAll(statisticsInfo.getAllSlbMeta(slbId));
        } else {
            res.getSlbMetas().addAll(statisticsInfo.getAllSlbMeta());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/vses")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVsMate(@Context final HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("vsId") List<Long> vsIds) throws Exception {
        VsMetaList res = new VsMetaList();
        if (vsIds != null && vsIds.size() > 0) {
            res.getVsMetas().addAll(statisticsInfo.getAllVsMeta(vsIds));
        } else {
            res.getVsMetas().addAll(statisticsInfo.getAllVsMeta());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getGroupMeta(@Context final HttpHeaders hh,
                                 @Context HttpServletRequest request,
                                 @QueryParam("groupId") List<Long> groupIds) throws Exception {
        GroupMetaList res = new GroupMetaList();
        if (groupIds != null && groupIds.size() > 0) {
            res.getGroupMetas().addAll(statisticsInfo.getAllGroupMeta(groupIds));
        } else {
            res.getGroupMetas().addAll(statisticsInfo.getAllGroupMeta());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/slbServers")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSlbServerMeta(@Context final HttpHeaders hh,
                                     @Context HttpServletRequest request,
                                     @QueryParam("ip") List<String> ips) throws Exception {
        SlbServerQpsList res = new SlbServerQpsList();
        if (ips != null && ips.size() > 0) {
            res.getSlbServerQpses().addAll(statisticsInfo.getAllSlbServerQps(ips));
        } else {
            res.getSlbServerQpses().addAll(statisticsInfo.getAllSlbServerQps());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/sbu")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSbuMeta(@Context final HttpHeaders hh,
                               @Context HttpServletRequest request,
                               @QueryParam("sbu") List<String> sbu) throws Exception {

        SbuMetaList res = new SbuMetaList();
        if (sbu != null && sbu.size() > 0) {
            res.getSbuMetas().addAll(statisticsInfo.getAllSbuMeta(sbu));
        } else {
            res.getSbuMetas().addAll(statisticsInfo.getAllSbuMeta());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/idc")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdcMeta(@Context final HttpHeaders hh,
                               @Context HttpServletRequest request,
                               @QueryParam("idc") List<String> idc) throws Exception {
        IdcMetaList res = new IdcMetaList();
        if (idc != null && idc.size() > 0) {
            res.getIdcMetas().addAll(statisticsInfo.getAllIdcMeta(idc));
        } else {
            res.getIdcMetas().addAll(statisticsInfo.getAllIdcMeta());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }
    @GET
    @Path("/app")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getappMeta(@Context final HttpHeaders hh,
                               @Context HttpServletRequest request,
                               @QueryParam("app") List<String> apps) throws Exception {
        AppMetaList res = new AppMetaList();
        if (apps != null && apps.size() > 0) {
            res.getAppMetas().addAll(statisticsInfo.getAllAppMeta(apps));
        } else {
            res.getAppMetas().addAll(statisticsInfo.getAllAppMeta());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/dist/dashboard")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSlbDashboard(@Context final HttpHeaders hh,
                                    @Context HttpServletRequest request,
                                    @QueryParam("op") String op,
                                    @QueryParam("domain") String domain,
                                    @QueryParam("ip") String ip,
                                    @QueryParam("date") String date,
                                    @QueryParam("type") String type,
                                    @QueryParam("cat") String catEnv) throws Exception {

        return responseHandler.handle(statisticsInfo.getAllDashboardMeta(op, domain, ip, date, type, catEnv), hh.getMediaType());

    }

    @GET
    @Path("/call/dashboard")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getApiCallDashboard(@Context final HttpHeaders hh,
                                        @Context HttpServletRequest request,
                                        @QueryParam("op") String op,
                                        @QueryParam("domain") String domain,
                                        @QueryParam("ip") String ip,
                                        @QueryParam("date") String date,
                                        @QueryParam("type") String type,
                                        @QueryParam("cat") String catEnv) throws Exception {
        return responseHandler.handle(statisticsInfo.getApiDashboardMeta(op, domain, ip, date, type, catEnv), hh.getMediaType());

    }
}
