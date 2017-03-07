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
import java.util.Set;

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
                             @QueryParam("targetId") Long targetId) throws Exception {
        List<String> tags = null;
        if (type != null) {
            if (targetId != null) {
                tags = tagService.getTags(type, targetId);
            } else {
                Set<Long> tagIds = tagService.queryByType(type);
                tags = tagService.getTags(tagIds.toArray(new Long[tagIds.size()]));
            }
        }

        if (tags == null) {
            tags = tagService.getAllTags();
        }

        TagList tagList = new TagList();
        for (String t : tags) {
            tagList.addTag(t);
        }
        tagList.setTotal(tagList.getTags().size());
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

    /**
     * @api {get} /api/tagging: [Write] Tag item(s)
     * @apiName Tagging
     * @apiGroup Tag
     * @apiSuccess (Success 200) {String} success
     * @apiParam {long[]} targetId          target id to be tagged
     * @apiParam {string=group,vs,slb}      type target type
     * @apiParam {string} tagName           tag name
     **/
    @GET
    @Path("/tagging")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response tagging(@Context HttpHeaders hh,
                            @Context HttpServletRequest request,
                            @QueryParam("tagName") String tagName,
                            @QueryParam("type") String type,
                            @QueryParam("targetId") List<Long> targetIds) throws Exception {
        if (tagName == null || type == null || targetIds == null)
            throw new ValidationException("At least one parameter is missing.");
        tagBox.tagging(tagName, type, targetIds.toArray(new Long[targetIds.size()]));
        return responseHandler.handle("Tagged " + Joiner.on(", ").join(targetIds) + " to " + tagName + ".", hh.getMediaType());
    }

    /**
     * @api {get} /api/untagging: [Write] Untag item(s)
     * @apiName Untagging
     * @apiGroup Tag
     * @apiSuccess (Success 200) {String} message success message
     * @apiParam {long[]} [targetId]            target id to be untagged. Nullable if `batch` is set to true
     * @apiParam {string=group,vs,slb} type     target type
     * @apiParam {string} tagName               tag name
     * @apiParam {boolean} [batch]              untag all the items having the tag
     **/
    @GET
    @Path("/untagging")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response untagging(@Context HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("tagName") String tagName,
                              @QueryParam("type") String type,
                              @QueryParam("targetId") List<Long> targetIds,
                              @QueryParam("batch") Boolean batch) throws Exception {
        if (tagName == null)
            throw new ValidationException("Tag name is required.");
        if (type != null && targetIds != null) {
            tagBox.untagging(tagName, type, targetIds.toArray(new Long[targetIds.size()]));
            return responseHandler.handle("Untagged " + Joiner.on(", ").join(targetIds) + " from " + tagName + ".", hh.getMediaType());
        }
        if (batch != null && batch.booleanValue()) {
            if (type != null) {
                tagBox.untagging(tagName, type, null);
                return responseHandler.handle("Untagged all the items with typeName - " + type + " from " + tagName + ".", hh.getMediaType());
            } else {
                throw new ValidationException("Type is required when doing batch untagging.");
            }
        }
        return responseHandler.handle("No action is performed.", hh.getMediaType());
    }

    /**
     * @api {get} /api/tag/remove: [Write] Remove tag
     * @apiName RemoveTag
     * @apiGroup Tag
     * @apiSuccess (Success 200) {String} message success message
     * @apiParam {string} targetName    tag to be deleted
     * @apiParam {boolean} [force]      force delete tag regardless its item dependency
     **/
    @GET
    @Path("/tag/remove")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response removeTag(@Context HttpHeaders hh,
                              @Context HttpServletRequest request,
                              @QueryParam("tagName") String tagName,
                              @QueryParam("force") Boolean force) throws Exception {
        if (tagName == null) throw new ValidationException("Tag name is required.");
        boolean forceEnabled = force != null && force;
        tagBox.removeTag(tagName, forceEnabled);
        return responseHandler.handle("Successfully removed tag - " + tagName + ".", hh.getMediaType());
    }

    /**
     * @api {get} /api/tag/clear: [Write] Clear tag
     * @apiName ClearTag
     * @apiGroup Tag
     * @apiSuccess (Success 200) {String} message success message
     * @apiParam {long} targetId    the item that tags needs to be cleared
     * @apiParam {string=group,vs,slb} type     target type
     **/
    @GET
    @Path(("/tag/clear"))
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response clearTag(@Context HttpHeaders hh,
                             @Context HttpServletRequest request,
                             @QueryParam("targetId") Long targetId,
                             @QueryParam("type") String type) throws Exception {
        if (targetId == null || type == null) {
            throw new ValidationException("Query Param targetId and type is required.");
        }
        tagBox.clear(type, targetId);
        return responseHandler.handle("Successfully clear tag from " + type + " " + targetId + ".", hh.getMediaType());
    }
}