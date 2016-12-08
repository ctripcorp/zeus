package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/nginx")
public class NginxResource {
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @GET
    @Path("/conf")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVsConf(@Context HttpServletRequest request, @Context HttpHeaders hh,
                              @QueryParam("vsId") Long vsId, @QueryParam("slbId") Long slbId, @QueryParam("version") Integer versionNum) throws Exception {

        if (vsId == null || slbId == null) {
            throw new ValidationException("Query vsId and slbId are required.");
        }

        IdVersion[] key = virtualServerCriteriaQuery.queryByIdAndMode(vsId, SelectionMode.ONLINE_EXCLUSIVE);
        if (key.length == 0) {
            throw new ValidationException("Cannot find activated version of vs-" + vsId + ".");
        }

        int version;
        if (null == versionNum || versionNum <= 0) {
            version = nginxConfService.getCurrentVersion(slbId);
        } else {
            version = versionNum;
        }
        List<Long> vsArray = new ArrayList<>();
        vsArray.add(vsId);
        NginxConfEntry confEntry = nginxConfService.getUpstreamsAndVhosts(slbId, (long) version, vsArray);
        StringBuilder stringBuilder = new StringBuilder();
        for (ConfFile cf : confEntry.getVhosts().getFiles()) {
            stringBuilder.append(cf.getContent());
        }

        VirtualServerConfResponse response = new VirtualServerConfResponse();
        response.setServerConf(stringBuilder.toString());
        stringBuilder.setLength(0);
        for (ConfFile cf : confEntry.getUpstreams().getFiles()) {
            stringBuilder.append(cf.getContent());
        }
        response.setUpstreamConf(stringBuilder.toString());
        response.setVersion(version).setVirtualServerId(vsId);
        return responseHandler.handle(response, hh.getMediaType());
    }
}
