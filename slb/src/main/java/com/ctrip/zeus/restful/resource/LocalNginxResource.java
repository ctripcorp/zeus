package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.nginx.*;
import com.ctrip.zeus.nginx.LocalSlbConfResponse;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.nginx.LocalNginxService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @Discription
 **/
@Component
@Path("/nginx/local")
public class LocalNginxResource {

    @Resource
    private LocalNginxService localNginxService;
    @Resource
    private ResponseHandler responseHandler;

    /*
     * @Description return local nginx.conf file stored in disk
     * @return
     **/
    @Path("/nginxconf")
    @GET
    public Response getNginxConf(@Context HttpHeaders hh, @Context HttpServletRequest request)
            throws Exception {
        String nginxConf = localNginxService.getNginxConf();
        return responseHandler.handle(nginxConf, hh.getMediaType());
    }

    /*
     * @Description return all conf files belonged to the vs specified by vsId
     * @return
     **/
    @GET
    @Path("/vsconf")
    public Response getVsConf(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("vsId") Long vsId)
            throws Exception {
        if (vsId == null || vsId <= 0) {
            throw new ValidationException("Vs Id not valid");
        }

        VirtualServerConfResponse response = new VirtualServerConfResponse();
        response.setServerConf(localNginxService.getVsConf(vsId));
        Map<String, String> contentsMap = localNginxService.getUpstreamConfs(vsId);
        StringBuilder stringBuilder = new StringBuilder();
        for (String content : contentsMap.values()) {
            stringBuilder.append(content);
        }
        response.setUpstreamConf(stringBuilder.toString());
        response.setVirtualServerId(vsId);
        return responseHandler.handle(response, hh.getMediaType());
    }

    /*
     * @Description return all vs confs and upstrean conf files local disk
     * @return
     **/
    @GET
    @Path("/slbconf")
    public Response getSlbConf(@Context HttpHeaders hh, @Context HttpServletRequest request)
            throws Exception {
        SlbConfResponse response = new SlbConfResponse();

        response.setNginxConf(localNginxService.getNginxConf());
        Vhosts vhosts = new Vhosts();
        Upstreams upstreams = new Upstreams();

        for (Long vsId : localNginxService.getAllVsIds()) {
            String name = "" + vsId + ".conf";
            String content = localNginxService.getVsConf(vsId);
            ConfFile vsConfFile = new ConfFile().setName(name).setContent(content);
            vhosts.addConfFile(vsConfFile);

            Map<String, String> upstreamFiles = localNginxService.getUpstreamConfs(vsId);
            for (Map.Entry<String, String> entry : upstreamFiles.entrySet()) {
                upstreams.addConfFile(new ConfFile().setName(entry.getKey()).setContent(entry.getValue()));
            }
        }
        response.setVhosts(vhosts);
        response.setUpstreams(upstreams);

        LocalSlbConfResponse localSlbConfResponse = new LocalSlbConfResponse();
        localSlbConfResponse.setIp(LocalInfoPack.INSTANCE.getIp());
        localSlbConfResponse.setSlbConfResponse(response);
        return responseHandler.handle(localSlbConfResponse, hh.getMediaType());
    }
}
