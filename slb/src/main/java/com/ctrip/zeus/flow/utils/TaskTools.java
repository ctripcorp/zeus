package com.ctrip.zeus.flow.utils;

import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.service.task.constant.TaskStatus;
import com.ctrip.zeus.support.DefaultObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("taskTools")
public class TaskTools {

    @Resource
    private EntityFactory entityFactory;
    @Resource
    private TaskManager taskManager;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private ConfigHandler configHandler;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private DrRepository drRepository;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private MessageQueue messageQueue;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Activate Groups and Policies and Drs
     * Exception in case of Failed.
     * **/
    public void activateEntity(List<Group> groupList, List<TrafficPolicy> policyList, List<Dr> drList, List<VirtualServer> vses) throws Exception {
        Set<Long> vsIds = new HashSet<>();
        Map<Group, List<Long>> groupVsMap = new HashMap<>();
        Map<TrafficPolicy, List<Long>> policyVsMap = new HashMap<>();
        Map<Dr, List<Long>> drVsMap = new HashMap<>();

        List<Long> gids = new ArrayList<>();
        List<Long> pids = new ArrayList<>();
        List<Long> drids = new ArrayList<>();

        if (groupList != null) {
            for (Group g : groupList) {
                gids.add(g.getId());
                if (!groupVsMap.containsKey(g)) {
                    groupVsMap.put(g, new ArrayList<Long>());
                }
                for (GroupVirtualServer vs : g.getGroupVirtualServers()) {
                    vsIds.add(vs.getVirtualServer().getId());
                    groupVsMap.get(g).add(vs.getVirtualServer().getId());
                }
            }
        }

        if (policyList != null) {
            for (TrafficPolicy p : policyList) {
                pids.add(p.getId());
                if (!policyVsMap.containsKey(p)) {
                    policyVsMap.put(p, new ArrayList<Long>());
                }
                for (PolicyVirtualServer vs : p.getPolicyVirtualServers()) {
                    vsIds.add(vs.getVirtualServer().getId());
                    policyVsMap.get(p).add(vs.getVirtualServer().getId());
                }
            }
        }

        if (drList != null) {
            for (Dr dr : drList) {
                drids.add(dr.getId());
                if (!drVsMap.containsKey(dr)) {
                    drVsMap.put(dr, new ArrayList<Long>());
                }
                for (DrTraffic drt : dr.getDrTraffics()) {
                    for (Destination des : drt.getDestinations()) {
                        vsIds.add(des.getVirtualServer().getId());
                        drVsMap.get(dr).add(des.getVirtualServer().getId());
                    }
                }
            }
        }

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds.toArray(new Long[vsIds.size()]));

        List<OpsTask> tasks = new ArrayList<>();

