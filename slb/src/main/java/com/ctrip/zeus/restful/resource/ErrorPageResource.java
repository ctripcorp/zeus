package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.model.waf.RuleFile;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.file.ErrorPageService;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.AssertUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
 * Created by fanqq on 2016/8/22.
 */
@Component
@Path("/installErrorPage")
public class ErrorPageResource {
    @Resource
    ErrorPageService errorPageService;
    @Resource
    ResponseHandler responseHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // CALLED BY PORTAL
    @POST
    @Path("/upload")
    public Response upload(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @QueryParam("code") String code,
                           @FormDataParam("page") InputStream page) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = page.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] content = swapStream.toByteArray();
        errorPageService.updateErrorPageFile(code, content);
        return responseHandler.handle("Upload success.", hh.getMediaType());
    }

    @GET
    @Path("/install")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("code") String code,
                            @QueryParam("version") Long version,
                            @QueryParam("slbId") Long slbId) throws Exception {
        if (version == null) {
            version = errorPageService.getMaxErrorPageVersion(code);
        }
        if (errorPageService.installErrorPage(slbId, code, version)) {
            return responseHandler.handle("Install success.", hh.getMediaType());
        } else {
            throw new RuntimeException("Failed to install error pages on some slb servers");
        }
    }

    @GET
    @Path("/online/version")
    public Response getErrorPage(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("code") String code,
                                 @QueryParam("slbId") Long slbId,
                                 @QueryParam("ip") String ip) throws Exception {
        FileData file = null;
        Assert.notNull(code, "code shall not be null");
        if (slbId != null) {
            file = errorPageService.getCurrentErrorPage(code, slbId);
        } else if (ip != null) {
            file = errorPageService.getCurrentErrorPage(code, ip);
        }
        if (file == null) {
            return responseHandler.handle("Not Found Online Version", hh.getMediaType());
        }
        return responseHandler.handle(new DefaultFile().setFile(new String(file.getFileData())).setName(file.getKey()).setVersion(file.getVersion()), hh.getMediaType());
    }

    @GET
    @Path("/get")
    public Response getErrorPageFile(@Context HttpServletRequest request,
                                     @Context HttpHeaders hh,
                                     @QueryParam("code") String fileName,
                                     @QueryParam("version") Long version) throws Exception {
        DefaultFile res = new DefaultFile();
        AssertUtils.assertNotNull(fileName, "fileName is null.");
        AssertUtils.assertNotNull(version, "version not null.");
        byte[] data = errorPageService.getErrorPage(fileName, version);
        if (data == null) {
            throw new NotFoundException("Not Found File Data By Name And Version.");
        }
        res.setName(fileName).setVersion(version).setFile(new String(data));
        return responseHandler.handle(res, hh.getMediaType());
    }

    @GET
    @Path("/getVersion")
    public Response getVersion(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("code") String code,
                               @QueryParam("ip") String ip,
                               @QueryParam("slbId") Long slbId) throws Exception {

        AssertUtils.assertNotNull(code, "fileName is null.");
        AssertUtils.assertNotNull(ip, "ip is null.");
        AssertUtils.assertNotNull(slbId, "slbId is null.");
        FileData file = errorPageService.getCurrentErrorPage(code, slbId);
        FileData serverFile = errorPageService.getCurrentErrorPage(code, ip);
        Map<String, DefaultFile> data = new HashMap<>();
        if (file != null) {
            data.put("slb", new DefaultFile().setName(file.getKey()).setVersion(file.getVersion()).setFile(new String(file.getFileData())));
        }
        if (serverFile != null) {
            data.put("slbServer", new DefaultFile().setName(serverFile.getKey()).setVersion(serverFile.getVersion()).setFile(new String(serverFile.getFileData())));
        }
        return responseHandler.handle(data, hh.getMediaType());
    }

    @GET
    @Path("/set/status")
    public Response setServerFileStatus(@Context HttpServletRequest request,
                                        @Context HttpHeaders hh,
                                        @QueryParam("ip") String ip,
                                        @QueryParam("version") Long version,
                                        @QueryParam("code") String code) throws Exception {
        AssertUtils.assertNotNull(ip, "ip is null.");
        AssertUtils.assertNotNull(version, "version is null.");
        AssertUtils.assertNotNull(code, "code is null.");
        errorPageService.updateFileStatus(code, ip, version);
        return responseHandler.handle("success", hh.getMediaType());
    }


    // CALLED BY AGENT

    // to be removed
    @POST
    @Path("/install/local")
    public Response postInstall(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("code") String code,
                                String str) throws Exception {
        RuleFile data = ObjectJsonParser.parse(str, RuleFile.class);
        if (code == null || data == null) {
            throw new ValidationException("Param version Or Post Boyd are needed.");
        }
        errorPageService.installLocalErrorPage(data.getContent().getBytes("UTF-8"), code);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }


    @POST
    @Path("/new/install/local")
    public Response newPostInstall(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("code") String code,
                                byte[] payload) throws Exception {
        if (code == null || payload == null) {
            throw new ValidationException("Param version Or Post Boyd are needed.");
        }
        errorPageService.installLocalErrorPage(payload, code);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @GET
    @Path("/get/files")
    public Response getSlbErrorPageFiles(@Context HttpServletRequest request,
                                         @Context HttpHeaders hh,
                                         @QueryParam("slbId") Long slbId) throws Exception {
        AssertUtils.assertNotNull(slbId, "slbId is null.");
        List<FileData> slbFiles = errorPageService.getCurrentFiles(slbId);

        List<DefaultFile> defaultFiles = null;
        if (slbFiles != null) {
            defaultFiles = new ArrayList<>();
            for (FileData c : slbFiles) {
                defaultFiles.add(new DefaultFile().setFile(new String(c.getFileData())).setName(c.getKey()).setVersion(c.getVersion()));
            }
        }

        return responseHandler.handle(defaultFiles, hh.getMediaType());
    }


    @GET
    @Path("/file/get")
    public Response getFileData(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("code") String code,
                                @QueryParam("version") Long version) throws Exception {
        if (code == null || version == null) throw new ValidationException("Code and fileName need.");
        byte[] data = errorPageService.getErrorPage(code, version);

        if (data == null) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_OCTET_STREAM).build();
        }
        return Response.ok(data).build();
    }

    @GET
    @Path("/slb/list")
    public Response getSlbDataFiles(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId is need.");

        List<FileData> slbFiles = errorPageService.getCurrentFiles(slbId);

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
    @Path("/ip/get")
    public Response getIpDataFile(@Context HttpServletRequest request,
                                  @Context HttpHeaders hh,
                                  @QueryParam("code") String code,
                                  @QueryParam("ip") String ip) throws Exception {
        if (ip == null || code == null) throw new ValidationException("ip and fileName need.");
        FileData file = errorPageService.getCurrentErrorPage(code, ip);

        DefaultFile result = null;
        if (file != null)
            result = new DefaultFile().setName(file.getKey()).setVersion(file.getVersion()).setFile(new String(file.getFileData()));
        return responseHandler.handle(result, hh.getMediaType());
    }

}
