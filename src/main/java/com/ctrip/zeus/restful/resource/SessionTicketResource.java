package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.page.entity.DefaultFile;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.file.SessionTicketService;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by fanqq on 2017/3/2.
 */
@Component
@Path("/session/ticket/key")
public class SessionTicketResource {
    @Resource
    ResponseHandler responseHandler;
    @Resource
    SessionTicketService sessionTicketService;

    @POST
    @Path("/upload")
    public Response update(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @FormDataParam("key") InputStream page) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = page.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] content = swapStream.toByteArray();
        sessionTicketService.addSessionTicketFile(content);
        return responseHandler.handle("Update success.", hh.getMediaType());
    }

    @GET
    @Path("/install")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("version") Long version,
                            @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId is need.");

        if (version == null) {
            version = sessionTicketService.getMaxVersion(slbId);
        }
        sessionTicketService.installSessionTicketFile(slbId, version);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @GET
    @Path("/install/local")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("version") Long version) throws Exception {
        if (version == null) {
            throw new ValidationException("Param version is need.");
        }
        sessionTicketService.installLocalSessionTicketFile(version);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @GET
    @Path("/online/version")
    public Response getErrorPage(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("slbId") Long slbId,
                                 @QueryParam("ip") String ip) throws Exception {
        DefaultFile res = null;
        if (slbId != null) {
            res = sessionTicketService.getCurrentSessionTicketFile(slbId);
        } else if (ip != null) {
            res = sessionTicketService.getCurrentSessionTicketFile(ip);
        }
        if (res == null) {
            return responseHandler.handle("Not Found Online Version", hh.getMediaType());
        }
        return responseHandler.handle(res, hh.getMediaType());
    }
}
