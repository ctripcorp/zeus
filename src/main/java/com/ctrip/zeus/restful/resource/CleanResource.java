package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.service.clean.CleanFilter;
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
    private CleanFilter oprationLogCleanFilter;
    @Resource
    private CleanFilter reportCleanFilter;
    @GET
    @Path("/task")
    @Authorize(name="activate")
    public Response cleanTask(@Context HttpServletRequest request,@Context HttpHeaders hh)throws Exception{
        taskCleanFilter.runFilter();
        return Response.status(200).entity("Clean Task List Success.").type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/config")
    @Authorize(name="activate")
    public Response cleanConfig(@Context HttpServletRequest request,@Context HttpHeaders hh)throws Exception{
        confCleanFilter.runFilter();
        return Response.status(200).entity("Clean Config List Success.").type(MediaType.APPLICATION_JSON).build();
    }
    @GET
    @Path("/archive")
    @Authorize(name="activate")
    public Response archiveConfig(@Context HttpServletRequest request,@Context HttpHeaders hh)throws Exception{
        archiveCleanFilter.runFilter();
        return Response.status(200).entity("Clean Archive List Success.").type(MediaType.APPLICATION_JSON).build();
    }
    @GET
    @Path("/operationLog")
    @Authorize(name="activate")
    public Response operationLogConfig(@Context HttpServletRequest request,@Context HttpHeaders hh)throws Exception{
        oprationLogCleanFilter.runFilter();
        return Response.status(200).entity("Clean Operation Log List Success.").type(MediaType.APPLICATION_JSON).build();
    }
    @GET
    @Path("/report")
    @Authorize(name="activate")
    public Response report(@Context HttpServletRequest request,@Context HttpHeaders hh)throws Exception{
        reportCleanFilter.runFilter();
        return Response.status(200).entity("Clean reported version List Success.").type(MediaType.APPLICATION_JSON).build();
    }
}
