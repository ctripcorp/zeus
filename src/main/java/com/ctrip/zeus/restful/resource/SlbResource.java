package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.service.SlbRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.List;

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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response list() {
        slbRepository.list();
        return Response.ok("hello").build();
    }

    @GET
    @Path("/{slbName:[a-zA-Z0-9_-]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response get(@Context HttpHeaders hh, @PathParam("slbName") String slbName) {
        List<Slb> list = slbRepository.list();
        for (Slb slb : list) {
            if (slbName.equals(slb.getName())) {
                if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
                    return Response.status(200).entity(String.format(Slb.XML, slb)).type(MediaType.APPLICATION_XML).build();
                }else{
                    return Response.status(200).entity(String.format(Slb.JSON, slb)).type(MediaType.APPLICATION_JSON).build();
                }
            }
        }
        return Response.status(404).type(hh.getMediaType()).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(String slb) {
        try {
            Slb sc = DefaultJsonParser.parse(Slb.class, slb);
            slbRepository.add(sc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }
}
