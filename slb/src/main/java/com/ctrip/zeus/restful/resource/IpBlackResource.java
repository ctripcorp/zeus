package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.ipblock.BlackIpListEntity;
import com.ctrip.zeus.service.ipblock.IpBlackListService;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Component
@Path("/ip/blacklist")
public class IpBlackResource {
    @Resource
    private AuthService authService;
    @Resource
    private IpBlackListService ipBlackListService;
    @Resource
    ResponseHandler responseHandler;

    @POST
    @Path("/set")
    public Response update(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           String data) throws Exception {
        AssertUtils.assertNotNull(data, "Data should not be null");
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.BLACK_LIST, ResourceDataType.Ip, AuthDefaultValues.ALL);
        BlackIpListEntity entity = ObjectJsonParser.parse(data, BlackIpListEntity.class);
        AssertUtils.assertNotNull(entity, "Invalidate Data.");
        return responseHandler.handle(ipBlackListService.setIpBlackList(entity), hh.getMediaType());
    }


    @POST
    @Path("/localUpdate")
    public Response localUpdate(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                String data) throws Exception {
        ipBlackListService.setLocalIpBlackList(data);
        return responseHandler.handle("Success", hh.getMediaType());
    }
}
