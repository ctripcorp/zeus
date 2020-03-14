package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.dao.entity.SlbArchiveGroup;
import com.ctrip.zeus.dao.entity.SlbGroupVsR;
import com.ctrip.zeus.dao.entity.SlbGroupVsRExample;
import com.ctrip.zeus.dao.mapper.SlbArchiveGroupMapper;
import com.ctrip.zeus.dao.mapper.SlbGroupVsRMapper;
import com.ctrip.zeus.domain.GroupType;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.SmartArchiveGroupMapper;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.RoleService;
import com.ctrip.zeus.service.auth.UserService;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.model.GroupVirtualServerContent;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.*;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.support.DefaultObjectJsonParser;
import com.ctrip.zeus.support.DefaultObjectJsonWriter;
import com.ctrip.zeus.tag.*;
import com.ctrip.zeus.util.MessageUtil;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by fanqq on 2017/1/9.
 */
@Component
@Path("/fillData")
public class FillDataResource {
    @Resource
    private ResponseHandler responseHandler;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private PropertyService propertyService;
    @Resource
    private CriteriaQueryFactory criteriaQueryFactory;
    @Resource
    private PropertyBox propertyBox;
    @Autowired
    private AppService appService;
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;

    @Resource
    private TagService tagService;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private TaskManager taskManager;
    @Resource
    private MessageQueue messageQueue;
    @Autowired
    private CertificateService certificateService;
    @Resource
    private SlbArchiveGroupMapper slbArchiveGroupMapper;
    @Resource
    private SlbGroupVsRMapper slbGroupVsRMapper;
    @Resource
    private SmartArchiveGroupMapper smartArchiveGroupMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GET
    @Path("/user/auth")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response userAutoFill(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryAll();
        Set<String> appIds = appService.getAppIdsByGroupIds(groupIds.toArray(new Long[groupIds.size()]));
        List<App> apps = appService.getAllAppsByAppIds(appIds);
        Role visitor = roleService.getRole(AuthDefaultValues.SLB_VISITOR_USER);
        for (App app : apps) {
            User user = userService.getUser(app.getOwner());
            if (user == null) {
                user = new User();
                user.setUserName(app.getOwner());
                user.setEmail(app.getOwnerEmail());
                user.setBu(app.getSbu());
                user.addRole(visitor);
                userService.newUser(user);
            }
        }
        return responseHandler.handle("Fill Data Success.", hh.getMediaType());
    }

