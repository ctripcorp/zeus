package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.clean.CleanFilter;
import com.ctrip.zeus.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by fanqq on 2015/10/21.
 */

@Component
@Path("/clean")
public class CleanResource {

    @Resource
    private CleanFilter taskCleanFilter;
    @Resource
    private CleanFilter confCleanFilter;
    @Resource
    private CleanFilter archiveCleanFilter;
    @Resource
    private AuthService authService;

    @GET
    @Path("/task")
    public Response cleanTask(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE, ResourceDataType.Clean, AuthDefaultValues.ALL);
        taskCleanFilter.runFilter();
        return Response.status(200).entity("Clean Task List Success.").type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/config")
    public Response cleanConfig(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE, ResourceDataType.Clean, AuthDefaultValues.ALL);
        confCleanFilter.runFilter();
        return Response.status(200).entity("Clean Config List Success.").type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/archive")
    public Response archiveConfig(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE, ResourceDataType.Clean, AuthDefaultValues.ALL);
        archiveCleanFilter.runFilter();
        return Response.status(200).entity("Clean Archive List Success.").type(MediaType.APPLICATION_JSON).build();
    }
}
