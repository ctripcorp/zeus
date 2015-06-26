package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.SlbValidateResponse;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.validate.SlbValidateLocal;
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
 * Created by fanqq on 2015/6/26.
 */
@Component
@Path("/validate")
public class ValidateResource {
    @Resource
    private SlbValidateLocal slbValidateLocal;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "slbValidate", uriGroupHint = -1)
    public Response slbValidate(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                        @QueryParam("slbId") Long slbId,
                                        @QueryParam("slbName") String slbName) throws Exception {
        if (slbId == null || slbId <= 0)
        {
            throw new ValidationException("Error Param!slbId Can not be Null!");
        }
        SlbValidateResponse response = slbValidateLocal.validate(slbId);
        return responseHandler.handle(response, hh.getMediaType());
    }
}
