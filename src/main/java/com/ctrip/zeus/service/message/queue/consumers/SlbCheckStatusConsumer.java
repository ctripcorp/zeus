package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.queue.entity.*;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.task.check.SlbCheckStatusRollingMachine;
import com.ctrip.zeus.util.MessageUtil;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2016/12/5.
 */
@Service("slbCheckStatusConsumer")
public class SlbCheckStatusConsumer extends AbstractConsumer {

    @Resource
    private SlbCheckStatusRollingMachine slbCheckStatusRollingMachine;
    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;

    private static Logger logger = LoggerFactory.getLogger(SlbCheckStatusConsumer.class);

    @PostConstruct
    private void init() {
        slbCheckStatusRollingMachine.enable(true, this);
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        for (Message m : messages) {
            if (m.getTargetId() != null && m.getTargetId() > 0L) {
                Long groupId = m.getTargetId();
                try {
                    migrateGroup(groupId, groupStatusService.getOfflineGroupStatus(groupId));
                } catch (Exception e) {
                    logger.error("Fail to get offline group status of group " + groupId + ".");
                }
            }
        }
    }

    @Override
    public void onUpdateGroup(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData d = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (d != null && d.getSuccess()) {
                Long groupId = m.getTargetId();
                try {
                    migrateGroup(groupId, groupStatusService.getOfflineGroupStatus(groupId));
                } catch (Exception e) {
                }
            }
        }
    }

    private void migrateGroup(Long groupId, GroupStatus groupStatus) throws Exception {
        Set<Long> slbIds = new HashSet<>();
        for (GroupVirtualServer gvs : groupRepository.getById(groupId).getGroupVirtualServers()) {
            slbIds.addAll(gvs.getVirtualServer().getSlbIds());
        }
        slbCheckStatusRollingMachine.migrate(slbIds, groupStatus);
    }

    @Override
    public void onDeleteGroup(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData d = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (d != null && d.getSuccess()) {
                slbCheckStatusRollingMachine.clear(m.getTargetId());
            }
        }
    }

    @Override
    public void onUpdateVs(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData d = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (d != null && d.getSuccess()) {
                Set<Long> groupIds = new HashSet<>();
                try {
                    for (IdVersion e : groupCriteriaQuery.queryByVsId(m.getTargetId())) {
                        groupIds.add(e.getId());
                    }
                    Map<Long, GroupStatus> groupStatusByGroup = new HashMap<>();
                    for (GroupStatus groupStatus : groupStatusService.getOfflineGroupsStatus(groupIds)) {
                        groupStatusByGroup.put(groupStatus.getGroupId(), groupStatus);
                    }
                    for (Group group : groupRepository.list(groupIds.toArray(new Long[groupIds.size()]))) {
                        migrateGroup(group.getId(), groupStatusByGroup.get(group.getId()));
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onOpsHealthy(List<Message> messages) {
        Set<Long> groupIds = new HashSet<>();
        for (Message m : messages) {
            SlbMessageData d = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (d != null && d.getSuccess()) {
                groupIds.add(m.getTargetId());
            }
        }
        Map<Long, Set<Long>> groups = new HashMap<>();
        try {
            for (Group g : groupRepository.list(groupIds.toArray(new Long[groupIds.size()]))) {
                Set<Long> slbIds = new HashSet<>();
                for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                    slbIds.addAll(gvs.getVirtualServer().getSlbIds());
                }
                groups.put(g.getId(), slbIds);
            }
            for (GroupStatus gs : groupStatusService.getOfflineGroupsStatus(groupIds)) {
                slbCheckStatusRollingMachine.update(gs);
            }
        } catch (Exception e) {
            logger.error("Fail to get offline group status of groups " + Joiner.on(",").join(groupIds) + ".");
        }
    }

    public void consistentCheck(Set<Long> slbIds) {
        Set<Long> totalSlbIds = new HashSet<>();
        try {
            for (IdVersion key : slbCriteriaQuery.queryAll(SelectionMode.OFFLINE_FIRST)) {
                totalSlbIds.add(key.getId());
            }
            totalSlbIds.removeAll(slbIds);
        } catch (Exception e) {
        }
        refresh(totalSlbIds);
    }

    public void refresh(Set<Long> slbIds) {
        for (Long slbId : slbIds) {
            try {
                slbCheckStatusRollingMachine.refresh(slbId, groupStatusService.getOfflineGroupsStatusBySlbId(slbId));
            } catch (Exception e) {
                logger.error("Fail to get offline groups status by slb " + slbId + ".");
            }
        }
    }
}