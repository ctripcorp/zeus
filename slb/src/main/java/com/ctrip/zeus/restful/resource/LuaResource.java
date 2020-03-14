package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.file.Constants;
import com.ctrip.zeus.service.lua.LuaService;
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
 * Created by fanqq on 2017/4/28.
 */
@Component
@Path("/lua")
public class LuaResource {

    @Resource
    LuaService luaService;
    @Resource
    ResponseHandler responseHandler;

    @POST
    @Path("/upload")
    public Response upload(@Context HttpServletRequest request,
                           @Context HttpHeaders hh,
                           @QueryParam("fileName") String fileName,
                           @FormDataParam("lua") InputStream page) throws Exception {
        if (fileName == null || page == null) {
            throw new ValidationException("Missing Parameter.");
        }
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = page.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] content = swapStream.toByteArray();
        luaService.addLuaFile(fileName, content);
        return responseHandler.handle("Upload success.", hh.getMediaType());
    }

    @GET
    @Path("/install")
    public Response install(@Context HttpServletRequest request,
                            @Context HttpHeaders hh,
                            @QueryParam("version") Long version,
                            @QueryParam("fileName") String fileName,
                            @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId is need.");

        if (version == null) {
            version = luaService.getMaxVersion(fileName, slbId);
        }
        if (luaService.installLuaFile(fileName, slbId, version)) {
            return responseHandler.handle("Install success.", hh.getMediaType());
        } else {
            throw new NginxProcessingException("Failed to install lua file:" + fileName + ", version:" + version);
        }
    }


    @GET
    @Path("/slb/list")
    public Response getSlbDataFiles(@Context HttpServletRequest request,
                                    @Context HttpHeaders hh,
                                    @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId need.");
        List<FileData> slbFiles = luaService.getCurrentLuaFiles(slbId);

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
                                  @QueryParam("fileName") String name,
                                  @QueryParam("ip") String ip) throws Exception {
        if (ip == null || name == null) throw new ValidationException("ip and fileName need.");
        FileData file = luaService.getCurrentLuaFile(name, ip);
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
        byte[] data = luaService.getFile(name, version);
        if (data == null) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_OCTET_STREAM).build();
        }
        return Response.ok(data).build();
    }

    @POST
    @Path("/install/local")
    public Response postInstall(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("fileName") String fileName,
                                byte[] payload) throws Exception {
        if (fileName == null || payload == null) {
            throw new ValidationException("Param filename or Post body are needed.");
        }
        if (luaService.installLocalLuaFile(fileName, payload)) {
            return responseHandler.handle("Install success.", hh.getMediaType());
        } else {
            throw new NginxProcessingException("Failed to install lua file:" + fileName);
        }
    }

    @GET
    @Path("/conf/install")
    public Response installConf(@Context HttpServletRequest request,
                                @Context HttpHeaders hh,
                                @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId is need.");

        if (luaService.installLuaConfFile(slbId)) {
            return responseHandler.handle("Install success.", hh.getMediaType());
        } else {
            throw new NginxProcessingException("Failed to install lua file on slb:" + slbId);
        }
    }

    @GET
    @Path("/conf/install/local")
    public Response installLocal(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh) throws Exception {
        luaService.installLocalLuaConfFile();
        return responseHandler.handle(Constants.SETSTATUSSUCCESSMSG, hh.getMediaType());
    }


    @GET
    @Path("/online/version")
    public Response getErrorPage(@Context HttpServletRequest request,
                                 @Context HttpHeaders hh,
                                 @QueryParam("fileName") String fileName,
                                 @QueryParam("slbId") Long slbId,
                                 @QueryParam("ip") String ip) throws Exception {
        FileData res = null;
        if (slbId != null) {
            res = luaService.getCurrentLuaFile(fileName, slbId);
        } else if (ip != null) {
            res = luaService.getCurrentLuaFile(fileName, ip);
        }
        if (res == null) {
            return responseHandler.handle("Not Found Online Version", hh.getMediaType());
        }
        return responseHandler.handle(new DefaultFile().setFile(new String(res.getFileData())).setName(res.getKey()).setVersion(res.getVersion()), hh.getMediaType());
    }

    @GET
    @Path("/get")
    public Response getLuaFile(@Context HttpServletRequest request,
                               @Context HttpHeaders hh,
                               @QueryParam("fileName") String fileName,
                               @QueryParam("version") Long version) throws Exception {
        DefaultFile res = new DefaultFile();
        AssertUtils.assertNotNull(fileName, "fileName is null.");
        AssertUtils.assertNotNull(version, "version not null.");
        byte[] data = luaService.getFile(fileName, version);
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
        FileData file = luaService.getCurrentLuaFile(fileName, slbId);
        FileData serverFile = luaService.getCurrentLuaFile(fileName, ip);
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
    @Path("/get/files")
    public Response getSlbLuaFiles(@Context HttpServletRequest request,
                                   @Context HttpHeaders hh,
                                   @QueryParam("slbId") Long slbId) throws Exception {
        AssertUtils.assertNotNull(slbId, "slbId is null.");
        List<FileData> slbFiles = luaService.getCurrentLuaFiles(slbId);
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
        luaService.updateServerFileStatus(name, ip, version);

        return responseHandler.handle(Constants.SETSTATUSSUCCESSMSG, hh.getMediaType());
    }
}
