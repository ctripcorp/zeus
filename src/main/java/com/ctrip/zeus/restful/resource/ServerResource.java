package com.ctrip.zeus.restful.resource;

import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component
@Path("/server")
public class ServerResource {

    @POST
    @Path("/upServer")
    public Response upServer(@Context HttpHeaders hh,String req) {
        return Response.ok().build();
    }

    @POST
    @Path("/downServer")
    public Response downServer(@Context HttpHeaders hh,String req) {
        return Response.ok().build();
    }

    @POST
    @Path("/upMember")
    public Response upMember(@Context HttpHeaders hh,String req) {
        
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            sc = DefaultSaxParser.parseEntity(Slb.class, slb);
        } else {
            sc = DefaultJsonParser.parse(Slb.class, slb);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/downMember")
    public Response downMember(@Context HttpHeaders hh,String req) {
        return Response.ok().build();
    }
}
