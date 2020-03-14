package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.operationlog.OperationLogDataList;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.operationLog.OperationLogService;
import com.ctrip.zeus.util.UserUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2015/7/20.
 */
@Component
@Path("/logs")
public class OperationLogResource {
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private AuthService authService;

    //TODO Auth Type:id
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response logs(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("type") String type,
                         @QueryParam("targetId") String targetId,
                         @QueryParam("op") String op,
                         @QueryParam("user") String user,
                         @QueryParam("clientIp") String clientIp,
                         @QueryParam("success") Boolean success,
                         @QueryParam("fromDate") String from,
                         @QueryParam("toDate") String to,
                         @QueryParam("count") Long count
    ) throws Exception {
        List<ResourceDataType> dataTypes = new ArrayList<>();
        if (type == null) {
            dataTypes.add(ResourceDataType.Group);
            dataTypes.add(ResourceDataType.Vs);
            dataTypes.add(ResourceDataType.Slb);
        } else if (type.equalsIgnoreCase("group")) {
            dataTypes.add(ResourceDataType.Group);
        } else if (type.equalsIgnoreCase("slb")) {
            dataTypes.add(ResourceDataType.Slb);
        } else if (type.equalsIgnoreCase("vs")) {
            dataTypes.add(ResourceDataType.Vs);
        }
        if (targetId == null) {
            for (ResourceDataType dt : dataTypes) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, dt, AuthDefaultValues.ALL);
            }
        } else {
            for (ResourceDataType dt : dataTypes) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, dt, targetId);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date fromDate = null;
        Date toDate = null;
        if (from != null) {
            fromDate = sdf.parse(from);
        }
        if (to != null) {
            toDate = sdf.parse(to);
        }
        OperationLogDataList result = operationLogService.find(type, targetId, op, user, clientIp, success, fromDate, toDate, count);
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/count")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response count(@Context HttpServletRequest request,
                          @Context HttpHeaders hh,
                          @QueryParam("type") String type,
                          @QueryParam("targetId") String targetId,
                          @QueryParam("op") String op,
                          @QueryParam("success") Boolean success,
                          @QueryParam("fromDate") String from,
                          @QueryParam("toDate") String to
    ) throws Exception {
        List<ResourceDataType> dataTypes = new ArrayList<>();
        if (type == null) {
            dataTypes.add(ResourceDataType.Group);
            dataTypes.add(ResourceDataType.Vs);
            dataTypes.add(ResourceDataType.Slb);
        } else if (type.equalsIgnoreCase("group")) {
            dataTypes.add(ResourceDataType.Group);
        } else if (type.equalsIgnoreCase("slb")) {
            dataTypes.add(ResourceDataType.Slb);
        } else if (type.equalsIgnoreCase("vs")) {
            dataTypes.add(ResourceDataType.Vs);
        }
        if (targetId == null) {
            for (ResourceDataType dt : dataTypes) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, dt, AuthDefaultValues.ALL);
            }
        } else {
            for (ResourceDataType dt : dataTypes) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, dt, targetId);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        Date fromDate = null;
        Date toDate = null;
        if (from != null) {
            fromDate = sdf.parse(from);
        }
        if (to != null) {
            toDate = sdf.parse(to);
        }
        Map<String, Long> result = operationLogService.count(op == null ? null : op.split(","), type, targetId == null ? null : targetId.split(","), success, fromDate, toDate);
        return responseHandler.handle(result, hh.getMediaType());
    }
}
