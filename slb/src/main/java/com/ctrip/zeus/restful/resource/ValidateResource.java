package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.client.ValidateClient;
import com.ctrip.zeus.client.ValidateClient.ValidateClientResponse;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.model.SlbValidateResponse;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.service.validate.SlbValidatorLocal;
import com.ctrip.zeus.util.CommandUtil;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanqq on 2015/6/26.
 */
@Component
@Path("/validate")
public class ValidateResource {
    @Resource
    private SlbValidatorLocal slbValidatorLocal;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private LocalInfoService localInfoService;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;

    private String nginxVersionCommand = "yum info installed ctrip-slb-nginx";
    private Pattern nginxVersionPattern = Pattern.compile("Version\\s*:\\s*(\\S+)\\s*[\\s\\S]*Release\\s*:\\s*(\\S+)\\s*");

    private DynamicStringProperty jdkPath = DynamicPropertyFactory.getInstance().getStringProperty("jdk8.path", "/usr/java/jdk1.8.0_60");
    private DynamicStringProperty jdk8Path = DynamicPropertyFactory.getInstance().getStringProperty("jdk8.ln.path", "/usr/java/jdk1.8");
    private DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    @GET
    @Path("/slb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response slbValidate(@Context HttpServletRequest request, @Context HttpHeaders hh,
                                @QueryParam("slbId") Long slbId,
                                @QueryParam("slbName") String slbName) throws Exception {
        if (slbId == null || slbId <= 0) {
            throw new ValidationException("Param slbId Can not be null!");
        }
        SlbValidateResponse response = slbValidatorLocal.validate(slbId);
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/local/java/version")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateLocalJavaVersion(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("fill") Boolean fill,
                                             @QueryParam("force") Boolean force) throws Exception {
        String JDK8_PATH = jdk8Path.get();
        File file = new File(JDK8_PATH);
        SlbValidateResponse response = new SlbValidateResponse();
        response.setIp(localInfoService.getLocalIp());
        if (file.exists()) {
            response.setSucceed(true);
        } else {
            response.setSucceed(false);
        }
        if (fill && (force || !response.getSucceed())) {
            String path = jdkPath.get();
            File jdkPath = new File(path);
            if (jdkPath.isDirectory() && jdkPath.exists()) {
                NginxResponse res = CommandUtil.execute("sudo ln -s " + path + " " + jdk8Path.get());
                if (res != null && res.getSucceed()) {
                    response.setSucceed(true);
                    response.setMsg("Add Soft Link File To Jdk1.8 Success.");
                } else {
                    response.setMsg("Add Soft Link File To Jdk1.8 Failed.");
                }
            }
        }
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/local/java/test")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateLocalJavaVersion(@Context HttpServletRequest request, @Context HttpHeaders hh) throws Exception {
        NginxResponse res = CommandUtil.execute("/usr/java/jdk1.8/bin/java -version");
        if (res == null) {
            res = new NginxResponse();
            res.setSucceed(false);
            res.setErrMsg("Failed to execute command");
        }
        SlbValidateResponse response = new SlbValidateResponse();
        response.setMsg(res.getOutMsg() + res.getErrMsg());
        response.setSucceed(res.getSucceed());
        response.setIp(localInfoService.getLocalIp());
        return responseHandler.handle(response, hh.getMediaType());
    }


