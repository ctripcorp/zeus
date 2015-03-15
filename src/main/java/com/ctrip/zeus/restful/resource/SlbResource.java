package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.SlbRepository;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/slb")
public class SlbResource {
    @Resource
    private SlbRepository slbRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response list(@Context HttpHeaders hh) {
        SlbList slbList = slbRepository.list();
        if (hh.getAcceptableMediaTypes().contains(MediaType.APPLICATION_XML_TYPE)) {
            return Response.status(200).entity(String.format(SlbList.XML, slbList)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(SlbList.JSON, slbList)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/{slbName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response get(@Context HttpHeaders hh, @PathParam("slbName") String slbName) {
        Slb slb = slbRepository.get(slbName);
        if (slb.getName() != null) {
            if (hh.getAcceptableMediaTypes().contains(MediaType.APPLICATION_XML_TYPE)) {
                return Response.status(200).entity(String.format(Slb.XML, slb)).type(MediaType.APPLICATION_XML).build();
            } else {
                return Response.status(200).entity(String.format(Slb.JSON, slb)).type(MediaType.APPLICATION_JSON).build();
            }
        }
        return Response.status(404).type(hh.getMediaType()).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response addOrUpdate(@Context HttpHeaders hh, String slb) throws IOException, SAXException {
        Slb sc = null;
        if (hh.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)) {
            sc = DefaultSaxParser.parseEntity(Slb.class, slb);
        } else {
            sc = DefaultJsonParser.parse(Slb.class, slb);
        }
        slbRepository.addOrUpdate(sc);
        return Response.ok().build();
    }
}
