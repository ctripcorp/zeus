package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.MemberAction;
import com.ctrip.zeus.model.entity.ServerAction;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Component
@Path("/server")
public class ServerResource {

    @POST
    @Path("/upServer")
    public Response upServer(@Context HttpHeaders hh,String req) throws IOException, SAXException {
        ServerAction a = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(ServerAction.class, req);
        } else {
            a = DefaultJsonParser.parse(ServerAction.class, req);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/downServer")
    public Response downServer(@Context HttpHeaders hh,String req) throws IOException, SAXException {
        ServerAction a = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(ServerAction.class, req);
        } else {
            a = DefaultJsonParser.parse(ServerAction.class, req);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/upMember")
    public Response upMember(@Context HttpHeaders hh,String req) throws IOException, SAXException {
        MemberAction a = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(MemberAction.class, req);
        } else {
            a = DefaultJsonParser.parse(MemberAction.class, req);
        }

        return Response.ok().build();
    }

    @POST
    @Path("/downMember")
    public Response downMember(@Context HttpHeaders hh,String req) throws IOException, SAXException {
        MemberAction a = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            a = DefaultSaxParser.parseEntity(MemberAction.class, req);
        } else {
            a = DefaultJsonParser.parse(MemberAction.class, req);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/info")
    public Response info(@Context HttpHeaders hh) throws IOException, SAXException {
        MemberAction a = new MemberAction().setAppName("app001")
                .addIp("192.168.1.1")
                .addIp("192.168.1.2");

        ServerAction a2 = new ServerAction()
                .addIp("192.168.1.1")
                .addIp("192.168.1.2");

        return Response.ok(String.format(MemberAction.JSON, a) + "\n\n\n" + String.format(ServerAction.JSON, a2)).build();
    }
}
