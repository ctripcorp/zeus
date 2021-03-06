package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.lock.LockService;
import com.ctrip.zeus.model.lock.LockList;
import com.ctrip.zeus.model.lock.LockStatus;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by zhoumy on 2015/4/16.
 */
@Component
@Path("/lock")
public class LockResource {
    @Resource
    private LockService lockService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private AuthService authService;

    @GET
    @Path("/status")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStatus(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        LockList ll = new LockList();
        for (LockStatus ls : lockService.getLockStatus()) {
            ll.addLockStatus(ls);
        }
        ll.setTotal(ll.getLocks().size());
        return responseHandler.handle(ll, hh.getMediaType());
    }

    @GET
    @Path("/unlock/{key}")
    public Response forceUnlock(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("key") String key) throws Exception {
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.MAINTENANCE, ResourceDataType.Lock, AuthDefaultValues.ALL);
        lockService.forceUnlock(key);
        return responseHandler.handle("Unlock succeeded.", hh.getMediaType());
    }
}