package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.client.LocalNginxConfClientManager;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.model.nginx.SlbConfResponse;
import com.ctrip.zeus.model.nginx.VirtualServerConfResponse;
import com.ctrip.zeus.nginx.LocalSlbConfResponse;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.service.update.AgentApiRefactorSwitches;
import com.ctrip.zeus.util.UserUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private AuthService authService;
    @Resource
    private LocalNginxConfClientManager localNginxConfClientManager;
    @Resource
    private AgentApiRefactorSwitches agentApiRefactorSwitches;

    @GET
    @Path("/conf")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVsConf(@Context HttpServletRequest request, @Context HttpHeaders hh,
                              @QueryParam("vsId") Long vsId, @QueryParam("slbId") Long slbId, @QueryParam("version") Integer versionNum) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE, ResourceDataType.Conf, AuthDefaultValues.ALL);

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
        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
            VirtualServerConfResponse response = localNginxConfClientManager.getVsConf(slbId, vsId);
            return responseHandler.handle(response, hh.getMediaType());
        }
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

    @GET
    @Path("/nginxconf")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVsConf(@Context HttpServletRequest request, @Context HttpHeaders hh,
                              @QueryParam("slbId") Long slbId, @QueryParam("version") Integer versionNum) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE, ResourceDataType.Conf, AuthDefaultValues.ALL);

        if (slbId == null) {
            throw new ValidationException("Query slbId is required.");
        }

        int version;
        if (null == versionNum || versionNum <= 0) {
            version = nginxConfService.getCurrentVersion(slbId);
        } else {
            version = versionNum;
        }
        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
            String nginxConf = localNginxConfClientManager.getNginxConf(slbId);
            return responseHandler.handle(nginxConf, hh.getMediaType());
        }

        String nginxConf = nginxConfService.getNginxConf(slbId, (long) version);

        return responseHandler.handle(nginxConf, hh.getMediaType());
    }

    @GET
    @Path("/slbconf")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSlbConf(@Context HttpServletRequest request, @Context HttpHeaders hh,
            @QueryParam("slbId") Long slbId, @QueryParam("version") Integer versionNum) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE,
                ResourceDataType.Conf, AuthDefaultValues.ALL);

        if (slbId == null) {
            throw new ValidationException("slbId is required.");
        }

        int version;
        if (null == versionNum || versionNum <= 0) {
            version = nginxConfService.getCurrentVersion(slbId);
        } else {
            version = versionNum;
        }
        if (agentApiRefactorSwitches.isSwitchOn(slbId)) {
            LocalSlbConfResponse response = localNginxConfClientManager.getSlbConf(slbId);
            if (response != null) {
                return responseHandler.handle(response.getSlbConfResponse(), hh.getMediaType());
            }
        }

        String nginxConf = nginxConfService.getNginxConf(slbId, (long) version);
        NginxConfEntry confEntry = nginxConfService.getUpstreamsAndVhosts(slbId, (long) version);

        SlbConfResponse response = new SlbConfResponse();
        response.setSlbId(slbId).setVersion(version);
        response.setNginxConf(nginxConf);
        response.setVhosts(confEntry.getVhosts());
        response.setUpstreams(confEntry.getUpstreams());
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/allslbconfs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSlbConfFromAllServers(
            @Context HttpHeaders hh,
            @Context HttpServletRequest request,
            @QueryParam("slbId") Long slbId)
            throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE, ResourceDataType.Conf, AuthDefaultValues.ALL);

        if (slbId == null) {
            throw new ValidationException("SlbId is required");
        }

        Map<String, SlbConfResponse> ipResponseMap = localNginxConfClientManager.getSlbConfFromAllServers(slbId);
        return responseHandler.handle(ipResponseMap, hh.getMediaType());
    }
}
