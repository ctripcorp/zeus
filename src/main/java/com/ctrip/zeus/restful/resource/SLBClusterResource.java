package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.entity.SlbCluster;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.service.SlbClusterRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/slb")
public class SLBClusterResource {
    @Resource
    private SlbClusterRepository slbClusterRepository;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response list() {
        slbClusterRepository.list();
        return Response.ok("hello").build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    public Response add(String slb) {
        try {
            SlbCluster sc = DefaultJsonParser.parse(SlbCluster.class, slb);
            slbClusterRepository.add(sc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok("hello").build();
    }
}