        for (Group g : groupVsMap.keySet()) {
            for (Long vsId : groupVsMap.get(g)) {
                if (!vsMap.getOnlineMapping().containsKey(vsId)) continue;
                for (Long slbId : vsMap.getOnlineMapping().get(vsId).getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setGroupId(g.getId())
                            .setTargetSlbId(slbId)
                            .setOpsType(TaskOpsType.ACTIVATE_GROUP)
                            .setVersion(g.getVersion())
                            .setSkipValidate(true)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            }
        }

        for (TrafficPolicy policy : policyVsMap.keySet()) {
            for (Long vsId : policyVsMap.get(policy)) {
                if (!vsMap.getOnlineMapping().containsKey(vsId)) continue;
                for (Long slbId : vsMap.getOnlineMapping().get(vsId).getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setPolicyId(policy.getId())
                            .setTargetSlbId(slbId)
                            .setOpsType(TaskOpsType.ACTIVATE_POLICY)
                            .setVersion(policy.getVersion())
                            .setSkipValidate(true)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            }
        }

        for (Dr dr : drVsMap.keySet()) {
            for (Long vsId : drVsMap.get(dr)) {
                if (!vsMap.getOnlineMapping().containsKey(vsId)) continue;
                for (Long slbId : vsMap.getOnlineMapping().get(vsId).getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setDrId(dr.getId())
                            .setTargetSlbId(slbId)
                            .setOpsType(TaskOpsType.ACTIVATE_DR)
                            .setVersion(dr.getVersion())
                            .setSkipValidate(true)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            }
        }

        List<Long> vids = new ArrayList<>();
        if (vses != null) {
            for (VirtualServer vs : vses) {
                vids.add(vs.getId());
                for (Long slbId : vs.getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setSlbVirtualServerId(vs.getId())
                            .setTargetSlbId(slbId)
                            .setOpsType(TaskOpsType.ACTIVATE_VS)
                            .setVersion(vs.getVersion())
                            .setSkipValidate(true)
                            .setCreateTime(new Date());
                    tasks.add(task);
                }
            }
        }

        List<Long> taskIds = taskManager.addAggTask(tasks);
        taskManager.getAggResult(taskIds, (long) configHandler.getIntValue("agg.task.timeout", 30000));

        if (groupList != null) {
            for (Group g : groupList) {
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/activate/group",
                        "自动流程", true).bindGroups(new Group[]{g}).build();
                messageQueue.produceMessage("/api/activate/group", g.getId(), slbMessageData);
            }
        }

        if (policyList != null) {
            for (TrafficPolicy p : policyList) {
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/activate/policy",
                        "自动流程", true).bindPolicies(new TrafficPolicy[]{p}).build();
                messageQueue.produceMessage("/api/activate/policy", p.getId(), slbMessageData);
            }
        }

        if (drList != null) {
            for (Dr dr : drList) {
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/activate/dr",
                        "自动流程", true).bindDrs(new Dr[]{dr}).build();
                messageQueue.produceMessage("/api/activate/dr", dr.getId(), slbMessageData);
            }
        }

