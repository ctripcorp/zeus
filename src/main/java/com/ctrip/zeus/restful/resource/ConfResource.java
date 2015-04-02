package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.service.conf.ConfService;
import com.ctrip.zeus.service.nginx.NginxService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.GET;
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
@Path("/confbak")
public class ConfResource {

    @Resource
    private ConfService confService;
    @Resource
    private NginxService nginxAgentService;

    @GET
    @Path("/activate")
    public Response list(@Context HttpHeaders hh,@QueryParam("slbName") List<String> slbNames,  @QueryParam("appName") List<String> appNames) {
        confService.activate(slbNames, appNames);
        reloadNginxConf();
        return Response.ok().build();
    }

    private void reloadNginxConf() {
        try {
            nginxAgentService.loadAll("default");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
