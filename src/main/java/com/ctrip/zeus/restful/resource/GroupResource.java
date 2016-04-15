package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.impl.ResultHandler;
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
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
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
    private final int TIMEOUT = 1000;

    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getAllGroups")
    public Response list(@Context HttpHeaders hh,
                         @Context HttpServletRequest request,
                         @QueryParam("slbId") final Long slbId,
                         @QueryParam("appId") final String appId,
                         @QueryParam("vsId") final Long vsId,
                         @TrimmedQueryParam("ip") final String ip,
                         @TrimmedQueryParam("slbName") final String slbName,
                         @TrimmedQueryParam("domain") final String domain,
                         @TrimmedQueryParam("type") String type,
                         @TrimmedQueryParam("tag") final String tag,
                         @TrimmedQueryParam("pname") final String pname,
                         @TrimmedQueryParam("pvalue") final String pvalue,
                         @TrimmedQueryParam("mode") final String mode) throws Exception {
        final SelectionMode selectionMode = SelectionMode.getMode(mode);

        final Long[] vsIdRange = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbId != null || slbName != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Long sId = slbId;
                        if (sId == null && slbName != null) {
                            sId = slbCriteriaQuery.queryByName(slbName);
                        }
                        if (sId != null) {
                            return virtualServerCriteriaQuery.queryBySlbId(sId);
                        }
                        return new HashSet<>();
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return domain != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryByDomain(domain);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbId != null || slbName != null || domain != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST);
                    }
                }).build(IdVersion.class)
                .run(new ResultHandler<IdVersion, Long>() {
                    @Override
                    public Long[] handle(Set<IdVersion> result) throws Exception {
                        if (vsId != null) {
                            if (result == null) return new Long[]{vsId};

                            boolean flag = false;
                            for (IdVersion e : result) {
                                if (e.getId().equals(vsId)) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (!flag)
                                return new Long[0];
                        }
                        return VersionUtils.extractUniqIds(result);
                    }
                });
        if (vsIdRange != null && vsIdRange.length == 0) {
            return responseHandler.handle(new GroupList(), hh.getMediaType());
        }

        final Set<IdVersion> groupFilter = vsIdRange == null ? null : groupCriteriaQuery.queryByVsIds(vsIdRange);
        final Long[] groupIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupFilter != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (IdVersion e : groupFilter) {
                            result.add(e.getId());
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return groupCriteriaQuery.queryAll();
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return appId != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return groupCriteriaQuery.queryByAppId(appId);
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tag != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return new HashSet<>(tagService.query(tag, "group"));
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return pname != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        if (pvalue != null)
                            return new HashSet<>(propertyService.query(pname, pvalue, "group"));
                        else
                            return new HashSet<>(propertyService.query(pname, "group"));
                    }
                }).build(Long.class).run();

        IdVersion[] keys = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupFilter != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return groupFilter;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return ip != null && groupFilter != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return groupCriteriaQuery.queryByGroupServer(ip);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return groupIds.length == 0 ? new HashSet<IdVersion>() : groupCriteriaQuery.queryByIdsAndMode(groupIds, selectionMode);
                    }
                })
                .build(IdVersion.class).run(new ResultHandler<IdVersion, IdVersion>() {
                    @Override
                    public IdVersion[] handle(Set<IdVersion> result) throws Exception {
                        result.removeAll(groupCriteriaQuery.queryAllVGroups());
                        return result.toArray(new IdVersion[result.size()]);
                    }
                });

        GroupList groupList = new GroupList();
        for (Group group : groupRepository.list(keys)) {
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
                                @TrimmedQueryParam("slbName") final String slbName,
                                @TrimmedQueryParam("domain") final String domain,
                                @TrimmedQueryParam("type") String type,
                                @TrimmedQueryParam("tag") final String tag,
                                @TrimmedQueryParam("pname") final String pname,
                                @TrimmedQueryParam("pvalue") final String pvalue,
                                @TrimmedQueryParam("mode") final String mode) throws Exception {
        final SelectionMode selectionMode = SelectionMode.getMode(mode);

        final Long[] vsIdRange = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbId != null || slbName != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        Long sId = slbId;
                        if (sId == null && slbName != null) {
                            sId = slbCriteriaQuery.queryByName(slbName);
                        }
                        if (sId != null) {
                            return virtualServerCriteriaQuery.queryBySlbId(sId);
                        }
                        return new HashSet<>();
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return domain != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryByDomain(domain);
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return slbId != null || slbName != null || domain != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return virtualServerCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST);
                    }
                }).build(IdVersion.class)
                .run(new ResultHandler<IdVersion, Long>() {
                    @Override
                    public Long[] handle(Set<IdVersion> result) throws Exception {
                        return VersionUtils.extractUniqIds(result);
                    }
                });
        if (vsIdRange != null && vsIdRange.length == 0)
            throw new ValidationException("Could not find select condition on virtual server.");

        final Set<IdVersion> groupFilter = vsIdRange == null ? null : groupCriteriaQuery.queryByVsIds(vsIdRange);
        final Long[] groupIds = new QueryExecuter.Builder<Long>()
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupFilter != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        Set<Long> result = new HashSet<>();
                        for (IdVersion e : groupFilter) {
                            result.add(e.getId());
                        }
                        return result;
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return groupCriteriaQuery.queryAllVGroups();
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return tag != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        return new HashSet<>(tagService.query(tag, "group"));
                    }
                })
                .addFilter(new FilterSet<Long>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return pname != null;
                    }

                    @Override
                    public Set<Long> filter() throws Exception {
                        if (pvalue != null)
                            return new HashSet<>(propertyService.query(pname, pvalue, "group"));
                        else
                            return new HashSet<>(propertyService.query(pname, "group"));
                    }
                }).build(Long.class).run();

        QueryExecuter<IdVersion> executer = new QueryExecuter.Builder<IdVersion>()
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return groupFilter != null;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return groupFilter;
                    }
                })
                .addFilter(new FilterSet<IdVersion>() {
                    @Override
                    public boolean shouldFilter() throws Exception {
                        return true;
                    }

                    @Override
                    public Set<IdVersion> filter() throws Exception {
                        return groupIds.length == 0 ? new HashSet<IdVersion>() : groupCriteriaQuery.queryByIdsAndMode(groupIds, selectionMode);
                    }
                }).build(IdVersion.class);

        GroupList groupList = new GroupList();
        for (Group group : groupRepository.list(executer.run())) {
            groupList.addGroup(getGroupByType(group, type));
        }
        groupList.setTotal(groupList.getGroups().size());
        return responseHandler.handle(groupList, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupByStatus")
    public Response get(@Context HttpHeaders hh, @Context HttpServletRequest request,
                        @QueryParam("groupId") Long groupId,
                        @TrimmedQueryParam("groupName") String groupName,
                        @TrimmedQueryParam("type") String type,
                        @TrimmedQueryParam("mode") final String mode) throws Exception {
        if (groupId == null && groupName == null) {
            throw new ValidationException("Missing parameters.");
        }
        if (groupId == null) {
            groupId = groupCriteriaQuery.queryByName(groupName);
        }
        if (groupId == null || groupId.longValue() == 0L) {
            throw new ValidationException("Group cannot be found.");
        }

        IdVersion[] keys = groupCriteriaQuery.queryByIdAndMode(groupId, SelectionMode.getMode(mode));
        List<Group> result = groupRepository.list(keys);
        if (result.size() == 0) throw new ValidationException("Group cannot be found.");
        if (result.size() == 1)
            return responseHandler.handle(getGroupByType(result.get(0), type), hh.getMediaType());
        GroupList groupList = new GroupList().setTotal(result.size());
        for (Group r : result) {
            groupList.addGroup(getGroupByType(r, type));
        }
        return responseHandler.handle(groupList, hh.getMediaType());
    }

    @POST
    @Path("/group/new")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "*/*"})
    @Authorize(name = "addGroup")
    public Response add(@Context HttpHeaders hh, @Context HttpServletRequest request, String group,
                        @QueryParam("force") Boolean force) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        Long groupId = groupCriteriaQuery.queryByName(g.getName());
        if (groupId > 0L) throw new ValidationException("Group name exists.");

        g = groupRepository.add(parseGroup(hh.getMediaType(), group), force == null ? false : force.booleanValue());
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
    public Response update(@Context HttpHeaders hh, @Context HttpServletRequest request, String group,
                           @QueryParam("force") Boolean force) throws Exception {
        Group g = parseGroup(hh.getMediaType(), group);
        DistLock lock = dbLockFactory.newLock(g.getName() + "_updateGroup");
        lock.lock(TIMEOUT);
        try {
            g = groupRepository.update(g, force == null ? false : force.booleanValue());
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
        if (groupId == null || groupId == 0L)
            throw new ValidationException("groupId is not given or does not exist.");
        groupRepository.delete(groupId);
        return responseHandler.handle("Group is deleted.", hh.getMediaType());
    }

    @GET
    @Path("/vgroup/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "deleteGroup")
    public Response deleteVGroup(@Context HttpHeaders hh, @Context HttpServletRequest request, @QueryParam("groupId") Long groupId) throws Exception {
        if (groupId == null)
            throw new Exception("Missing parameter.");
        groupRepository.deleteVGroup(groupId);
        return responseHandler.handle("Virtual group is deleted.", hh.getMediaType());
    }

    @GET
    @Path("/group/upgradeAll")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response upgradeAll(@Context HttpHeaders hh, @Context HttpServletRequest request,
                               @QueryParam("groupId") List<Long> groupId) throws Exception {
        List<Long> list = null;
        if (groupId != null && groupId.size() > 0) {
            list = groupId;
        } else {
            list = new ArrayList<>(groupCriteriaQuery.queryAll());
        }
        Set<Long> result = groupRepository.port(list.toArray(new Long[list.size()]));
        if (result.size() == 0)
            return responseHandler.handle("Upgrade all successfully.", hh.getMediaType());
        else
            return responseHandler.handle("Upgrade fail on ids: " + Joiner.on(",").join(result), hh.getMediaType());
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
