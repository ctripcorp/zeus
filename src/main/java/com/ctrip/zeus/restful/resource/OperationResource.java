package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.MemberAction;
import com.ctrip.zeus.model.entity.ServerAction;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.status.StatusService;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/16/2015.
 */
@Component
@Path("/op")
public class OperationResource {
    @Resource
    private StatusService statusService;

    @GET
    @Path("/upServer")
    public Response upServer(@Context HttpHeaders hh, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.upServer(ip);
        return Response.ok().build();
    }

    @GET
    @Path("/downServer")
    public Response downServer(@Context HttpHeaders hh, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.downServer(ip);
        return Response.ok().build();
    }

    @GET
    @Path("/upMember")
    public Response upMember(@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.upMember(appName, ip);
        return Response.ok().build();
    }

    @GET
    @Path("/downMember")
    public Response downMember(@Context HttpHeaders hh, @QueryParam("appName") String appName, @QueryParam("ip") String ip) throws IOException, SAXException {
        statusService.downMember(appName, ip);
        return Response.ok().build();
    }


}
