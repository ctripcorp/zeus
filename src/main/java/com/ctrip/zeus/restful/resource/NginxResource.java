package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.service.NginxService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/nginx")
public class NginxResource {

    @Resource
    private NginxService nginxService;

    @Path("/load")
    @Produces({"*/*"})
    public Response list() {
        nginxService.load();
        return Response.ok("hello").build();
    }
}
