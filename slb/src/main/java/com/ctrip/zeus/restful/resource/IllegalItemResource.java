package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.verify.IllegalDataUnit;
import com.ctrip.zeus.service.verify.VerifyManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * @Discription
 **/
@Component
@Path("/illegal")
public class IllegalItemResource {

    @Resource
    private VerifyManager verifyManager;

    @Resource
    private ResponseHandler responseHandler;

    @GET
    public Response getItemIds(
            @QueryParam("type") List<String> types,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest request) throws Exception {
        Map<String, List<IllegalDataUnit>> illegalData = verifyManager.getIllegalData(types);
        return responseHandler.handle(illegalData, httpHeaders.getMediaType());
    }
}
