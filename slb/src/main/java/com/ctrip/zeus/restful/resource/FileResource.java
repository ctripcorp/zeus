package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.model.waf.RuleFile;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.file.FileSysService;
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
 * Created by fanqq on 2017/10/17.
 */
@Component
@Path("/file")
public class FileResource {

    @Resource
    FileSysService fileSysService;
    @Resource
    ResponseHandler responseHandler;

    @POST
    @Path("/upload")
    public Response upload(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @QueryParam("fileName") String name,
                           @FormDataParam("file") InputStream file) throws Exception {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = file.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] content = swapStream.toByteArray();
        fileSysService.updateFile(name, content);
        return responseHandler.handle("upload success.", hh.getMediaType());
    }

    @GET
    @Path("/install")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("fileName") String fileName,
                            @QueryParam("version") Long version,
                            @QueryParam("slbId") Long slbId) throws Exception {
        if (version == null) {
            version = fileSysService.getMaxFileVersion(fileName);
        }
        if (fileSysService.installFile(slbId, fileName, version)) {
            return responseHandler.handle("Install success.", hh.getMediaType());
        } else {
            throw new NginxProcessingException("Failed to install or set status of file status");
        }
    }

    // to be removed
    @POST
    @Path("/install/local")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("fileName") String fileName,
                            String str) throws Exception {
        RuleFile data = ObjectJsonParser.parse(str, RuleFile.class);

        if (fileName == null || data == null) {
            throw new ValidationException("Param FileName And Body are needed.");
        }
        fileSysService.installLocalFile(fileName, data.getContent().getBytes("UTF-8"));
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @POST
    @Path("/new/install/local")
    public Response byteInstall(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("fileName") String fileName,
                            byte[] payload) throws Exception {
        if (fileName == null || payload == null || payload.length == 0) {
            throw new ValidationException("Param FileName And Body are needed.");
        }
        fileSysService.installLocalFile(fileName, payload);
        return responseHandler.handle("Install success.", hh.getMediaType());
    }

    @GET
    @Path("/slb/list")
    public Response getSlbDataFiles(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId need.");
        List<FileData> slbFiles = fileSysService.getCurrentFiles(slbId);

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
    public Response getIpDataFiles(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh,
                                   @QueryParam("fileName") String name,
                                   @QueryParam("ip") String ip) throws Exception {
        if (ip == null || name == null) throw new ValidationException("Ip and FileName need.");
        FileData file = fileSysService.getCurrentFile(ip, name);

        DefaultFile result = null;
        if (file != null)
            result = new DefaultFile().setName(file.getKey()).setVersion(file.getVersion()).setFile(new String(file.getFileData()));
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/file/get")
    public Response getFileData(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("fileName") String name,
                                @QueryParam("version") Long version) throws Exception {
        if (name == null || version == null) throw new ValidationException("Version and fileName need.");
        byte[] data = fileSysService.getFile(name, version);
        if (data == null) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_OCTET_STREAM).build();
        }
        return Response.ok(data).build();
    }

    @GET
    @Path("/online/version")
    public Response getFilePageOnlineVersion(@Context HttpServletRequest request,
                                             @Context HttpHeaders hh,
                                             @QueryParam("fileName") String fileName,
                                             @QueryParam("slbId") Long slbId,
                                             @QueryParam("ip") String ip) throws Exception {
        FileData file = null;
        if (slbId != null) {
            file = fileSysService.getCurrentFile(slbId, fileName);
        } else if (ip != null) {
            file = fileSysService.getCurrentFile(ip, fileName);
        }
        if (file == null) {
            return responseHandler.handle("Not Found Online Version", hh.getMediaType());
        }
        return responseHandler.handle(new DefaultFile().setVersion(file.getVersion()).setName(file.getKey()).setFile(new String(file.getFileData())), hh.getMediaType());
    }

    @GET
    @Path("/get")
    public Response getFilePage(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("fileName") String fileName,
                                @QueryParam("version") Long version) throws Exception {
        DefaultFile res = new DefaultFile();
        AssertUtils.assertNotNull(fileName, "fileName is null.");
        AssertUtils.assertNotNull(version, "version not null.");
        byte[] data = fileSysService.getFile(fileName, version);
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
                               @QueryParam("fileName") String fileName,
                               @QueryParam("ip") String ip,
                               @QueryParam("slbId") Long slbId) throws Exception {

        AssertUtils.assertNotNull(fileName, "fileName is null.");
        AssertUtils.assertNotNull(ip, "ip is null.");
        AssertUtils.assertNotNull(slbId, "slbId is null.");
        FileData file = fileSysService.getCurrentFile(slbId, fileName);
        FileData serverFile = fileSysService.getCurrentFile(ip, fileName);
        Map<String, DefaultFile> data = new HashMap<>();
        if (file != null) {
            data.put("slb", new DefaultFile().setFile(new String(file.getFileData())).setVersion(file.getVersion()).setName(file.getKey()));
        }
        if (serverFile != null) {
            data.put("slbServer", new DefaultFile().setFile(new String(serverFile.getFileData())).setVersion(serverFile.getVersion()).setName(serverFile.getKey()));
        }
        return responseHandler.handle(data, hh.getMediaType());
    }

    @GET
    @Path("/get/files")
    public Response getSlbLuaFiles(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh,
                                   @QueryParam("slbId") Long slbId) throws Exception {
        AssertUtils.assertNotNull(slbId, "slbId is null.");

        List<FileData> slbFiles = fileSysService.getCurrentFiles(slbId);
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
    @Path("/set/status")
    public Response setServerFileStatus(@Context HttpServletRequest request,
                                        @Context HttpHeaders hh,
                                        @QueryParam("ip") String ip,
                                        @QueryParam("version") Long version,
                                        @QueryParam("fileName") String name) throws Exception {
        AssertUtils.assertNotNull(ip, "ip is null.");
        AssertUtils.assertNotNull(version, "version is null.");
        AssertUtils.assertNotNull(name, "fileName is null.");
        fileSysService.updateFileStatus(name, ip, version);
        return responseHandler.handle("success", hh.getMediaType());
    }

}
