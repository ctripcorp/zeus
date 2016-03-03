package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.task.entity.OpsTask;
import com.ctrip.zeus.task.entity.TaskResult;
import com.ctrip.zeus.task.entity.TaskResultList;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by fanqq on 2015/6/11.
 */

@Component
@Path("/deactivate")
public class DeactivateResource {
    @Resource
    private TagBox tagBox;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ResponseHandler responseHandler;
    @Resource
    private TaskManager taskManager;
    @Resource
    private EntityFactory entityFactory;


    private static DynamicIntProperty lockTimeout = DynamicPropertyFactory.getInstance().getIntProperty("lock.timeout", 5000);
    private static DynamicBooleanProperty writable = DynamicPropertyFactory.getInstance().getBooleanProperty("activate.writable", true);
    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 15000L);


    @GET
    @Path("/group")
    @Authorize(name = "deactivate")
    public Response deactivateGroup(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("groupId") List<Long> groupIds, @QueryParam("groupName") List<String> groupNames) throws Exception {
        List<Long> _groupIds = new ArrayList<>();
        List<Long> _slbIds = new ArrayList<>();

        if (groupIds != null && !groupIds.isEmpty()) {
            _groupIds.addAll(groupIds);
        }
        if (groupNames != null && !groupNames.isEmpty()) {
            for (String groupName : groupNames) {
                Group group = groupRepository.get(groupName);
                if (group == null) {
                    continue;
                }
                _groupIds.add(group.getId());
            }
        }


        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByIds(_groupIds.toArray(new Long[]{}));
        if (groupMap.getOnlineMapping() == null || groupMap.getOnlineMapping().size() == 0) {
            throw new ValidationException("Group is not activated.");
        }

        List<OpsTask> tasks = new ArrayList<>();
        for (Long id : _groupIds) {
            Group group = groupMap.getOnlineMapping().get(id);
            if (group == null) {
                throw new ValidationException("Group is not activated.GroupId:" + id);
            }
            Set<Long> vsIds = new HashSet<>();
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
            ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
            if (vsMap.getOnlineMapping() == null || vsMap.getOnlineMapping().size() == 0) {
                throw new ValidationException("Related Vs is not activated.");
            }
            Set<Long> slbIds = new HashSet<>();
            for (Long vsId : vsMap.getOnlineMapping().keySet()) {
                slbIds.add(vsMap.getOnlineMapping().get(vsId).getSlbId());
            }
            for (Long slbId : slbIds) {
                OpsTask task = new OpsTask();
                task.setGroupId(id);
                task.setOpsType(TaskOpsType.DEACTIVATE_GROUP);
                task.setTargetSlbId(slbId);
                tasks.add(task);
            }
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        List<TaskResult> results = taskManager.getResult(taskIds, apiTimeout.get());

        TaskResultList resultList = new TaskResultList();
        for (TaskResult t : results) {
            resultList.addTaskResult(t);
        }
        resultList.setTotal(results.size());

        try {
            tagBox.tagging("deactive", "group", _groupIds.toArray(new Long[_groupIds.size()]));
            tagBox.untagging("active", "group", _groupIds.toArray(new Long[_groupIds.size()]));
        } catch (Exception ex) {
        }
        return responseHandler.handle(resultList, hh.getMediaType());
    }

    @GET
    @Path("/vs")
    @Authorize(name = "activate")
    public Response deactivateVirtualServer(@Context HttpServletRequest request,
                                            @Context HttpHeaders hh,
                                            @QueryParam("vsId") Long vsId) throws Exception {
        ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByVsIds(new Long[]{vsId});
        if (groupMap.getOnlineMapping() != null && groupMap.getOnlineMapping().size() > 0) {
            throw new ValidationException("Has Activated Groups Related to Vs[" + vsId + "]");
        }
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(new Long[]{vsId});
        if (vsMap.getOnlineMapping() == null || vsMap.getOnlineMapping().get(vsId) == null) {
            throw new ValidationException("Vs is not activated.VsId:" + vsId);
        }
        VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
        OpsTask deactivateTask = new OpsTask();
        deactivateTask.setSlbVirtualServerId(vsId);
        deactivateTask.setCreateTime(new Date());
        deactivateTask.setOpsType(TaskOpsType.DEACTIVATE_VS);
        deactivateTask.setTargetSlbId(vs.getSlbId());
        Long taskId = taskManager.addTask(deactivateTask);

        TaskResult results = taskManager.getResult(taskId, 10000L);
        return responseHandler.handle(results, hh.getMediaType());
    }

    @GET
    @Path("/slb")
    @Authorize(name = "activate")
    public Response deactivateSlb(@Context HttpServletRequest request,
                                  @Context HttpHeaders hh,
                                  @QueryParam("slbId") Long slbId) throws Exception {
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesBySlbIds(slbId);
        if (vsMap.getOnlineMapping() != null && vsMap.getOnlineMapping().size() > 0) {
            throw new ValidationException("Has Activated Vses Related to Slb[" + slbId + "]");
        }
        IdVersion idVersion = new IdVersion(slbId, 0);
        slbRepository.updateStatus(new IdVersion[]{idVersion});

        return responseHandler.handle(slbRepository.getById(slbId), hh.getMediaType());
    }
}
