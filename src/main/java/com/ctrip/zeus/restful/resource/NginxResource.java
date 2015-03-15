package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.service.nginx.NginxService;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import javax.ws.rs.GET;
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
@Path("/nginx")
public class NginxResource {

    @Resource
    private NginxService nginxService;

    @GET
    @Path("/load")
    @Produces({"*/*"})
    public Response load() throws IOException, DalException {
        String result = nginxService.load();
        return Response.ok(result).type(MediaType.TEXT_PLAIN).build();
    }
}
