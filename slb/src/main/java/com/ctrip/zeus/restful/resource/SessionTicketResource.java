package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.file.Constants;
import com.ctrip.zeus.service.file.SessionTicketService;
import com.ctrip.zeus.util.AssertUtils;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
    @Path("/get/data/files")
    public Response getDataFiles(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("slbId") Long slbId,
                                 @QueryParam("ip") String ip) throws Exception {
        if (slbId == null || ip == null) throw new ValidationException("SlbId and ip need.");


        FileData slbFile = sessionTicketService.getCurrentSessionTicketFile(slbId);
        FileData ipFile = sessionTicketService.getCurrentSessionTicketFile(ip);

        Map<String, DefaultFile> files = new HashMap<>();
        if(slbFile==null){
            files.put("slb",null);
        }else{
            files.put("slb", new DefaultFile().setName(slbFile.getKey()).setVersion(slbFile.getVersion()).setFile(new String(slbFile.getFileData())));
        }
        if(ipFile==null){
            files.put("ip",null);
        }else{
            files.put("ip", new DefaultFile().setName(ipFile.getKey()).setVersion(ipFile.getVersion()).setFile(new String(ipFile.getFileData())));
        }

        return responseHandler.handle(files, hh.getMediaType());
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
        if (sessionTicketService.installSessionTicketFile(slbId, version)) {
            return responseHandler.handle("Install success.", hh.getMediaType());
        } else {
            throw new NginxProcessingException("Failed to install session ticket file");
        }
    }

    @POST
    @Path("/install/local")
    public Response postInstall(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                byte[] payload) throws Exception {
        if (payload == null || payload.length == 0) {
            throw new ValidationException("Param, Body are need.");
        }
        if (sessionTicketService.installLocalSessionTicketFile(payload)) {
            return responseHandler.handle("Install success.", hh.getMediaType());
        } else {
            throw new NginxProcessingException("Failed to install");
        }
    }


    @GET
    @Path("/online/version")
    public Response getSessionTicketPage(@Context HttpServletRequest request,
                                         @Context HttpHeaders hh,
                                         @QueryParam("slbId") Long slbId,
                                         @QueryParam("ip") String ip) throws Exception {
        FileData f = null;
        if (slbId != null) {
            f = sessionTicketService.getCurrentSessionTicketFile(slbId);
        } else if (ip != null) {
            f = sessionTicketService.getCurrentSessionTicketFile(ip);
        }
        if (f == null) {
            return responseHandler.handle("Not Found Online Version", hh.getMediaType());
        }

        return responseHandler.handle(new DefaultFile().setName(f.getKey()).setVersion(f.getVersion()).setFile(new String((f.getFileData()))), hh.getMediaType());
    }


    @GET
    @Path("/get")
    public Response getLuaFile(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("version") Long version) throws Exception {
        DefaultFile res = new DefaultFile();
        AssertUtils.assertNotNull(version, "version not null.");
        byte[] data = sessionTicketService.getFile(version);
        if (data == null) {
            throw new NotFoundException("Not Found File Data By Name And Version.");
        }
        res.setName("index").setVersion(version).setFile(new String(data));
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/get/file")
    public Response getSessionTicketFile(@Context HttpServletRequest request,
                                         @Context HttpHeaders hh,
                                         @QueryParam("version") Long version) throws Exception {
        AssertUtils.assertNotNull(version, "version not null.");
        byte[] data = sessionTicketService.getFile(version);
        if (data == null) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_OCTET_STREAM).build();
        }
        return Response.ok(data).build();
    }

    @GET
    @Path("/getVersion")
    public Response getVersion(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("ip") String ip,
                               @QueryParam("slbId") Long slbId) throws Exception {

        AssertUtils.assertNotNull(ip, "ip is null.");
        AssertUtils.assertNotNull(slbId, "slbId is null.");
        FileData file = sessionTicketService.getCurrentSessionTicketFile(slbId);
        FileData serverFile = sessionTicketService.getCurrentSessionTicketFile(ip);

        Map<String, DefaultFile> data = new HashMap<>();
        data.put("slb", new DefaultFile().setFile(new String(file.getFileData())).setName(file.getKey()).setVersion(file.getVersion()));
        data.put("slbServer", new DefaultFile().setFile(new String(serverFile.getFileData())).setName(serverFile.getKey()).setVersion(serverFile.getVersion()));
        return responseHandler.handle(data, hh.getMediaType());
    }


    @GET
    @Path("/set/status")
    public Response setServerFileStatus(@Context HttpServletRequest request,
                                        @Context HttpHeaders hh,
                                        @QueryParam("ip") String ip,
                                        @QueryParam("version") Long version) throws Exception {
        AssertUtils.assertNotNull(ip, "ip is null.");
        AssertUtils.assertNotNull(version, "version is null.");
        sessionTicketService.updateFileStatus(ip, version);
        return responseHandler.handle(Constants.SETSTATUSSUCCESSMSG, hh.getMediaType());
    }
}
