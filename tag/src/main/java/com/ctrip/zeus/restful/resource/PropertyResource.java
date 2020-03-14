package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.TagValidationException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.util.UserUtils;
import com.google.common.base.Joiner;
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
import java.util.*;

/**
 * Created by zhoumy on 2015/7/20.
 */
@Component
@Path("/")
public class PropertyResource {
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private PropertyService propertyService;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private AuthService authService;


    private DynamicStringProperty preservedProperties = DynamicPropertyFactory.getInstance().getStringProperty("slb.property.preserved", "SBU,status,healthy");
    private Set<String> preservedPropertyNames;

    public PropertyResource() {
        getPreservedPropertyName();
        preservedProperties.addCallback(new Runnable() {
            @Override
            public void run() {
                getPreservedPropertyName();
            }
        });
    }

    @GET
    @Path("/property/query/targets")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listProperties(@Context HttpHeaders hh,
                                   @Context HttpServletRequest request,
                                   @QueryParam("type") String type,
                                   @QueryParam("pname") String pname,
                                   @QueryParam("pvalue") String pvalue) throws Exception {
        if (type == null || pname == null) {
            throw new TagValidationException("Parameter type and pname can not be null.");
        }
        List<Long> result;
        if (pvalue != null) {
            result = propertyService.queryTargets(pname, pvalue, type);
        } else {
            result = new ArrayList<>(propertyService.queryTargets(pname, type));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.size());
        response.put("targets", result);
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listProperties(@Context HttpHeaders hh,
                                   @Context HttpServletRequest request,
                                   @QueryParam("type") String type,
                                   @QueryParam("targetId") Long targetId) throws Exception {

        List<ResourceDataType> dataTypes = new ArrayList<>();
        if (type == null) {
            dataTypes.add(ResourceDataType.Group);
            dataTypes.add(ResourceDataType.Vs);
            dataTypes.add(ResourceDataType.Slb);
        } else if (type.equalsIgnoreCase("group")) {
            dataTypes.add(ResourceDataType.Group);
        } else if (type.equalsIgnoreCase("slb")) {
            dataTypes.add(ResourceDataType.Slb);
        } else if (type.equalsIgnoreCase("vs")) {
            dataTypes.add(ResourceDataType.Vs);
        }
        if (targetId == null) {
            for (ResourceDataType dt : dataTypes) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.PROPERTY, dt, AuthDefaultValues.ALL);
            }
        } else {
            for (ResourceDataType dt : dataTypes) {
                authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.PROPERTY, dt, targetId);
            }
        }

        List<Property> list = null;
        if (type != null) {
            if (targetId != null) {
                list = propertyService.getProperties(type, targetId);
            } else {
                Set<Long> propIds = propertyService.queryByType(type);
                list = propertyService.getProperties(propIds.toArray(new Long[propIds.size()]));
            }
        }

        if (list == null) {
            list = propertyService.getAllProperties();
        }

        PropertyList propertyList = new PropertyList().setTotal(list.size());
        for (Property property : list) {
            propertyList.addProperty(property);
        }
        return responseHandler.handle(propertyList, hh.getMediaType());
    }

    /**
     * @api {get} /api/property/set: [Write] Set property
     * @apiName SetProperty
     * @apiGroup Property
     * @apiSuccess (Success 200) {String} message success message
     * @apiParam {long[]} targetId          target id to be tagged
     * @apiParam {string=group,vs,slb} type target type
     * @apiParam {string} pname             property name
     * @apiParam {string} pvalue            property value
     **/
    @GET
    @Path("/property/set")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addPropertyItem(@Context HttpHeaders hh,
                                    @Context HttpServletRequest request,
                                    @QueryParam("pname") String pname,
                                    @QueryParam("pvalue") String pvalue,
                                    @QueryParam("type") String type,
                                    @QueryParam("targetId") List<Long> targetIds) throws Exception {
        if (pname == null || pvalue == null || type == null || targetIds == null)
            throw new ValidationException("At least one parameter is missing.");
        ResourceDataType resourceDataType = ResourceDataType.Group;
        if (type.equalsIgnoreCase("group")) {
            resourceDataType = ResourceDataType.Group;
        } else if (type.equalsIgnoreCase("vs")) {
            resourceDataType = ResourceDataType.Vs;
        } else if (type.equalsIgnoreCase("slb")) {
            resourceDataType = ResourceDataType.Slb;
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.PROPERTY, resourceDataType, targetIds.toArray(new Long[]{}));

        if (preservedPropertyNames.contains(pname)) {
            throw new ValidationException("Property name " + pname + " is preserved. Please rename your property.");
        }
        propertyBox.set(pname, pvalue, type.toLowerCase(), targetIds.toArray(new Long[targetIds.size()]));
        return responseHandler.handle(Joiner.on(",").join(targetIds) + " is/are added to property " + pname + "/" + pvalue + ".", hh.getMediaType());
    }

    /**
     * @api {get} /api/property/clear: [Write] Clear property
     * @apiName ClearProperty
     * @apiGroup Property
     * @apiSuccess (Success 200) {String} message success message
     * @apiParam {long[]} targetId          target id to be tagged
     * @apiParam {string=group,vs,slb} type target type
     * @apiParam {string} pname             property name
     * @apiParam {string} pvalue            property value
     **/
    @GET
    @Path("/property/clear")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deletePropertyItem(@Context HttpHeaders hh,
                                       @Context HttpServletRequest request,
                                       @QueryParam("pname") String pname,
                                       @QueryParam("pvalue") String pvalue,
                                       @QueryParam("type") String type,
                                       @QueryParam("targetId") List<Long> targetIds) throws Exception {
        if (targetIds == null && type == null) {
            throw new ValidationException("Query parameter targetId and type are required.");
        }

        if (pname == null && pvalue == null) {
            for (Long tId : targetIds) {
                propertyBox.clear(type, tId);
            }
            return responseHandler.handle("Successfully clear property from " + type + " " + Joiner.on(",").join(targetIds) + ".", hh.getMediaType());
        }

        if (pname == null || pvalue == null) {
            throw new ValidationException("Both pname and pvalue is required.");
        }

        ResourceDataType resourceDataType = ResourceDataType.Group;
        if (type.equalsIgnoreCase("group")) {
            resourceDataType = ResourceDataType.Group;
        } else if (type.equalsIgnoreCase("vs")) {
            resourceDataType = ResourceDataType.Vs;
        } else if (type.equalsIgnoreCase("slb")) {
            resourceDataType = ResourceDataType.Slb;
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.PROPERTY, resourceDataType, targetIds.toArray(new Long[]{}));

        propertyBox.clear(pname, pvalue, type, targetIds.toArray(new Long[targetIds.size()]));
        return responseHandler.handle(Joiner.on(",").join(targetIds) + " is/are deleted from property " + pname + "/" + pvalue + ".", hh.getMediaType());
    }

    /**
     * @api {get} /api/property/delete: [Write] Delete property
     * @apiName DeleteProperty
     * @apiGroup Property
     * @apiSuccess (Success 200) {String} message success message
     * @apiParam {string} pname         property to be deleted
     * @apiParam {boolean} [force]      force delete property regardless its item dependency
     **/
    @GET
    @Path("/property/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteProperty(@Context HttpHeaders hh,
                                   @Context HttpServletRequest request,
                                   @QueryParam("pname") String pname,
                                   @QueryParam("force") Boolean force) throws Exception {
        if (pname == null) throw new ValidationException("Parameter pname is required.");
        boolean forceEnabled = force != null && force;
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.PROPERTY, ResourceDataType.Group, AuthDefaultValues.ALL);
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.PROPERTY, ResourceDataType.Slb, AuthDefaultValues.ALL);
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.PROPERTY, ResourceDataType.Vs, AuthDefaultValues.ALL);

        propertyBox.removeProperty(pname, forceEnabled);
        return responseHandler.handle("Successfully deleted property " + pname + ".", hh.getMediaType());
    }

    private void getPreservedPropertyName() {
        Set<String> tmp = new HashSet<>();
        for (String s : preservedProperties.get().split(",")) {
            tmp.add(s);
        }
        preservedPropertyNames = tmp;
    }
}