        if (vses != null) {
            for (VirtualServer vs : vses) {
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/activate/vs",
                        "自动流程", true).bindVses(new VirtualServer[]{vs}).build();
                messageQueue.produceMessage("/api/activate/vs", vs.getId(), slbMessageData);
            }
        }

        setProperty("status", "activated", "group", gids.toArray(new Long[gids.size()]));
        setProperty("status", "activated", "policy", pids.toArray(new Long[pids.size()]));
        setProperty("status", "activated", "dr", drids.toArray(new Long[drids.size()]));
        setProperty("status", "activated", "vs", vids.toArray(new Long[vids.size()]));
    }

    public void activateSyncVses(List<VirtualServer> vses) throws Exception {
        List<Long> vids = new ArrayList<>();
        if (vses == null) {
            return;
        }
        List<OpsTask> tasks = new ArrayList<>();
        Map<Long, List<OpsTask>> map = new HashMap<>();
        for (VirtualServer vs : vses) {
            vids.add(vs.getId());
            for (Long slbId : vs.getSlbIds()) {
                OpsTask task = new OpsTask();
                task.setSlbVirtualServerId(vs.getId())
                        .setTargetSlbId(slbId)
                        .setOpsType(TaskOpsType.ACTIVATE_VS)
                        .setVersion(vs.getVersion())
                        .setSkipValidate(true)
                        .setCreateTime(new Date());
                List<OpsTask> l = map.get(task.getTargetSlbId());
                if (l == null) {
                    l = new ArrayList<>();
                    map.put(task.getTargetSlbId(), l);
                }
                task.setStatus(TaskStatus.PENDING);
                l.add(task);
            }
        }


        for (Long id : map.keySet()) {
            OpsTask tmpTask = new OpsTask();
            tmpTask.setTargetSlbId(id);
            tmpTask.setOpsType(TaskOpsType.ACTIVATE_SYNC_VSES);
            tmpTask.setTaskList(DefaultObjectJsonWriter.write(map.get(id)));
            tasks.add(tmpTask);
        }
        List<Long> taskIds = taskManager.addTask(tasks);
        taskManager.getResult(taskIds, (long) configHandler.getIntValue("agg.task.timeout", 30000));

        for (VirtualServer vs : vses) {
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/activate/vs",
                    "自动流程", true).bindVses(new VirtualServer[]{vs}).build();
            messageQueue.produceMessage("/api/activate/vs", vs.getId(), slbMessageData);
        }
        setProperty("status", "activated", "vs", vids.toArray(new Long[vids.size()]));
    }

    private void setProperty(String pname, String pvalue, String type, Long[] ids) {
        try {
            if (ids.length > 0) {
                propertyBox.set(pname, pvalue, type, ids);
            }
        } catch (Exception ex) {
            logger.error("[[Fail=SetProperty]] Set Property Failed.PName:" + pname + ";Pvalue:" + pvalue + ";type:" + type + ";Ids:" + ids);
        }
    }

    public void bindToNewVs(VirtualServer vs, List<Long> vsIds) throws Exception {
        List<Long> newVsIds = new ArrayList<>(vsIds);
        newVsIds.remove(vs.getId());
        ModelStatusMapping<Group> groups = entityFactory.getGroupsByVsIds(new Long[]{vs.getId()});
        List<Group> groupList = new ArrayList<>();
        for (Group g : groups.getOfflineMapping().values()) {
            g = groupBindVs(g, vs, newVsIds);
            if (g != null) {
                groupList.add(g);
            }
        }

        List<TrafficPolicy> policyList = new ArrayList<>();
        ModelStatusMapping<TrafficPolicy> policyMap = entityFactory.getPoliciesByVsIds(new Long[]{vs.getId()});
        for (TrafficPolicy p : policyMap.getOfflineMapping().values()) {
            p = policyBindVs(p, vs, newVsIds);
            if (p != null) {
                policyList.add(p);
            }
        }

        List<Dr> drList = new ArrayList<>();
        ModelStatusMapping<Dr> drMap = entityFactory.getDrsByVsIds(new Long[]{vs.getId()});
        for (Dr d : drMap.getOfflineMapping().values()) {
            d = drBindVs(d, vs, newVsIds);
            if (d != null) {
                drList.add(d);
            }
        }

        activateEntity(groupList, policyList, drList, null);
    }


    /**
     * Bind Dr To New Vses
     * Return updated dr in success.
     * Return null in case of no need to update.
     * Exception in case of Failed.
     * **/
    private Dr drBindVs(Dr d, VirtualServer vs, List<Long> newVsIds) throws Exception {
        Destination orgDes = null;
        boolean changed = false;
        for (DrTraffic drTraffic : d.getDrTraffics()) {
            List<Long> vsIds = new ArrayList<>();
            for (Destination des : drTraffic.getDestinations()) {
                if (des.getVirtualServer().getId().equals(vs.getId())) {
                    orgDes = des;
                } else {
                    vsIds.add(des.getVirtualServer().getId());
                }
            }
            if (orgDes == null) continue;
            for (Long id : newVsIds) {
                if (vsIds.contains(id)) continue;
                Destination tmp = new Destination();
                tmp.setVirtualServer(orgDes.getVirtualServer());
                tmp.getControls().addAll(orgDes.getControls());
                drTraffic.addDestination(tmp);
                changed = true;
            }
        }
        if (!changed) return null;

        DistLock lock = dbLockFactory.newLock(d.getId() + "_updateDr");
        lock.lock(3000);
        try {
            d = drRepository.update(d);
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/dr/update",
                    "自动流程", true).bindDrs(new Dr[]{d}).build();
            messageQueue.produceMessage("/api/dr/update", d.getId(), slbMessageData);
            setProperty("status", "toBeActivated", "dr", new Long[]{d.getId()});
        } finally {
            lock.unlock();
        }
        return d;

    }

    /**
     * Unbind Dr From Vses
     * Return updated dr in success.
     * Return null in case of no need to update.
     * Exception in case of Failed.
     * **/
    private Dr drUnbindVs(Dr d, List<Long> newVsIds) throws Exception {
        DistLock lock = dbLockFactory.newLock(d.getId() + "_updateDr");
        lock.lock(3000);
        try {
            boolean changed = false;
            for (DrTraffic drTraffic : d.getDrTraffics()) {
                List<Destination> removeDes = new ArrayList<>();
                for (Destination des : drTraffic.getDestinations()) {
                    if (newVsIds.contains(des.getVirtualServer().getId())) {
                        removeDes.add(des);
                    }
                }
                if (removeDes.size() > 0) {
                    changed = true;
                    drTraffic.getDestinations().removeAll(removeDes);
                }
            }
            if (!changed) return null;
            d = drRepository.update(d);
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/dr/update",
                    "自动流程", true).bindDrs(new Dr[]{d}).build();
            messageQueue.produceMessage("/api/dr/update", d.getId(), slbMessageData);
            setProperty("status", "toBeActivated", "dr", new Long[]{d.getId()});
        } finally {
            lock.unlock();
        }
        return d;

    }

    /**
     * Bind Policy To New Vses
     * Return updated policy in success.
     * Return null in case of no need to update.
     * Exception in case of Failed.
     * **/
    private TrafficPolicy policyBindVs(TrafficPolicy p, VirtualServer vs, List<Long> newVsIds) throws Exception {
        PolicyVirtualServer orgPvs = null;
        List<Long> pvsIds = new ArrayList<>();
        for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
            if (pvs.getVirtualServer().getId().equals(vs.getId())) {
                orgPvs = pvs;
            } else {
                pvsIds.add(pvs.getVirtualServer().getId());
            }
        }
        if (orgPvs == null) return null;

        boolean changed = false;

        for (Long id : newVsIds) {
            if (pvsIds.contains(id)) continue;
            PolicyVirtualServer tmpPvs = new PolicyVirtualServer();
            tmpPvs.setVirtualServer(new VirtualServer().setId(id));
            tmpPvs.setPath(orgPvs.getPath());
            tmpPvs.setPriority(orgPvs.getPriority());
            p.addPolicyVirtualServer(tmpPvs);
            changed = true;
        }

        if (!changed) return null;

        DistLock lock = dbLockFactory.newLock(p.getId() + "_updatePolicy");
        lock.lock(3000);
        try {
            p = trafficPolicyRepository.update(p, true);
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/policy/update",
                    "自动流程", true).bindPolicies(new TrafficPolicy[]{p}).build();
            messageQueue.produceMessage("/api/policy/update", p.getId(), slbMessageData);
            setProperty("status", "toBeActivated", "policy", new Long[]{p.getId()});
        } finally {
            lock.unlock();
        }
        return p;
    }

    /**
     * Unbind Policy From Vses
     * Return updated policy in success.
     * Return null in case of no need to update.
     * Exception in case of Failed.
     * **/
    private TrafficPolicy policyUnbindVs(TrafficPolicy p, List<Long> newVsIds) throws Exception {
        DistLock lock = dbLockFactory.newLock(p.getId() + "_updatePolicy");
        lock.lock(3000);
        try {
            List<PolicyVirtualServer> removePolicies = new ArrayList<>();
            for (PolicyVirtualServer pvs : p.getPolicyVirtualServers()) {
                if (newVsIds.contains(pvs.getVirtualServer().getId())) {
                    removePolicies.add(pvs);
                }
            }
            if (removePolicies.size() == 0) return null;
            p.getPolicyVirtualServers().removeAll(removePolicies);
            p = trafficPolicyRepository.update(p, true);
            setProperty("status", "toBeActivated", "policy", new Long[]{p.getId()});
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/policy/update",
                    "自动流程", true).bindPolicies(new TrafficPolicy[]{p}).build();
            messageQueue.produceMessage("/api/policy/update", p.getId(), slbMessageData);
        } finally {
            lock.unlock();
        }
        return p;
    }

    /**
     * Bind Group To New Vses
     * Return updated group in success.
     * Return null in case of no need to update.
     * Exception in case of Failed.
     * **/
    private Group groupBindVs(Group g, VirtualServer vs, List<Long> newVsIds) throws Exception {
        GroupVirtualServer orgGvs = null;
        List<Long> gvsIds = new ArrayList<>();
        for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
            if (gvs.getVirtualServer().getId().equals(vs.getId())) {
                orgGvs = gvs;
            } else {
                gvsIds.add(gvs.getVirtualServer().getId());
            }
        }
        if (orgGvs == null) return null;
        boolean changed = false;

        for (Long id : newVsIds) {
            if (gvsIds.contains(id)) continue;
            GroupVirtualServer tmpGvs = new GroupVirtualServer();
            tmpGvs.setVirtualServer(new VirtualServer().setId(id));
            tmpGvs.setPath(orgGvs.getPath());
            tmpGvs.setRedirect(orgGvs.getRedirect());
            tmpGvs.setCustomConf(orgGvs.getCustomConf());
            tmpGvs.setPriority(orgGvs.getPriority());
            tmpGvs.setRewrite(orgGvs.getRewrite());
            tmpGvs.getRouteRules().addAll(orgGvs.getRouteRules());
            g.addGroupVirtualServer(tmpGvs);
            changed = true;
        }

        if (!changed) return null;

        DistLock lock = dbLockFactory.newLock(g.getId() + "_updateGroup");
        lock.lock(3000);
        try {
            g = groupRepository.update(g, true);
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/group/update",
                    "自动流程", true).bindGroups(new Group[]{g}).build();
            messageQueue.produceMessage("/api/group/update", g.getId(), slbMessageData);
            setProperty("status", "toBeActivated", "group", new Long[]{g.getId()});
        } finally {
            lock.unlock();
        }
        return g;
    }


    /**
     * Unbind Group Vses
     * Return updated group in success.
     * Return null in case of no need to update.
     * Exception in case of Failed.
     * **/
    private Group groupUnbindVs(Group g, List<Long> newVsIds) throws Exception {
        DistLock lock = dbLockFactory.newLock(g.getId() + "_updateGroup");
        lock.lock(3000);
        try {
            List<GroupVirtualServer> removeGvses = new ArrayList<>();
            for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                if (newVsIds.contains(gvs.getVirtualServer().getId())) {
                    removeGvses.add(gvs);
                }
            }
            if (removeGvses.size() == 0) return null;
            g.getGroupVirtualServers().removeAll(removeGvses);
            g = groupRepository.update(g, true);
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/group/update",
                    "自动流程", true).bindGroups(new Group[]{g}).build();
            messageQueue.produceMessage("/api/group/update", g.getId(), slbMessageData);
            setProperty("status", "toBeActivated", "group", new Long[]{g.getId()});
        } finally {
            lock.unlock();
        }
        return g;
    }

    public void unbindCleanVses(List<Long> sourceVsId) throws Exception {
        Long[] vsIds = sourceVsId.toArray(new Long[sourceVsId.size()]);

        List<TrafficPolicy> policyList = new ArrayList<>();
        ModelStatusMapping<TrafficPolicy> policyMap = entityFactory.getPoliciesByVsIds(vsIds);
        for (TrafficPolicy p : policyMap.getOfflineMapping().values()) {
            p = policyUnbindVs(p, sourceVsId);
            if (p != null) {
                policyList.add(p);
            }
        }

        List<Dr> drList = new ArrayList<>();
        ModelStatusMapping<Dr> drMap = entityFactory.getDrsByVsIds(vsIds);
        for (Dr d : drMap.getOfflineMapping().values()) {
            d = drUnbindVs(d, sourceVsId);
            if (d != null) {
                drList.add(d);
            }
        }

        ModelStatusMapping<Group> groups = entityFactory.getGroupsByVsIds(vsIds);
        List<Group> groupList = new ArrayList<>();
        for (Group g : groups.getOfflineMapping().values()) {
            g = groupUnbindVs(g, sourceVsId);
            if (g != null) {
                groupList.add(g);
            }
        }

        activateEntity(groupList, policyList, drList, null);

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(vsIds);
        if (vsMap.getOnlineMapping() == null || vsMap.getOfflineMapping().size() == 0) {
            return;
        }

        List<OpsTask> deactivatingTask = new ArrayList<>();
        for (Long vsId : sourceVsId) {
            VirtualServer vs = vsMap.getOnlineMapping().get(vsId);
            if (vs == null) continue;
            for (Long slbId : vs.getSlbIds()) {
                OpsTask task = new OpsTask();
                task.setSlbVirtualServerId(vsId);
                task.setCreateTime(new Date());
                task.setOpsType(TaskOpsType.DEACTIVATE_VS);
                task.setTargetSlbId(slbId);
                deactivatingTask.add(task);
            }
        }
        List<Long> taskIds = taskManager.addTask(deactivatingTask);

        taskManager.getResult(taskIds, 30000L);
        for (Long vsId : vsMap.getOfflineMapping().keySet()) {
            virtualServerRepository.delete(vsId);
        }
    }
}
