package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.model.waf.RuleFile;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.file.IndexPageService;
import com.ctrip.zeus.support.ObjectJsonParser;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2016/9/1.
 */
@Component
@Path("/indexPage")
public class IndexPageResource {
    @Resource
    private IndexPageService indexPageService;
    @Resource
    private ResponseHandler responseHandler;

    @POST
    @Path("/upload")
    public Response update(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @FormDataParam("page") InputStream page) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = page.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] content = swapStream.toByteArray();
        indexPageService.updateIndexPageFile(content);
        return responseHandler.handle("Update success.", hh.getMediaType());
    }

    @GET
    @Path("/install")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("version") Long version,
                            @QueryParam("slbId") Long slbId) throws Exception {
        if (version == null) {
            version = indexPageService.getMaxIndexPageVersion();
        }
        indexPageService.installIndexPage(slbId, version);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @GET
    @Path("/install/local")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("code") String code,
                            @QueryParam("version") Long version) throws Exception {
        if (version == null) {
            throw new ValidationException("Param version is need.");
        }
        indexPageService.installLocalIndexPage(version);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @POST
    @Path("/install/local")
    public Response postInstall(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("code") String code,
                                @QueryParam("version") Long version,
                                byte[] payload) throws Exception {
        if (version == null || payload == null) {
            throw new ValidationException("Param version Or Request Body are need.");
        }

        indexPageService.installLocalIndexPage(payload, version);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @GET
    @Path("/online/version")
    public Response getErrorPage(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("slbId") Long slbId,
                                 @QueryParam("ip") String ip) throws Exception {
        FileData res = null;
        if (slbId != null) {
            res = indexPageService.getCurrentIndexPage(slbId);
        } else if (ip != null) {
            res = indexPageService.getCurrentIndexPage(ip);
        }
        if (res == null) {
            return responseHandler.handle("Not Found Online Version", hh.getMediaType());
        }
        return responseHandler.handle(new DefaultFile().setVersion(res.getVersion()).setName(res.getKey()).setFile(new String(res.getFileData())), hh.getMediaType());
    }

    @GET
    @Path("/list/current/files")
    public Response getCurrentFiles(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("slbId") Long slbId) throws Exception {

        AssertUtils.assertNotNull(slbId, "slbId not null.");

        List<FileData> slbFiles = indexPageService.listCurrentIndexPage(slbId);
        List<DefaultFile> result = null;
        if (slbFiles != null) {
            result = new ArrayList<>();
            for (FileData slbFile : slbFiles) {
                result.add(new DefaultFile().setName(slbFile.getKey()).setVersion(slbFile.getVersion()).setFile(new String(slbFile.getFileData())));
            }
        }

        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/get/current/file")
    public Response getCurrentFiles(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("fileName") String name,
                                    @QueryParam("ip") String ip) throws Exception {

        AssertUtils.assertNotNull(name, "FileName not null.");
        AssertUtils.assertNotNull(ip, "ip not null.");

        FileData file = indexPageService.getCurrentIndexPage(name, ip);
        DefaultFile result = null;
        if (file != null)
            result = new DefaultFile().setName(file.getKey()).setVersion(file.getVersion()).setFile(new String(file.getFileData()));
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/file/get")
    public Response getFileData(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("version") Long version) throws Exception {

        AssertUtils.assertNotNull(version, "Version not null.");

        byte[] data = indexPageService.getIndexPage(version);
        if (data == null) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_OCTET_STREAM).build();
        }
        return Response.ok(data).build();
    }


    @GET
    @Path("/get")
    public Response getIndexFile(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("version") Long version) throws Exception {
        DefaultFile res = new DefaultFile();
        AssertUtils.assertNotNull(version, "version not null.");
        byte[] data = indexPageService.getIndexPage(version);
        if (data == null) {
            throw new NotFoundException("Not Found File Data By Name And Version.");
        }
        res.setName("index").setVersion(version).setFile(new String(data));
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/getVersion")
    public Response getVersion(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("ip") String ip,
                               @QueryParam("slbId") Long slbId) throws Exception {

        AssertUtils.assertNotNull(ip, "ip is null.");
        AssertUtils.assertNotNull(slbId, "slbId is null.");
        FileData file = indexPageService.getCurrentIndexPage(slbId);
        FileData serverFile = indexPageService.getCurrentIndexPage(ip);
        Map<String, DefaultFile> data = new HashMap<>();
        if(file!=null){
            data.put("slb", new DefaultFile().setFile(new String(file.getFileData())).setVersion(file.getVersion()).setName(file.getKey()));
        }
        if(serverFile!=null){
            data.put("slb", new DefaultFile().setFile(new String(serverFile.getFileData())).setVersion(serverFile.getVersion()).setName(serverFile.getKey()));
        }
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
        indexPageService.updateFileStatus(ip, version);
        return responseHandler.handle("success", hh.getMediaType());
    }

}
