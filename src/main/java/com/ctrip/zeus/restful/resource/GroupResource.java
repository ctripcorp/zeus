package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.QueryParamRender;
import com.ctrip.zeus.service.query.*;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.tag.entity.Property;
import com.ctrip.zeus.tag.entity.PropertyList;
import com.ctrip.zeus.tag.entity.TagList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/")
public class GroupResource {
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;

    private final String vGroupAppId = "VirtualGroup";
    private final int TIMEOUT = 1000;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response list(@Context HttpHeaders hh,
                         @Context final HttpServletRequest request,
                         @TrimmedQueryParam("mode") final String mode,
                         @TrimmedQueryParam("type") final String type,
                         @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "group", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        GroupList groupList = new GroupList();
        for (Group group : groupRepository.list(searchKeys)) {
            groupList.addGroup(group);
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(ObjectJsonWriter.write(groupList, type), hh.getMediaType());
    }

    @GET
    @Path("/vgroups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response listVGroups(@Context HttpHeaders hh,
                                @Context HttpServletRequest request,
                                @TrimmedQueryParam("mode") final String mode) throws Exception {
        Set<IdVersion> keys = groupCriteriaQuery.queryAllVGroups(SelectionMode.getMode(mode));
        GroupList groupList = new GroupList();
        for (Group group : groupRepository.list(keys.toArray(new IdVersion[keys.size()]))) {
            groupList.addGroup(group);
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(groupList, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupByStatus")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @TrimmedQueryParam("type") String type,
                        @TrimmedQueryParam("mode") final String mode,
                        @Context UriInfo uriInfo) throws Exception {
        QueryEngine queryRender = new QueryEngine(QueryParamRender.extractRawQueryParam(uriInfo), "group", SelectionMode.getMode(mode));
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);
        List<Group> result = groupRepository.list(searchKeys);
        if (result.size() == 0) throw new ValidationException("Group cannot be found.");
        if (result.size() == 1) {
            return responseHandler.handle(ObjectJsonWriter.write(result.get(0), type), hh.getMediaType());
        }
        GroupList groupList = new GroupList().setTotal(result.size());
        for (Group g : result) {
            groupList.addGroup(g);
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(ObjectJsonWriter.write(groupList, type), hh.getMediaType());
    }

    @POST
    @Path("/group/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addGroup")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String group,
                        @QueryParam("force") Boolean force) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        g.setVirtual(null);
        Long groupId = groupCriteriaQuery.queryByName(g.getName());
        if (groupId > 0L) throw new ValidationException("Group name exists.");
        g = groupRepository.add(g, force != null && force);

        try {
            propertyBox.set("status", "deactivated", "group", g.getId());
        } catch (Exception ex) {
        }
        return responseHandler.handle(g, hh.getMediaType());
    }

    @POST
    @Path("/vgroup/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addGroup")
    public Response addVGroup(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        g.setVirtual(true);
        g.setAppId(vGroupAppId);
        Long groupId = groupCriteriaQuery.queryByName(g.getName());
        if (groupId > 0L) throw new ValidationException("Group name exists.");

        g = groupRepository.addVGroup(g);
        return responseHandler.handle(g, hh.getMediaType());
    }

    @POST
    @Path("/group/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateGroup")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String group,
                           @QueryParam("force") Boolean force) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        g.setVirtual(null);

        DistLock lock = dbLockFactory.newLock(g.getName() + "_updateGroup");
        lock.lock(TIMEOUT);
        try {
            g = groupRepository.update(g, force == null ? false : force.booleanValue());
        } finally {
            lock.unlock();
        }

        try {
            if (groupCriteriaQuery.queryByIdAndMode(g.getId(), SelectionMode.OFFLINE_EXCLUSIVE).length == 1) {
                propertyBox.set("status", "toBeActivated", "group", g.getId());
            }
        } catch (Exception ex) {
        }
        return responseHandler.handle(g, hh.getMediaType());
    }

    @POST
    @Path("/vgroup/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateGroup")
    public Response updateVGroup(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        g.setVirtual(true);
        g.setAppId(vGroupAppId);

        DistLock lock = dbLockFactory.newLock(g.getName() + "_updateGroup");
        lock.lock(TIMEOUT);
        try {
            g = groupRepository.updateVGroup(g);
        } finally {
            lock.unlock();
        }
        return responseHandler.handle(g, hh.getMediaType());
    }

    @GET
    @Path("/group/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteGroup")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request,
                           @QueryParam("groupId") Long groupId,
                           @QueryParam("groupName") String groupName) throws Exception {
        if (groupId == null) {
            if (groupName != null && !groupName.isEmpty())
                groupId = groupCriteriaQuery.queryByName(groupName);
        }
        if (groupId == null)
            throw new ValidationException("Query parameter - groupId is not provided or could not be found by query.");
        Group archive = groupRepository.getById(groupId);
        if (archive == null) throw new ValidationException("Group cannot be found with id " + groupId + ".");

        groupRepository.delete(groupId);
        try {
            archiveRepository.archiveGroup(archive);
        } catch (Exception ex) {
            logger.warn("Try archive deleted group failed. " + GenericSerializer.writeJson(archive, false), ex);
        }
        try {
            propertyBox.clear("group", groupId);
        } catch (Exception ex) {
        }
        return responseHandler.handle("Group is deleted.", hh.getMediaType());
    }

    @GET
    @Path("/vgroup/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteGroup")
    public Response deleteVGroup(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null)
            throw new Exception("Query parameter - groupId is required.");
        Group archive = groupRepository.getById(groupId);
        if (archive == null) throw new ValidationException("Virtual group cannot be found with id " + groupId + ".");

        groupRepository.deleteVGroup(groupId);
        try {
            archiveRepository.archiveGroup(archive.setVirtual(true));
        } catch (Exception ex) {
            logger.warn("Try archive deleted virtual group failed. " + GenericSerializer.writeJson(archive, false), ex);
        }
        return responseHandler.handle("Virtual group is deleted.", hh.getMediaType());
    }

    private Group parseGroup(MediaType mediaType, String group) throws Exception {
        Group g;
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            g = DefaultSaxParser.parseEntity(Group.class, group);
        } else {
            try {
                g = ObjectJsonParser.parse(group, Group.class);
            } catch (Exception e) {
                throw new Exception("Group cannot be parsed.");
            }
        }
        g.setAppId(trimIfNotNull(g.getAppId()));
        g.setName(trimIfNotNull(g.getName()));
        if (g.getHealthCheck() != null)
            g.getHealthCheck().setUri(trimIfNotNull(g.getHealthCheck().getUri()));
        for (GroupServer groupServer : g.getGroupServers()) {
            groupServer.setIp(trimIfNotNull(groupServer.getIp()));
            groupServer.setHostName(trimIfNotNull(groupServer.getHostName()));
        }
        if (g.getLoadBalancingMethod() != null)
            g.getLoadBalancingMethod().setValue(trimIfNotNull(g.getLoadBalancingMethod().getValue()));
        return g;
    }

    private String trimIfNotNull(String value) {
        return value != null ? value.trim() : value;
    }
}
