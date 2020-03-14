package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.update.SlbServerConfManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by fanqq on 2016/3/16.
 */
@Component
@Path("/update")
public class UpdateResource {
    @Resource
    private SlbServerConfManager slbServerConfManager;
    @Resource
    private ResponseHandler responseHandler;
    //TODO
    @GET
    @Path("/conf")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response logs(@Context HttpServletRequest request,
                         @Context HttpHeaders hh,
                         @QueryParam("refresh") Boolean refresh,
                         @QueryParam("reload") Boolean reload) throws Exception {
        if (refresh == null) {
            refresh = false;
        }
        NginxResponse response = slbServerConfManager.update(refresh, reload);
        return responseHandler.handle(response, hh.getMediaType());
    }
}
