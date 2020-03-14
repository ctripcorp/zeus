package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.domain.ClusterInfo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.healthcheck.HealthCheckService;
import com.ctrip.zeus.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/hc")
public class HealthCheckerResource {
    @Resource
    private AuthService authService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private HealthCheckService healthCheckService;

    /***
     * Health Checker Register To Slb Api
     * Return All Members In Special Cluster.
     * Health Checker Should Register In Every 1 Hour.Otherwise, This Member Will Be Removed From Cluster.
     * @param hh
     * @param request
     * @param cluster
     * @param ip
     * @return
     * @throws Exception
     */
    @GET
    @Path("/register")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response register(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request,
                         @QueryParam("cluster") String cluster,
                         @QueryParam("ip") String ip) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Group, AuthDefaultValues.ALL);
        assert (cluster != null);
        assert (ip != null);
        return responseHandler.handleSerializedValue(healthCheckService.register(ip,cluster), hh.getMediaType());
    }

    @GET
    @Path("/properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response properties(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request,
                         @QueryParam("cluster") String cluster) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Group, AuthDefaultValues.ALL);
        assert (cluster != null);
        return responseHandler.handleSerializedValue(healthCheckService.properties(cluster), hh.getMediaType());
    }

    @GET
    @Path("/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response checkSlbs(@Context HttpHeaders hh,
                               @Context final HttpServletRequest request,
                               @QueryParam("cluster") String cluster) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Group, AuthDefaultValues.ALL);
        assert (cluster != null);
        return responseHandler.handle(healthCheckService.checkSlbIds(cluster), hh.getMediaType());
    }

    @GET
    @Path("/list")
    public Response getCluster(@Context HttpServletRequest request,
                               @Context HttpHeaders headers) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.READ, ResourceDataType.Group, AuthDefaultValues.ALL);

        List<ClusterInfo> clusterInfos = healthCheckService.list();

        return responseHandler.handle(clusterInfos, headers.getMediaType());
    }


    @POST
    @Path("/properties/update")
    public Response updateProperties(@Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     ClusterInfo clusterInfo) throws Exception {
        if (clusterInfo == null) {
            throw new ValidationException("Invalid post body. See ClusterInfo");
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Group, AuthDefaultValues.ALL);

        healthCheckService.updateProperties(clusterInfo);

        return responseHandler.handle(healthCheckService.getClusterInfoByName(clusterInfo.getName()), headers.getMediaType());
    }

}