    @GET
    @Path("/related/slbs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response relatedSlbs(@Context HttpHeaders hh, @Context HttpServletRequest request,
                                @QueryParam("slbId") List<Long> slbIds) throws Exception {
        Set<Long> ids;
        if (slbIds == null || slbIds.size() == 0) {
            ids = slbCriteriaQuery.queryAll();
        } else {
            ids = new HashSet<>(slbIds);
        }
        List<Long> allUpdatedVses = new ArrayList<>();
        List<Long> allTobeActivatedVses = new ArrayList<>();
        for (Long targetSlbId : ids) {
            List<String> tags = tagService.getTags("slb", targetSlbId);

            if (tags == null || tags.size() == 0) continue;

            List<Long> toSlbIds = new ArrayList<>();
            for (String tag : tags) {
                if (tag.startsWith(TagNames.RELATED_SLB_TAG_PREFIX)) {
                    try {
                        toSlbIds.add(Long.parseLong(tag.substring(TagNames.RELATED_SLB_TAG_PREFIX.length())));
                    } catch (Exception e) {
                        logger.warn("Parser Tag Failed.Tag:" + tag + ";SlbId:" + targetSlbId, e);
                    }
                }
            }

            if (toSlbIds.size() == 0) continue;


            Set<IdVersion> idv = virtualServerCriteriaQuery.queryBySlbIds(toSlbIds.toArray(new Long[toSlbIds.size()]));
            if (idv == null || idv.size() == 0) continue;
            Set<Long> fromVsIds = new HashSet<>();
            for (IdVersion idVersion : idv) {
                fromVsIds.add(idVersion.getId());
            }


            //TODO fill data
            List<DistLock> locks = new ArrayList<>();
            try {
                for (Long vsId : fromVsIds) {
                    DistLock lock = dbLockFactory.newLock(vsId + "_updateVs");
                    locks.add(lock);
                    lock.lock(10000);
                }
                ModelStatusMapping<VirtualServer> vses = entityFactory.getVsesByIds(fromVsIds.toArray(new Long[fromVsIds.size()]));
                List<Long> updateVses = new ArrayList<>();
                List<Long> tobeActivatedVses = new ArrayList<>();
                List<OpsTask> tasks = new ArrayList<>();
                for (Long vsId : fromVsIds) {
                    VirtualServer online = vses.getOnlineMapping().get(vsId);
                    VirtualServer offline = vses.getOfflineMapping().get(vsId);
                    if (online == null || !offline.getVersion().equals(online.getVersion())) {
                        tobeActivatedVses.add(offline.getId());
                        continue;
                    }
                    if (!offline.getSlbIds().contains(targetSlbId)) {
                        offline.addValue(targetSlbId);
                        updateVses.add(offline.getId());
                        virtualServerRepository.update(offline);
                        try {
                            String slbMessageData = MessageUtil.getMessageData(request, null, null,
                                    new VirtualServer[]{offline}, null, null, request.getRequestURI(), "/api/vs/update", true);
                            messageQueue.produceMessage("/api/vs/update", vsId, slbMessageData);
                        } catch (Exception e) {

                        }

                        for (Long tmpSlbId : offline.getSlbIds()) {
                            OpsTask task = new OpsTask();
                            task.setSlbVirtualServerId(vsId)
                                    .setTargetSlbId(tmpSlbId)
                                    .setVersion(offline.getVersion())
                                    .setOpsType(TaskOpsType.ACTIVATE_VS)
                                    .setCreateTime(new Date());
                            tasks.add(task);
                        }
                    }
                }
                try {
                    propertyBox.set("status", "toBeActivated", "vs", updateVses.toArray(new Long[updateVses.size()]));
                } catch (Exception ex) {
                }

                allUpdatedVses.addAll(updateVses);
                allTobeActivatedVses.addAll(tobeActivatedVses);

                List<Long> taskIds = taskManager.addAggTask(tasks);
                try {
                    taskManager.getResult(taskIds, 30000L);
                    try {
                        for (Long vsId : updateVses) {
                            String slbMessageData = MessageUtil.getMessageData(request, null, null,
                                    new VirtualServer[]{vses.getOfflineMapping().get(vsId)}, null, null, request.getRequestURI(), "/api/activate/vs", true);
                            messageQueue.produceMessage("/api/activate/vs", vsId, slbMessageData);
                        }
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                    try {
                        for (Long vsId : updateVses) {
                            String slbMessageData = MessageUtil.getMessageData(request, null, null,
                                    new VirtualServer[]{vses.getOfflineMapping().get(vsId)}, null, null, request.getRequestURI(), "/api/activate/vs", false);
                            messageQueue.produceMessage("/api/activate/vs", vsId, slbMessageData);
                        }
                    } catch (Exception e2) {
                    }
                    throw new Exception("Updated VsId:" + updateVses.toString() + " toBeActivatedVsIds:" + tobeActivatedVses.toString() + e.getMessage(), e);
                }
                try {
                    propertyBox.set("status", "activated", "vs", updateVses.toArray(new Long[updateVses.size()]));
                } catch (Exception ex) {
                }

            } finally {
                for (DistLock lock : locks) {
                    if (lock != null) lock.unlock();
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("updated", allUpdatedVses);
        result.put("toBeActivatedVsIds", allTobeActivatedVses);
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/group/type")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response fillGroupType(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        QueryEngine queryRender = new QueryEngine(new ArrayDeque<String[]>(0), "vgroup", SelectionMode.OFFLINE_FIRST);
        queryRender.init(true);
        IdVersion[] searchKeys = queryRender.run(criteriaQueryFactory);

        List<Group> groups = getGroups(searchKeys);
        Map<Long, Group> groupIdMapping = new HashMap<>(groups.size());
        Long[] groupIds = new Long[groups.size()];
        for (int i = 0; i < groups.size(); ++i) {
            Group group = groups.get(i);
            groupIds[i] = group.getId();
            groupIdMapping.put(group.getId(), group);
        }
        ModelStatusMapping<Group> groupModelStatusMapping = entityFactory.getGroupsByIds(groupIds);

        List<Long> updatedGroupIds = new ArrayList<>(groups.size());
        List<Long> toBeActivatedGroupIds = new ArrayList<>(groups.size());
        List<Long> notActivatedGroupIds = new ArrayList<>();
        List<OpsTask> tasks = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        final String vgroupType = GroupType.VGROUP.toString();
        for (Group group : groups) {
            // The type field has been filled. Skip it.
            if (vgroupType.equals(group.getType())) {
                continue;
            }

            // The VirtualServer object will be overwritten by GroupRepository.updateVGroup call.
            // We shall make a backup here.
            List<VirtualServer> vsList = new ArrayList<>(group.getGroupVirtualServers().size());
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                vsList.add(gvs.getVirtualServer());
            }

            // Make updates
            Long groupId = group.getId();
            group.setType(vgroupType);
            DistLock lock = dbLockFactory.newLock(group.getId() + "_updateGroup");
            lock.lock(1000);
            try {
                groupRepository.updateVGroup(group, true);
            } catch (Exception ex) {
                errors.add(String.format("Update group #%d failed: %s-%s", group.getId(), ex.getClass().getSimpleName(),
                        ex.getMessage()));
                continue;
            } finally {
                lock.unlock();
            }

            propertyBox.set(PropertyNames.GROUP_TYPE, group.getType(), ItemTypes.GROUP, groupId);
            updatedGroupIds.add(groupId);

            // Generate log for the update operation
            try {
                String slbMessageData = MessageUtil.getMessageData(request, new Group[]{group}, null, null, null, null,
                        request.getRequestURI(), "/api/group/update", true);
                messageQueue.produceMessage("/api/group/update", groupId, slbMessageData);
            } catch (Exception e) {
                errors.add("Produce update message failed for " + group.getId() + ": " + e.getClass().getName() + " "
                        + e.getMessage());
            }

            // If the group is not already fully activated, we shall not activate it, either.
            Group online = groupModelStatusMapping.getOnlineMapping().get(groupId);
            Group offline = groupModelStatusMapping.getOfflineMapping().get(groupId);
            if (online == null || !offline.getVersion().equals(online.getVersion())) {
                notActivatedGroupIds.add(groupId);
                continue;
            }

            // Build tasks to activate the group
            toBeActivatedGroupIds.add(groupId);
            for (VirtualServer vs : vsList) {
                List<Long> slbIds = vs.getSlbIds();
                if (slbIds == null) {
                    continue;
                }
                for (Long slbId : slbIds) {
                    OpsTask task = new OpsTask();
                    task.setGroupId(groupId).setTargetSlbId(slbId).setVersion(group.getVersion())
                            .setOpsType(TaskOpsType.ACTIVATE_GROUP).setCreateTime(new Date()).setSkipValidate(true);
                    tasks.add(task);
                }
            }
        }

        if (!toBeActivatedGroupIds.isEmpty()) {
            try {
                propertyBox.set("status", "toBeActivated", ItemTypes.GROUP, toBeActivatedGroupIds.toArray(new Long[0]));
            } catch (Exception e) {
                errors.add("Set status to toBeActivated failed : " + e.getClass().getName() + " " + e.getMessage());
            }

            if (!tasks.isEmpty()) {
                // Save tasks to DB and wait for them until finished.
                List<Long> taskIds = taskManager.addAggTask(tasks);
                Exception exception = null;
                try {
                    taskManager.getResult(taskIds, 30000L);
                } catch (Exception e) {
                    exception = e;
                    errors.add("Waiting for activation task to be completed failed : " + e.getClass().getName() + " "
                            + e.getMessage());
                }

                // Generate logs for activate operations.
                for (Long groupId : toBeActivatedGroupIds) {
                    try {
                        String slbMessageData = MessageUtil.getMessageData(request,
                                new Group[]{groupIdMapping.get(groupId)}, null, null, null, null,
                                request.getRequestURI(), "/api/activate/group", exception == null);
                        messageQueue.produceMessage("/api/activate/group", groupId, slbMessageData);
                    } catch (Exception e) {
                        errors.add("Produce activate message failed for " + groupId + ": " + e.getClass().getName()
                                + " " + e.getMessage());
                    }
                }

                try {
                    propertyBox.set("status", "activated", ItemTypes.GROUP, toBeActivatedGroupIds.toArray(new Long[0]));
                } catch (Exception e) {
                    errors.add("Set status to activated failed : " + e.getClass().getName() + " " + e.getMessage());
                }
            }
        }

        Map<String, Object> response = ImmutableMap.of("success", Boolean.TRUE, "updatedGroups", updatedGroupIds,
                "notActivateGroups", notActivatedGroupIds, "errors", errors);
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/group/type/validate")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateGroupType(@Context HttpHeaders hh, @Context HttpServletRequest request) throws Exception {
        QueryEngine queryRender = new QueryEngine(new ArrayDeque<String[]>(0), "group", SelectionMode.OFFLINE_FIRST);
        queryRender.init(true);
        IdVersion[] groupSearchKeys = queryRender.run(criteriaQueryFactory);
        queryRender = new QueryEngine(new ArrayDeque<String[]>(0), "vgroup", SelectionMode.OFFLINE_FIRST);
        queryRender.init(true);
        IdVersion[] vgroupSearchKeys = queryRender.run(criteriaQueryFactory);

        IdVersion[] searchKeys = (IdVersion[]) ArrayUtils.addAll(groupSearchKeys, vgroupSearchKeys);
        List<Group> groups = getGroups(searchKeys);

        Long[] groupIds = new Long[groups.size()];
        for (int i = 0; i < groups.size(); ++i) {
            groupIds[i] = groups.get(i).getId();
        }
        Map<Long, Property> groupTypeProperties =
                propertyService.getProperties(PropertyNames.GROUP_TYPE, ItemTypes.GROUP, groupIds);

        List<Map<String, Object>> errorList = new ArrayList<>();
        String vgroupType = GroupType.VGROUP.toString(), groupType = GroupType.GROUP.toString();
        for (Group group : groups) {
            List<String> errors = null;
            String message;
            if (group.isVirtual()) {
                if (!vgroupType.equals(group.getType())) {
                    message = String.format("Incorrect type field. Expected=%s Actual=%s", vgroupType, group.getType());
                    (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                }
                Property groupTypeProperty = groupTypeProperties.get(group.getId());
                if (groupTypeProperty == null) {
                    (errors != null ? errors : (errors = new ArrayList<>())).add("Missing groupType property.");
                } else if (!vgroupType.equals(groupTypeProperty.getValue())) {
                    message = String.format("Incorrect groupType property. Expected=%s Actual=%s", vgroupType,
                            groupTypeProperty.getValue());
                    (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                }
            } else {
                if (group.getType() != null && !group.getType().isEmpty() && !groupType.equals(group.getType())) {
                    message = String.format("Incorrect type field. Expected=%s Actual=%s", groupType, group.getType());
                    (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                }
                Property groupTypeProperty = groupTypeProperties.get(group.getId());
                if (groupTypeProperty != null && !groupType.equals(groupTypeProperty.getValue())) {
                    message = String.format("Incorrect groupType property. Expected=%s Actual=%s", groupType,
                            groupTypeProperty.getValue());
                    (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                }
            }

            if (errors != null) {
                errorList.add(ImmutableMap.of("groupId", group.getId(), "errors", errors));
            }
        }

        Map<String, Object> response = ImmutableMap.of("success", Boolean.TRUE, "errorList", errorList);
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/group/rgvsContent")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response fillGroupVirtualServerContent(@Context HttpHeaders hh, @Context HttpServletRequest request)
            throws Exception {

        int updatedCount = 0;
        List<String> failedItems = new ArrayList<>();

        List<SlbGroupVsR> rgvsObjects = slbGroupVsRMapper.selectByExample(new SlbGroupVsRExample().createCriteria().example());
        List<SlbGroupVsR> objectNeededToUpdate = new ArrayList<>(rgvsObjects.size());
        for (SlbGroupVsR rgvsObject : rgvsObjects) {
            boolean needUpdate = true;
            GroupVirtualServerContent gvsContent;
            if (rgvsObject.getContent() != null) {
                gvsContent = DefaultObjectJsonParser.parse(rgvsObject.getContent(), GroupVirtualServerContent.class);
                if (gvsContent == null) {
                    needUpdate = true;
                    gvsContent = new GroupVirtualServerContent();
                } else {
                    needUpdate = (gvsContent.getGroupId() == null || !gvsContent.getGroupId().equals(rgvsObject.getGroupId()))
                            || (gvsContent.getGroupVersion() == null || !gvsContent.getGroupVersion().equals(rgvsObject.getGroupVersion()))
                            || (gvsContent.getVirtualServer() == null || !rgvsObject.getVsId().equals(gvsContent.getVirtualServer().getId()))
                            || (gvsContent.getPath() == null || !gvsContent.getPath().equals(rgvsObject.getPath()))
                            || (gvsContent.getPriority() == null || !gvsContent.getPriority().equals(rgvsObject.getPriority()));
                }
            } else {
                gvsContent = new GroupVirtualServerContent();
            }
            if (!needUpdate) {
                continue;
            }
            gvsContent.setGroupId(rgvsObject.getGroupId()).setGroupVersion(rgvsObject.getGroupVersion())
                    .setVirtualServer(new VirtualServer().setId(rgvsObject.getVsId())).setPath(rgvsObject.getPath())
                    .setPriority(rgvsObject.getPriority());
            rgvsObject.setContent(DefaultObjectJsonWriter.write(gvsContent).getBytes(StandardCharsets.UTF_8));
            objectNeededToUpdate.add(rgvsObject);
        }


        for (SlbGroupVsR item : objectNeededToUpdate) {
            int i = slbGroupVsRMapper.updateByExample(item, new SlbGroupVsRExample().createCriteria().andIdEqualTo(item.getId()).andGroupIdEqualTo(item.getGroupId()).andVsIdEqualTo(item.getVsId()).andGroupVersionEqualTo(item.getGroupVersion()).example());
            if (i == 1) {
                ++updatedCount;
                continue;
            }
            SlbGroupVsR rgvs = objectNeededToUpdate.get(i);
            failedItems.add(rgvs.getGroupId() + "_" + rgvs.getVsId() + "_" + rgvs.getGroupVersion());
        }
        Map<String, Object> response =
                ImmutableMap.of("success", Boolean.TRUE, "updatedCount", updatedCount, "failedItems", failedItems);
        return responseHandler.handle(response, hh.getMediaType());
    }

    @GET
    @Path("/group/rgvsContent/validate")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response validateGroupVirtualServerContent(@Context HttpHeaders hh, @Context HttpServletRequest request)
            throws Exception {
        List<Map<String, Object>> errorList = new ArrayList<>();

        List<SlbGroupVsR> slbGroupVsRS = slbGroupVsRMapper.selectByExampleWithBLOBs(new SlbGroupVsRExample());
        for (SlbGroupVsR rgvsObject : slbGroupVsRS) {
            List<String> errors = null;
            String message;
            GroupVirtualServerContent gvsContent;
            if (rgvsObject.getContent() != null) {
                gvsContent = DefaultObjectJsonParser.parse(rgvsObject.getContent(), GroupVirtualServerContent.class);
                if (gvsContent == null) {
                    message = String.format("Invalid GvsContent value: %s",
                            new String(rgvsObject.getContent(), StandardCharsets.UTF_8));
                    (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                } else {
                    if (gvsContent.getGroupId() == null) {
                        message = "Missing group ID in GvsContent.";
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    } else if (!gvsContent.getGroupId().equals(rgvsObject.getGroupId())) {
                        message = String.format("Incorrect group ID field value. Expected=%s Actual=%s",
                                rgvsObject.getGroupId(), gvsContent.getGroupId());
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    }
                    if (gvsContent.getGroupVersion() == null) {
                        message = "Missing group version in GvsContent.";
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    } else if (!gvsContent.getGroupVersion().equals(rgvsObject.getGroupVersion())) {
                        message = String.format("Incorrect group version field value. Expected=%s Actual=%s",
                                rgvsObject.getGroupVersion(), gvsContent.getGroupVersion());
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    }
                    if (gvsContent.getVirtualServer() == null || gvsContent.getVirtualServer().getId() == null) {
                        message = "Missing VS info in GvsContent.";
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    } else if (!gvsContent.getVirtualServer().getId().equals(rgvsObject.getVsId())) {
                        message = String.format("Incorrect VS ID field value. Expected=%s Actual=%s",
                                rgvsObject.getVsId(), gvsContent.getVirtualServer().getId());
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    }
                    if (gvsContent.getPath() == null) {
                        message = "Missing path in GvsContent.";
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    } else if (!gvsContent.getPath().equals(rgvsObject.getPath())) {
                        message = String.format("Incorrect path field value. Expected=%s Actual=%s",
                                rgvsObject.getPath(), gvsContent.getPath());
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    }
                    if (gvsContent.getPriority() == null) {
                        message = "Missing priority in GvsContent.";
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    } else if (!gvsContent.getPriority().equals(rgvsObject.getPriority())) {
                        message = String.format("Incorrect priority field value. Expected=%s Actual=%s",
                                rgvsObject.getPriority(), gvsContent.getPriority());
                        (errors != null ? errors : (errors = new ArrayList<>())).add(message);
                    }
                }
            } else {
                message = "Missing GvsContent";
                (errors != null ? errors : (errors = new ArrayList<>())).add(message);
            }
            if (errors != null) {
                String gvsv = rgvsObject.getGroupId() + "_" + rgvsObject.getVsId() + "_" + rgvsObject.getGroupVersion();
                errorList.add(ImmutableMap.of("gvsv", gvsv, "errors", errors));
            }
        }

        Map<String, Object> response = ImmutableMap.of("success", Boolean.TRUE, "errorList", errorList);
        return responseHandler.handle(response, hh.getMediaType());
    }


    private List<Group> getGroups(IdVersion[] keys) throws Exception {
        List<Group> groups = new ArrayList<>(keys.length);
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }
        for (SlbArchiveGroup d : smartArchiveGroupMapper.findAllByIdVersion(Arrays.asList(hashes), Arrays.asList(values))) {
            try {
                Group group = ContentReaders.readGroupContent(d.getContent());
                group.setCreatedTime(d.getDatachangeLasttime());
                groups.add(group);
            } catch (Exception e) {
            }
        }

        Set<Long> vsIds = new HashSet<>();
        for (Group group : groups) {
            for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                vsIds.add(groupVirtualServer.getVirtualServer().getId());
            }
        }

        Map<Long, VirtualServer> map = buildVsMapping(vsIds.toArray(new Long[0]));
        for (Group group : groups) {
            for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                groupVirtualServer.setVirtualServer(map.get(groupVirtualServer.getVirtualServer().getId()));
            }
        }
        return groups;
    }

    private Map<Long, VirtualServer> buildVsMapping(Long[] vsIds) throws Exception {
        Set<IdVersion> vsKeys = virtualServerCriteriaQuery.queryByIdsAndMode(vsIds, SelectionMode.OFFLINE_FIRST);
        return Maps.uniqueIndex(virtualServerRepository.listAll(vsKeys.toArray(new IdVersion[0])),
                new Function<VirtualServer, Long>() {
                    @Override
                    public Long apply(VirtualServer virtualServer) {
                        return virtualServer != null ? virtualServer.getId() : null;
                    }
                });
    }
}
