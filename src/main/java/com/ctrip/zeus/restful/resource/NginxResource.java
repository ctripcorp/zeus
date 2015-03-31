package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.nginx.NginxResponse;
import com.ctrip.zeus.nginx.NginxServerStatus;
import com.ctrip.zeus.service.SlbException;
import com.ctrip.zeus.service.nginx.NginxService;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

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
    public Response load() {
        try {
            NginxResponse result = nginxService.load();
            if (result.isSucceed()) {
                return Response.ok(result.toJson()).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                return Response.serverError().entity(result.toJson()).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        } catch (SlbException e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/status")
    @Produces({"*/*"})
    public Response status() {
        try {
            NginxServerStatus status = nginxService.getStatus();

            return Response.ok(status.toJson()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/loadAll")
    @Produces({"*/*"})
    public Response loadAll() {
        try {
            List<NginxResponse> result = nginxService.loadAll();
            Gson gson = new Gson();
            return Response.ok(gson.toJson(result)).type(MediaType.APPLICATION_JSON_TYPE).build();
        }catch (Exception e){
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/allStatus")
    @Produces({"*/*"})
    public Response allStatus() throws IOException {
        try {
            List<NginxServerStatus> result = nginxService.getStatusAll();
            Gson gson = new Gson();
            return Response.ok(gson.toJson(result)).type(MediaType.APPLICATION_JSON_TYPE).build();
        }catch (Exception e){
            return Response.serverError().build();
        }
    }
}
