package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.auth.AuthDefaultValues;
import com.ctrip.zeus.service.auth.AuthService;
import com.ctrip.zeus.service.auth.ResourceDataType;
import com.ctrip.zeus.service.auth.ResourceOperationType;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.UserUtils;
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
import java.util.*;

@Component
@Path("/flow/vs/migration")
public class FlowVsMigrationResource {


    @Resource
    private AuthService authService;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private TaskManager taskManager;
    @Resource
    private ResponseHandler responseHandler;

    private final static int TIMEOUT = 1000;

    @GET
    @Path("/bindSlb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response bindSlb(@Context HttpHeaders hh,
                            @Context final HttpServletRequest request,
                            @QueryParam("vsId") List<Long> vsIds,
                            @QueryParam("sourceSlbId") Long sourceSlbId,
                            @QueryParam("targetSlbId") Long targetSlbId) throws Exception {
        if (vsIds == null || vsIds.size() == 0 || sourceSlbId == null || targetSlbId == null) {
            throw new ValidationException("Invalidate Params.");
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Flow, AuthDefaultValues.ALL);
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[vsIds.size()]));
        vsIds.retainAll(vsMap.getOfflineMapping().keySet());
        if (!vsMap.getOnlineMapping().keySet().containsAll(vsIds)) {
            vsIds.removeAll(vsMap.getOnlineMapping().keySet());
            throw new ValidationException("Missing Online Vses.vsIds:" + vsIds);
        }
        Set<Long> toBeActivatedVses = new HashSet<>();
        Set<Long> updatedVsids = new HashSet<>();
        Set<VirtualServer> updatedVses = new HashSet<>();
        for (Long id : vsMap.getOnlineMapping().keySet()) {
            VirtualServer online = vsMap.getOnlineMapping().get(id);
            VirtualServer offline = vsMap.getOfflineMapping().get(id);
            if (online.getSlbIds().contains(targetSlbId)) {
                continue;
            }
            if (!online.getVersion().equals(offline.getVersion())) {
                toBeActivatedVses.add(id);
                if (offline.getSlbIds().contains(targetSlbId)) {
                    updatedVsids.add(offline.getId());
                    updatedVses.add(offline);
                }
                continue;
            }

            DistLock lock = dbLockFactory.newLock(online.getId() + "_updateVs");
            lock.lock(TIMEOUT);
            try {
                offline.addValue(targetSlbId);
                offline = virtualServerRepository.update(offline);
                updatedVsids.add(offline.getId());
                updatedVses.add(offline);
                try {
                    propertyBox.set("status", "toBeActivated", "vs", online.getId());
                } catch (Exception ex) {
                }
            } finally {
                lock.unlock();
            }
            String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri("/api/vs/update").bindVses(new VirtualServer[]{offline}).build();
            messageQueue.produceMessage("/api/vs/update", offline.getId(), slbMessageData);
        }
        activateVsids(request, updatedVses, null);
        Map<String, Set<Long>> result = new HashMap<>();
        result.put("updatedVses", updatedVsids);
        result.put("activatedVses", updatedVsids);
        result.put("toBeActivatedVses", toBeActivatedVses);
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/unbindSlb")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response unbindSlb(@Context HttpHeaders hh,
                              @Context final HttpServletRequest request,
                              @QueryParam("vsId") List<Long> vsIds,
                              @QueryParam("sourceSlbId") Long sourceSlbId,
                              @QueryParam("targetSlbId") Long targetSlbId) throws Exception {
        if (vsIds == null || vsIds.size() == 0 || sourceSlbId == null || targetSlbId == null) {
            throw new ValidationException("Invalidate Params.");
        }
        authService.authValidate(UserUtils.getUserName(request), ResourceOperationType.NEW, ResourceDataType.Flow, AuthDefaultValues.ALL);
        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[vsIds.size()]));
        vsIds.retainAll(vsMap.getOfflineMapping().keySet());
        if (!vsMap.getOnlineMapping().keySet().containsAll(vsIds)) {
            vsIds.removeAll(vsMap.getOnlineMapping().keySet());
            throw new ValidationException("Missing Online Vses.vsIds:" + vsIds);
        }
        Set<Long> toBeActivatedVses = new HashSet<>();
        Set<Long> updatedVsids = new HashSet<>();
        Set<VirtualServer> updatedVses = new HashSet<>();
        for (Long id : vsMap.getOnlineMapping().keySet()) {
            VirtualServer online = vsMap.getOnlineMapping().get(id);
            VirtualServer offline = vsMap.getOfflineMapping().get(id);
            if (!online.getSlbIds().contains(sourceSlbId)) {
                continue;
            }
            if (!online.getVersion().equals(offline.getVersion())) {
                toBeActivatedVses.add(id);
                if (!offline.getSlbIds().contains(sourceSlbId) && offline.getSlbIds().contains(targetSlbId)) {
                    updatedVses.add(offline);
                    updatedVsids.add(offline.getId());
                }
                continue;
            }
            DistLock lock = dbLockFactory.newLock(online.getId() + "_updateVs");
            lock.lock(TIMEOUT);
            try {
                offline.getSlbIds().remove(sourceSlbId);
                offline = virtualServerRepository.update(offline);
                updatedVsids.add(offline.getId());
                updatedVses.add(offline);
                try {
                    propertyBox.set("status", "toBeActivated", "vs", online.getId());
                } catch (Exception ex) {
                }
            } finally {
                lock.unlock();
            }
            String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri("/api/vs/update").bindVses(new VirtualServer[]{offline}).build();
            messageQueue.produceMessage("/api/vs/update", offline.getId(), slbMessageData);
        }
        activateVsids(request, updatedVses, sourceSlbId);
        Map<String, Set<Long>> result = new HashMap<>();
        result.put("updatedVses", updatedVsids);
        result.put("activatedVses", updatedVsids);
        result.put("toBeActivatedVses", toBeActivatedVses);
        return responseHandler.handle(result, hh.getMediaType());
    }


    private void activateVsids(HttpServletRequest request, Set<VirtualServer> updatedVses, Long sourceSlbId) throws Exception {
        List<Long> taskIds = new ArrayList<>();
        Map<Long, VirtualServer> vsIds = new HashMap<>();
        for (VirtualServer offlineVersion : updatedVses) {
            Set<Long> offlineRelatedSlbIds = new HashSet<>(offlineVersion.getSlbIds());
            vsIds.put(offlineVersion.getId(), offlineVersion);
            ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(offlineRelatedSlbIds.toArray(new Long[offlineRelatedSlbIds.size()]));
            List<OpsTask> tasks = new ArrayList<>();
            for (Long slbId : offlineRelatedSlbIds) {
                Slb slb = slbMap.getOnlineMapping().get(slbId);
                if (slb == null) {
                    if (offlineRelatedSlbIds.contains(slbId)) {
                        throw new ValidationException("Slb " + slbId + " is found deactivated.");
                    } else {
                        throw new Exception("Slb " + slbId + " is found deactivated of an online vs. VsId=" + offlineVersion.getId());
                    }
                }
                OpsTask task = new OpsTask();
                task.setSlbVirtualServerId(offlineVersion.getId())
                        .setTargetSlbId(slbId)
                        .setVersion(offlineVersion.getVersion())
                        .setOpsType(TaskOpsType.ACTIVATE_VS)
                        .setCreateTime(new Date());
                tasks.add(task);
                if (sourceSlbId != null) {
                    task = new OpsTask();
                    task.setSlbVirtualServerId(offlineVersion.getId())
                            .setTargetSlbId(sourceSlbId)
                            .setVersion(offlineVersion.getVersion())
                            .setOpsType(TaskOpsType.SOFT_DEACTIVATE_VS)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            }
            taskIds.addAll(taskManager.addAggTask(tasks));
        }
        taskManager.getResult(taskIds, 60000L);
        try {
            propertyBox.set("status", "activated", "vs", vsIds.keySet().toArray(new Long[vsIds.size()]));
        } catch (Exception ex) {
        }
        for (Long vsId : vsIds.keySet()) {
            List<VirtualServer> vsList = new ArrayList<>();
            vsList.add(vsIds.get(vsId));
            vsList.add(new VirtualServer().setId(vsId).setVersion(vsIds.get(vsId).getVersion() - 1));

            String slbMessageData = MessageUtil.getMessageBuilder(request, true).bindUri("/api/activate/vs").bindVses(vsList.toArray(new VirtualServer[2])).build();
            messageQueue.produceMessage("/api/activate/vs", vsId, slbMessageData);
        }
    }
}
