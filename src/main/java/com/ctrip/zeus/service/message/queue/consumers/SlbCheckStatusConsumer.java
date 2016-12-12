package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.queue.entity.*;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.task.check.SlbCheckStatusRollingMachine;
import com.ctrip.zeus.util.MessageUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
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
    private ConfigHandler configHandler;
    @Resource
    private ArchiveRepository archiveRepository;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupRepository groupRepository;

    private static Logger logger = LoggerFactory.getLogger(SlbCheckStatusConsumer.class);

    @PostConstruct
    public void init() {
        try {
            if (configHandler.getEnable("message.queue", null, null, null, true)) {
                slbCheckStatusRollingMachine.enable(true);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        for (Message m : messages) {
            if (m.getTargetId() != null && m.getTargetId() > 0L) {
                Long groupId = m.getTargetId();
                try {
                    Group g = groupRepository.getById(groupId);
                    Set<Long> slbIds = new HashSet<>();
                    for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                        slbIds.addAll(gvs.getVirtualServer().getSlbIds());
                    }
                    GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(groupId);
                    slbCheckStatusRollingMachine.update(slbIds, groupStatus);
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
                GroupData g = d.getGroupDatas().get(0);
                try {
                    Group curr = archiveRepository.getGroupArchive(g.getId(), g.getVersion());
                    Group prev = archiveRepository.getGroupArchive(g.getId(), g.getVersion() - 1);

                    Long[] currVsIds = new Long[curr.getGroupVirtualServers().size()];
                    Long[] prevVsIds = new Long[prev.getGroupVirtualServers().size()];
                    for (int i = 0; i < curr.getGroupVirtualServers().size(); i++) {
                        currVsIds[i] = curr.getGroupVirtualServers().get(i).getVirtualServer().getId();
                    }
                    for (int i = 0; i < prev.getGroupVirtualServers().size(); i++) {
                        prevVsIds[i] = prev.getGroupVirtualServers().get(i).getVirtualServer().getId();
                    }

                    Arrays.sort(currVsIds);
                    Arrays.sort(prevVsIds);
                    if (Arrays.equals(currVsIds, prevVsIds)) return;

                    Set<Long> currSlbIds = new HashSet<>();
                    Set<Long> prevSlbIds = new HashSet<>();
                    for (VirtualServer vs : virtualServerRepository.listAll(currVsIds)) {
                        currSlbIds.addAll(vs.getSlbIds());
                    }
                    for (VirtualServer vs : virtualServerRepository.listAll(prevVsIds)) {
                        prevSlbIds.addAll(vs.getSlbIds());
                    }
                    if (currSlbIds.equals(prevSlbIds)) return;

                    List<GroupStatus> list = new ArrayList<>();
                    list.add(groupStatusService.getOfflineGroupStatus(g.getId()));
                    slbCheckStatusRollingMachine.migrate(prevSlbIds, currSlbIds, Sets.newHashSet(m.getTargetId()), list);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onDeleteGroup(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData d = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (d != null && d.getSuccess()) {
                try {
                    Set<Long> slbIds = new HashSet<>();
                    Group g = archiveRepository.getGroupArchive(m.getTargetId(), 0);
                    for (GroupVirtualServer gvs : g.getGroupVirtualServers()) {
                        slbIds.addAll(gvs.getVirtualServer().getSlbIds());
                    }
                    slbCheckStatusRollingMachine.clear(slbIds, m.getTargetId());
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onUpdateVs(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData d = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (d != null && d.getSuccess()) {
                VsData vs = d.getVsDatas().get(0);
                try {
                    VirtualServer curr = archiveRepository.getVsArchive(vs.getId(), vs.getVersion());
                    VirtualServer prev = archiveRepository.getVsArchive(vs.getId(), vs.getVersion() - 1);

                    Set<Long> currSlbIdArray = new HashSet<>(curr.getSlbIds().size());
                    Set<Long> prevSlbIdArray = new HashSet<>(prev.getSlbIds().size());

                    if (currSlbIdArray.equals(prevSlbIdArray)) return;

                    Set<Long> groupIds = new HashSet<>();
                    for (IdVersion k : groupCriteriaQuery.queryByVsId(vs.getId())) {
                        groupIds.add(k.getId());
                    }
                    List<GroupStatus> groupStatuses = groupStatusService.getOfflineGroupsStatus(groupIds);
                    slbCheckStatusRollingMachine.migrate(prevSlbIdArray, currSlbIdArray, groupIds, groupStatuses);
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
                slbCheckStatusRollingMachine.update(groups.get(gs.getGroupId()), gs);
            }
        } catch (Exception e) {
            logger.error("Fail to get offline groups status of groups " + Joiner.on(",").join(groupIds) + ".");
        }
    }
}