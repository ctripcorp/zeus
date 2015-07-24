package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.tag.entity.TagList;
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
 * Created by zhoumy on 2015/7/16.
 */
@Component
@Path("/")
public class TagResource {
    @Resource
    private TagBox tagBox;
    @Resource
    private TagService tagService;
    @Resource
    private ResponseHandler responseHandler;

    @GET
    @Path("/tags")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listTags(@Context HttpHeaders hh,
                             @Context HttpServletRequest request,
                             @QueryParam("type") String type,
                             @QueryParam("id") Long id) throws Exception {
        List<String> list;
        if (type != null && id != null) {
            list = tagService.getTags(type, id);
        } else {
            list = tagBox.getAllTags();
        }
        TagList tagList = new TagList().setTotal(list.size());
        for (String s : list) {
            tagList.addTag(s);
        }
        return responseHandler.handle(tagList, hh.getMediaType());
    }

    @GET
    @Path("/tag/rename")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response renameTag(@Context HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("oldName") String oldName,
                              @QueryParam("newName") String newName) throws Exception {
        if (oldName == null || newName == null)
            throw new ValidationException("Both oldName and newName must be provided.");
        tagBox.renameTag(oldName, newName);
        return responseHandler.handle(oldName + " is renamed to " + newName + ".", hh.getMediaType());
    }

    @GET
    @Path("/tagging")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response tagging(@Context HttpHeaders hh,
                            @Context HttpServletRequest request,
                            @QueryParam("tagName") String tagName,
                            @QueryParam("type") String type,
                            @QueryParam("id") List<Long> ids) throws Exception {
        if (tagName == null || type == null || ids == null)
            throw new ValidationException("At least one parameter is missing.");
        tagBox.tagging(tagName, type, ids.toArray(new Long[ids.size()]));
        return responseHandler.handle("Tagged " + Joiner.on(", ").join(ids) + " to " + tagName + ".", hh.getMediaType());
    }

    @GET
    @Path("/untagging")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response untagging(@Context HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("tagName") String tagName,
                              @QueryParam("type") String type,
                              @QueryParam("id") List<Long> ids,
                              @QueryParam("batch") Boolean batch) throws Exception {
        if (tagName == null)
            throw new ValidationException("Tag name is required.");
        if (type != null && ids != null) {
            tagBox.untagging(tagName, type, ids.toArray(new Long[ids.size()]));
            return responseHandler.handle("Untagged " + Joiner.on(", ").join(ids) + " from " + tagName + ".", hh.getMediaType());
        }
        if (batch != null && batch.booleanValue()) {
            if (type != null) {
                tagBox.untagging(tagName, type, null);
                return responseHandler.handle("Untagged all the items with typeName - " + type + " from " + tagName + ".", hh.getMediaType());
            } else {
                tagBox.removeTag(tagName);
                return responseHandler.handle("Deleted tag named " + tagName + ".", hh.getMediaType());
            }
        }
        throw new ValidationException("At least one parameter is missing.");
    }
}