    @GET
    @Path("/java/version")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateJavaVersion(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("slbId") List<Long> slbIds,
                                        @QueryParam("fill") Boolean fill, @QueryParam("force") Boolean force) throws Exception {
        if (slbIds == null) {
            throw new ValidationException("Invalidate Param SlbId.");
        }

        if (slbIds.size() == 0) {
            slbIds.addAll(slbCriteriaQuery.queryAll());
        }
        ModelStatusMapping<Slb> slbModelStatusMapping = entityFactory.getSlbsByIds(slbIds.toArray(new Long[slbIds.size()]));
        if (slbModelStatusMapping.getOfflineMapping() == null || slbModelStatusMapping.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Slb By Id." + "[[SlbIds=" + slbIds + "]]");
        }
        Map<Long, List<SlbValidateResponse>> result = new HashMap<>();
        for (Long id : slbIds) {
            if (slbModelStatusMapping.getOfflineMapping().get(id) == null) {
                throw new ValidationException("Not Found Slb By Id." + id);
            }
            Slb slb = slbModelStatusMapping.getOfflineMapping().get(id);
            result.put(id, new ArrayList<SlbValidateResponse>());

            for (SlbServer slbServer : slb.getSlbServers()) {
                ValidateClient validateClient = ValidateClient.getClient("http://" + slbServer.getIp() + ":" + adminServerPort.get());
                SlbValidateResponse res = validateClient.javaVersionValidate(fill, force);
                result.get(id).add(res);
            }
        }
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/java/test")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateJavaVersion(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("slbId") List<Long> slbIds) throws Exception {
        if (slbIds == null) {
            throw new ValidationException("Invalidate Param SlbId.");
        }

        if (slbIds.size() == 0) {
            slbIds.addAll(slbCriteriaQuery.queryAll());
        }
        ModelStatusMapping<Slb> slbModelStatusMapping = entityFactory.getSlbsByIds(slbIds.toArray(new Long[slbIds.size()]));
        if (slbModelStatusMapping.getOfflineMapping() == null || slbModelStatusMapping.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Slb By Id." + "[[SlbIds=" + slbIds + "]]");
        }
        Map<Long, List<SlbValidateResponse>> result = new HashMap<>();
        for (Long id : slbIds) {
            if (slbModelStatusMapping.getOfflineMapping().get(id) == null) {
                throw new ValidationException("Not Found Slb By Id." + id);
            }
            Slb slb = slbModelStatusMapping.getOfflineMapping().get(id);
            result.put(id, new ArrayList<SlbValidateResponse>());

            for (SlbServer slbServer : slb.getSlbServers()) {
                ValidateClient validateClient = ValidateClient.getClient("http://" + slbServer.getIp() + ":" + adminServerPort.get());
                SlbValidateResponse res = validateClient.javaTest();
                result.get(id).add(res);
            }
        }
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/local/nginx/version")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateLocalNginxVersion(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("details") Boolean details) throws Exception {
        SlbValidateResponse response = new SlbValidateResponse();
        response.setIp(localInfoService.getLocalIp());
        response.setSlbId(localInfoService.getLocalSlbId());
        NginxResponse res = executeCommand(nginxVersionCommand);
        if (res == null) {
            response.setSucceed(false);
            response.setMsg("Command Execute Error.");
            return responseHandler.handle(response, hh.getMediaType());
        }
        if (!res.isSucceed()) {
            response.setSucceed(false);
            response.setMsg(res.getErrMsg());
            return responseHandler.handle(response, hh.getMediaType());
        }
        if (details != null && details) {
            response.setSucceed(true);
            response.setMsg(res.getOutMsg());
            return responseHandler.handle(response, hh.getMediaType());
        }

        Matcher mather = nginxVersionPattern.matcher(res.getOutMsg());
        if (!mather.find()) {
            response.setSucceed(false);
            response.setMsg("Not Found Version.");
            return responseHandler.handle(response, hh.getMediaType());
        }

        if (mather.groupCount() != 2) {
            response.setSucceed(false);
            response.setMsg("Found Error Version.");
            return responseHandler.handle(response, hh.getMediaType());
        }

        response.setSucceed(true);
        response.setMsg(mather.group(1) + "_" + mather.group(2));
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/nginx/version")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateNginxVersion(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("slbId") List<Long> slbIds, @QueryParam("details") Boolean details) throws Exception {
        if (slbIds == null) {
            throw new ValidationException("Invalidate Param SlbId.");
        }

        if (slbIds.size() == 0) {
            slbIds.addAll(slbCriteriaQuery.queryAll());
        }
        ModelStatusMapping<Slb> slbModelStatusMapping = entityFactory.getSlbsByIds(slbIds.toArray(new Long[slbIds.size()]));
        if (slbModelStatusMapping.getOfflineMapping() == null || slbModelStatusMapping.getOfflineMapping().size() == 0) {
            throw new ValidationException("Not Found Slb By Id." + "[[SlbIds=" + slbIds + "]]");
        }
        Map<Long, Map<String, List<SlbValidateResponse>>> result = new HashMap<>();
        List<ValidateClientResponse> validateClientResponses = new ArrayList<>();
        for (Long id : slbIds) {
            if (slbModelStatusMapping.getOfflineMapping().get(id) == null) {
                throw new ValidationException("Not Found Slb By Id." + id);
            }
            Slb slb = slbModelStatusMapping.getOfflineMapping().get(id);
            result.put(id, new HashMap<String, List<SlbValidateResponse>>());

            for (SlbServer slbServer : slb.getSlbServers()) {
                ValidateClient validateClient = getValidateClient("http://" + slbServer.getIp() + ":" + adminServerPort.get());
                Future<Response> responseFuture = validateClient.nginxVersionValidateAsync(details);
                validateClientResponses.add(new ValidateClientResponse().setSlbId(id).setIp(slbServer.getIp())
                        .setResponseFuture(responseFuture).setValidateClient(validateClient));
            }
        }
        for (ValidateClientResponse validateClientResponse : validateClientResponses) {
            SlbValidateResponse response = validateClientResponse.getValidateClient().nginxVersionValidateCallback(validateClientResponse);
            Map<String, List<SlbValidateResponse>> slbNginxVersionGroup = result.get(validateClientResponse.getSlbId());
            if (!slbNginxVersionGroup.containsKey(response.getMsg())) {
                slbNginxVersionGroup.put(response.getMsg(), new ArrayList<SlbValidateResponse>());
            }
            slbNginxVersionGroup.get(response.getMsg()).add(new SlbValidateResponse().setSucceed(response.getSucceed()).setIp(response.getIp()));
        }
        return responseHandler.handle(result, hh.getMediaType());
    }

    //for test
    NginxResponse executeCommand(String command) {
        return CommandUtil.execute(command);
    }

    //for test
    ValidateClient getValidateClient(String uri) throws Exception {
        return ValidateClient.getClient(uri);
    }
}
