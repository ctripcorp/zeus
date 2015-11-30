package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupList;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.restful.filter.FilterSet;
import com.ctrip.zeus.restful.filter.QueryExecuter;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.restful.message.TrimmedQueryParam;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.AssertUtils;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("slbId") final Long slbId,
                         @QueryParam("appId") final String appId,
                         @TrimmedQueryParam("slbName") final String slbName,
                         @TrimmedQueryParam("domain") final String domain,
                         @TrimmedQueryParam("type") String type,
                         @TrimmedQueryParam("tag") final String tag,
                         @TrimmedQueryParam("pname") final String pname,
                         @TrimmedQueryParam("pvalue") final String pvalue) throws Exception {
        GroupList groupList = new GroupList();
        QueryExecuter executer = new QueryExecuter.Builder()
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        return groupCriteriaQuery.queryAll();
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (appId != null)
                            input.retainAll(groupCriteriaQuery.queryByAppId(appId));
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (tag != null) {
                            input.retainAll(tagService.query(tag, "group"));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (pname != null) {
                            if (pvalue != null)
                                input.retainAll(propertyService.query(pname, pvalue, "group"));
                            else
                                input.retainAll(propertyService.query(pname, "group"));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (domain != null) {
                            Set<Long> vsIds = virtualServerCriteriaQuery.queryByDomain(domain);
                            input.retainAll(groupCriteriaQuery.queryByVsIds(vsIds.toArray(new Long[vsIds.size()])));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        Long sId = slbId;
                        if (sId == null && slbName != null) {
                            sId = slbCriteriaQuery.queryByName(slbName);
                        }
                        if (sId != null) {
                            input.retainAll(groupCriteriaQuery.queryBySlbId(sId));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        input.removeAll(groupCriteriaQuery.queryAllVGroups());
                        return input;
                    }
                })
                .build();
        for (Group group : groupRepository.list(executer.run())) {
            groupList.addGroup(getGroupByType(group, type));
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(groupList, hh.getMediaType());
    }

    @GET
    @Path("/vgroups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response listVGroups(@Context HttpHeaders hh,
                                @Context HttpServletRequest request,
                                @QueryParam("slbId") final Long slbId,
                                @TrimmedQueryParam("domain") final String domain,
                                @TrimmedQueryParam("type") String type,
                                @TrimmedQueryParam("tag") final String tag,
                                @TrimmedQueryParam("pname") final String pname,
                                @TrimmedQueryParam("pvalue") final String pvalue) throws Exception {
        GroupList groupList = new GroupList();
        QueryExecuter executer = new QueryExecuter.Builder()
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        return groupCriteriaQuery.queryAllVGroups();
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (tag != null) {
                            input.retainAll(tagService.query(tag, "group"));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (pname != null) {
                            if (pvalue != null)
                                input.retainAll(propertyService.query(pname, pvalue, "group"));
                            else
                                input.retainAll(propertyService.query(pname, "group"));
                        }
                        return input;
                    }
                })
                .addFilterId(new FilterSet<Long>() {
                    @Override
                    public Set<Long> filter(Set<Long> input) throws Exception {
                        if (domain != null) {
                            Set<Long> vsIds = virtualServerCriteriaQuery.queryByDomain(domain);
                            input.retainAll(groupCriteriaQuery.queryByVsIds(vsIds.toArray(new Long[vsIds.size()])));
                        }
                        return input;
                    }
                })
                .build();
        for (Group group : groupRepository.list(executer.run())) {
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
        if (groupId == null) {
            groupId = groupCriteriaQuery.queryByName(groupName);
        }
        if (groupId != null) {
            group = groupRepository.getById(groupId);
        }
        if (group == null) {
            throw new ValidationException("Group cannot be found.");
        }
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
    @Path("/vgroup/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addGroup")
    public Response addVGroup(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = groupRepository.addVGroup(parseGroup(hh.getMediaType(), group));
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

    @POST
    @Path("/vgroup/update")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "updateGroup")
    public Response updateVGroup(@Context HttpHeaders hh, @Context HttpServletRequest request, String group) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        DistLock lock = dbLockFactory.newLock(g.getName() + "_updateGroup");
        try {
            lock.lock();
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
    public Response delete(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null)
            throw new Exception("Missing parameter.");
        groupRepository.delete(groupId);
        return Response.ok().build();
    }

    @GET
    @Path("/vgroup/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteGroup")
    public Response deleteVGroup(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null)
            throw new Exception("Missing parameter.");
        groupRepository.deleteVGroup(groupId);
        return Response.ok().build();
    }

    @GET
    @Path("/group/syncStatus")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response upgradeAll(@Context HttpHeaders hh,
                               @Context HttpServletRequest request,
                               @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId != null) {
            groupRepository.syncMemberStatus(groupRepository.getById(groupId));
            return responseHandler.handle("Successfully synced group status.", hh.getMediaType());
        }
        List<Long> failedIds = new ArrayList<>();
        Set<Long> groupIds = groupCriteriaQuery.queryAll();
        for (Group group : groupRepository.list(groupIds.toArray(new Long[groupIds.size()]))) {
            try {
                groupRepository.syncMemberStatus(group);
            } catch (Exception ex) {
                failedIds.add(group.getId());
            }
        }
        if (failedIds.size() == 0)
            return responseHandler.handle("Successfully synced all group statuses.", hh.getMediaType());
        else
            return responseHandler.handle("Error occurs when syncing group statuses on id " + Joiner.on(',').join(groupIds) + ".", hh.getMediaType());
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
