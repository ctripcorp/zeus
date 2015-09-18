package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupList;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.AssertUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private SlbRepository slbRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private TagService tagService;
    @Resource
    private PropertyService propertyService;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("slbId") Long slbId,
                         @QueryParam("appId") String appId,
                         @TrimmedQueryParam("slbName") String slbName,
                         @TrimmedQueryParam("domain") String domain,
                         @TrimmedQueryParam("type") String type,
                         @TrimmedQueryParam("tag") String tag,
                         @TrimmedQueryParam("pname") String pname,
                         @TrimmedQueryParam("pvalue") String pvalue) throws Exception {
        GroupList groupList = new GroupList();
        Set<Long> filtered = groupCriteriaQuery.queryAll();
        if (appId != null) {
            filtered.retainAll(groupCriteriaQuery.queryByAppId(appId));
        }
        if (tag != null) {
            filtered.retainAll(tagService.query(tag, "group"));
        }
        if (pname != null) {
            if (pvalue != null)
                filtered.retainAll(propertyService.query(pname, pvalue, "group"));
            else
                filtered.retainAll(propertyService.query(pname, "group"));
        }
        if (domain != null) {
            Set<Long> vsIds = virtualServerCriteriaQuery.queryByDomain(domain);
            filtered.retainAll(groupCriteriaQuery.queryByVsIds(vsIds.toArray(new Long[vsIds.size()])));
        }
        if (slbName != null || slbId != null) {
            if (slbId == null) {
                Slb slb = slbRepository.get(slbName);
                if (slb != null)
                    slbId = slb.getId();
            }
            filtered.retainAll(groupCriteriaQuery.queryBySlbId(slbId));
        }
        for (Group group : groupRepository.list(filtered.toArray(new Long[filtered.size()]))) {
            groupList.addGroup(getGroupByType(group, type));
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(groupList, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroup")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("groupId") Long groupId,
                        @TrimmedQueryParam("groupName") String groupName,
                        @TrimmedQueryParam("type") String type) throws Exception {
        Group group = null;
        if (groupId == null && groupName == null) {
            throw new ValidationException("Missing parameters.");
        }
        if (groupId != null) {
            group = groupRepository.getById(groupId);
        }
        if (group == null && groupName != null) {
            group = groupRepository.get(groupName);
        }
        AssertUtils.assertNotNull(group, "Group cannot be found.");
        return responseHandler.handle(getGroupByType(group, type), hh.getMediaType());
    }

    @POST
    @Path("/group/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addGroup")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = groupRepository.add(parseGroup(hh.getMediaType(), group));
        return responseHandler.handle(g, hh.getMediaType());
    }

    @POST
    @Path("/group/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateGroup")
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        DistLock lock = dbLockFactory.newLock(g.getName() + "_updateGroup");
        try {
            lock.lock();
            g = groupRepository.update(g);
        } finally {
            lock.unlock();
        }
        return responseHandler.handle(g, hh.getMediaType());
    }

    @GET
    @Path("/group/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteGroup")
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null)
            throw new Exception("Missing parameter.");
        groupRepository.delete(groupId);
        return Response.ok().build();
    }

    private Group parseGroup(MediaType mediaType, String group) throws Exception {
        Group g;
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            g = DefaultSaxParser.parseEntity(Group.class, group);
        } else {
            try {
                g = DefaultJsonParser.parse(Group.class, group);
            } catch (Exception e) {
                throw new Exception("Group cannot be parsed.");
            }
        }
        g.setAppId(g.getAppId().trim());
        g.setName(g.getName().trim());
        if (g.getHealthCheck() != null)
            g.getHealthCheck().setUri(g.getHealthCheck().getUri().trim());
        for (GroupServer groupServer : g.getGroupServers()) {
            groupServer.setIp(groupServer.getIp().trim());
            groupServer.setHostName(groupServer.getHostName().trim());
        }
        g.getLoadBalancingMethod().setValue(g.getLoadBalancingMethod().getValue());
        return g;
    }

    private Group getGroupByType(Group group, String type) {
        if ("INFO".equalsIgnoreCase(type)) {
            return new Group().setId(group.getId())
                    .setName(group.getName());
        }
        if ("NORMAL".equalsIgnoreCase(type)) {
            return new Group().setId(group.getId())
                    .setName(group.getName())
                    .setAppId(group.getAppId())
                    .setHealthCheck(group.getHealthCheck())
                    .setLoadBalancingMethod(group.getLoadBalancingMethod())
                    .setSsl(group.getSsl())
                    .setVersion(group.getVersion());
        }
        return group;
    }
}
