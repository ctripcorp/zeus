package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.entity.Property;
import com.ctrip.zeus.tag.entity.PropertyList;
import com.google.common.base.Joiner;
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
import java.util.List;
import java.util.Set;

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

    @GET
    @Path("/properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listProperties(@Context HttpHeaders hh,
                                   @Context HttpServletRequest request,
                                   @QueryParam("type") String type,
                                   @QueryParam("targetId") Long targetId) throws Exception {
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
        propertyBox.set(pname, pvalue, type, targetIds.toArray(new Long[targetIds.size()]));
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
        propertyBox.removeProperty(pname, forceEnabled);
        return responseHandler.handle("Successfully deleted property " + pname + ".", hh.getMediaType());
    }
}