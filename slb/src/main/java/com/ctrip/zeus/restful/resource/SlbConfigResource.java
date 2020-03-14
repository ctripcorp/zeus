package com.ctrip.zeus.restful.resource;

import com.alibaba.fastjson.JSON;
import com.ctrip.zeus.dao.entity.SlbConfig;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.config.SlbConfigService;
import com.ctrip.zeus.util.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Discription
 **/
@Component
@Path("/config")
public class SlbConfigResource {

    @Resource
    private SlbConfigService slbConfigService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private AuthService authService;

    @GET
    @Path("/get")
    public Response get(@Context HttpServletRequest request, @Context HttpHeaders hh,
                        @QueryParam("key") String key)
            throws Exception {

        if (key == null) throw new ValidationException("key is required");

        List<String> keys = new ArrayList<>();
        keys.add(key);

        Map<String, String> result = slbConfigService.query(keys);
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/all")
    public Response all(@Context HttpServletRequest request, @Context HttpHeaders httpHeaders,
                        @QueryParam("system") Boolean system)
            throws Exception {
        List<SlbConfig> configs = slbConfigService.all(system == null ? false : system);

        StringBuilder stringBuilder = new StringBuilder();
        for (SlbConfig config : configs) {
            String line = config.getPropertyKey() + "=" + config.getPropertyValue() + "\n";
            stringBuilder.append(line);
        }
        return Response.status(Response.Status.OK).entity(stringBuilder.toString()).type(MediaType.TEXT_PLAIN).build();
    }

    @POST
    @Path("/batchupdate")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response batchUpdate(@Context HttpServletRequest request, @Context HttpHeaders hh, String requestBody,
                                @QueryParam("system") Boolean system)
            throws Exception {
        // Not need to check requestBody's nullness because ObjectJsonParser.parseArray method
        // will return null in this situation
        List<SlbConfig> updateRecords = JSON.parseArray(requestBody, SlbConfig.class);
        if (updateRecords == null) {
            throw new ValidationException("can not parse post entity to List<SlbConfig>.");
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.UPDATE, ResourceDataType.Config, AuthDefaultValues.ALL);

        slbConfigService.batchUpdate(buildConfigMap(updateRecords), system == null ? false : system);

        return responseHandler.handle("Upsert succeed.", hh.getMediaType());
    }

    @Path("/batchdelete")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response batchDelete(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("key") List<String> keys,
                                @QueryParam("system") Boolean system)
            throws Exception {
        if (keys == null || keys.size() == 0) {
            throw new ValidationException("Query parameter key must be provided. ");
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.DELETE, ResourceDataType.Config, AuthDefaultValues.ALL);

        slbConfigService.batchDelete(keys, system == null ? false : system);
        return responseHandler.handle("Batch delete succeed.", hh.getMediaType());
    }

    @Path("/batchinsert")
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response batchInsert(@Context HttpServletRequest request, @Context HttpHeaders hh, String requestBody,
                                @QueryParam("system") Boolean system)
            throws Exception {
        List<SlbConfig> insertRecords = JSON.parseArray(requestBody, SlbConfig.class);
        if (insertRecords == null) {
            throw new ValidationException("Cannot parse List<SlbConfig> out of request body. Request body: " + requestBody);
        }

        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Config, AuthDefaultValues.ALL);

        Map<String, String> keyValueMap = buildConfigMap(insertRecords);
        slbConfigService.batchInsert(keyValueMap, system == null ? false : system);

        return responseHandler.handle("Batch insertion succeed. ", hh.getMediaType());
    }

    /*
     * @Description build a key-value map out of List<SlbConfig>
     * throws ValidationException when two KV-pairs have same key.
     * @return
     **/
    private Map<String, String> buildConfigMap(List<SlbConfig> records)
            throws ValidationException {
        if (records == null) {
            return new HashMap<>();
        }
        Map<String, String> configMaps = new HashMap<>(records.size());
        for (SlbConfig slbConfig : records) {
            if (slbConfig.getPropertyKey() == null) {
                throw new ValidationException("Config key can not be null");
            }
            if (configMaps.containsKey(slbConfig.getPropertyKey())) {
                // configs with same key found,throw exception
                throw new ValidationException("PV pairs with same key are found. Key: " + slbConfig.getPropertyKey());
            }
            configMaps.put(slbConfig.getPropertyKey(), slbConfig.getPropertyValue());
        }
        return configMaps;
    }
}
