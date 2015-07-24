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
                                   @QueryParam("id") Long id) throws Exception {
        List<Property> list;
        if (type != null && id != null) {
            list = propertyService.getProperties(type, id);
        } else {
            list = propertyBox.getAllProperties();
        }
        PropertyList propertyList = new PropertyList().setTotal(list.size());
        for (Property property : list) {
            propertyList.addProperty(property);
        }
        return responseHandler.handle(propertyList, hh.getMediaType());
    }

    @GET
    @Path("/property/rename")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response renameProperty(@Context HttpHeaders hh,
                                   @Context HttpServletRequest request,
                                   @QueryParam("oldName") String oldName,
                                   @QueryParam("newName") String newName,
                                   @QueryParam("name") String name,
                                   @QueryParam("oldValue") String oldValue,
                                   @QueryParam("newValue") String newValue,
                                   @QueryParam("batch") Boolean batch) throws Exception {
        if ((oldName == null || newName == null) && name == null)
            throw new ValidationException("Property name is missing.");
        if (batch != null && batch.booleanValue()) {
            if (oldName == null || newName == null)
                throw new ValidationException("Batch job cannot be done without oldName and newName given.");
            if (oldValue != null && newValue != null)
                throw new ValidationException("Parameter conflicts.");
            propertyBox.renameProperty(oldName, newName);
            return responseHandler.handle("Property named " + oldName + "is replaced by " + newName + ".", hh.getMediaType());
        }
        if (oldValue == null || newValue == null)
            throw new ValidationException("Property value is missing.");
        if (name != null) {
            propertyBox.renameProperty(name, name, oldValue, newValue);
            return responseHandler.handle(name + "/" + oldValue + " is replaced by " + name + "/" + newValue + ".", hh.getMediaType());
        }
        propertyBox.renameProperty(oldName, newName, oldValue, newValue);
        return responseHandler.handle(oldName + "/" + oldValue + " is replaced by " + newName + "/" + newValue + ".", hh.getMediaType());
    }

    @GET
    @Path("/property/add")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addPropertyItem(@Context HttpHeaders hh,
                                    @Context HttpServletRequest request,
                                    @QueryParam("pname") String pname,
                                    @QueryParam("pvalue") String pvalue,
                                    @QueryParam("type") String type,
                                    @QueryParam("id") List<Long> ids) throws Exception {
        if (pname == null || pvalue == null || type == null || ids == null)
            throw new ValidationException("At least one parameter is missing.");
        propertyBox.add(pname, pvalue, type, ids.toArray(new Long[ids.size()]));
        return responseHandler.handle(Joiner.on(", ").join(ids) + " is added to property " + pname + "/" + pvalue + ".", hh.getMediaType());
    }

    @GET
    @Path("/property/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deletePropertyItem(@Context HttpHeaders hh,
                                       @Context HttpServletRequest request,
                                       @QueryParam("pname") String pname,
                                       @QueryParam("pvalue") String pvalue,
                                       @QueryParam("type") String type,
                                       @QueryParam("id") List<Long> ids,
                                       @QueryParam("batch") Boolean batch) throws Exception {
        if ((pname == null && pvalue == null) || type == null)
            throw new ValidationException("At least one parameter is missing.");
        if (ids != null) {
            propertyBox.delete(pname, pvalue, type, ids.toArray(new Long[ids.size()]));
            return responseHandler.handle(Joiner.on(", ").join(ids) + " is deleted from property " + pname + "/" + pvalue + ".", hh.getMediaType());
        }
        if (batch != null && batch.booleanValue()) {
            propertyBox.delete(pname, pvalue, type, null);
            return responseHandler.handle("Deleted all properties with type - " + type + " from " + pname + "/" + pvalue + ".", hh.getMediaType());

        }
        return responseHandler.handle("No action is performed.", hh.getMediaType());
    }
}