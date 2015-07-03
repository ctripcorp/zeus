package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.support.C;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("groupSync")
public class GroupSyncImpl implements GroupSync {
    @Resource
    private GroupDao groupDao;
    @Resource
    private GroupHealthCheckDao groupHealthCheckDao;
    @Resource
    private GroupLoadBalancingMethodDao groupLoadBalancingMethodDao;
    @Resource
    private GroupServerDao groupServerDao;
    @Resource
    private GroupSlbDao groupSlbDao;
    @Resource
    private SlbRepository slbRepository;

    @Resource
    private GroupValidator groupModelValidator;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public GroupDo add(Group group) throws Exception {
        groupModelValidator.validate(group);
        GroupDo d = C.toGroupDo(0L, group);
        d.setCreatedTime(new Date());
        d.setVersion(1);

        groupDao.insert(d);
        group.setId(d.getId());
        cascadeSync(group);

        return d;
    }

    @Override
    public GroupDo update(Group group) throws Exception {
        groupModelValidator.validate(group);
        GroupDo check = groupDao.findById(group.getId(), GroupEntity.READSET_FULL);
        if (check.getVersion() > group.getVersion())
            throw new ValidationException("Newer Group version is detected.");

        GroupDo d = C.toGroupDo(group.getId(), group);
        groupDao.updateById(d, GroupEntity.UPDATESET_FULL);

        GroupDo updated = groupDao.findById(group.getId(), GroupEntity.READSET_FULL);
        d.setVersion(updated.getVersion());
        cascadeSync(group);
        return d;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
        groupSlbDao.deleteByGroup(new GroupSlbDo().setGroupId(groupId));
        groupServerDao.deleteByGroup(new GroupServerDo().setGroupId(groupId));
        groupHealthCheckDao.deleteByGroup(new GroupHealthCheckDo().setGroupId(groupId));
        groupLoadBalancingMethodDao.deleteByGroup(new GroupLoadBalancingMethodDo().setGroupId(groupId));
        return groupDao.deleteById(new GroupDo().setId(groupId));
    }

    private void cascadeSync(Group group) throws Exception {
        syncGroupSlbs(group.getId(), group.getGroupSlbs());
        syncGroupHealthCheck(group.getId(), group.getHealthCheck());
        syncLoadBalancingMethod(group.getId(), group.getLoadBalancingMethod());
        syncGroupServers(group.getId(), group.getGroupServers());
    }

    private void syncGroupSlbs(Long groupId, List<GroupSlb> groupSlbs) throws Exception {
        List<GroupSlbDo> oldList = groupSlbDao.findAllByGroup(groupId, GroupSlbEntity.READSET_FULL);
        Map<String, GroupSlbDo> oldMap = Maps.uniqueIndex(oldList, new Function<GroupSlbDo, String>() {
            @Override
            public String apply(GroupSlbDo input) {
                return input.getGroupId() + "" + input.getSlbVirtualServerId();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (GroupSlb e : groupSlbs) {
            Long vsId = slbRepository.getVirtualServer(e.getVirtualServer().getId(),
                    e.getSlbId(), e.getVirtualServer().getName()).getId();
            GroupSlbDo old = oldMap.get(groupId + "" + vsId);
            if (old != null) {
                oldList.remove(old);
            }
            e.setSlbId(slbRepository.getByVirtualServer(vsId).getId());
            e.setGroupId(groupId);
            e.getVirtualServer().setId(vsId);
            groupSlbDao.insert(C.toGroupSlbDo(e)
                    .setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (GroupSlbDo d : oldList) {
            groupSlbDao.deleteByPK(new GroupSlbDo().setId(d.getId()));
        }
    }

    private void syncGroupHealthCheck(Long groupKey, HealthCheck healthCheck) throws DalException {
        if (healthCheck == null) {
            logger.info("No health check method is found when adding/updating group with id " + groupKey);
            groupHealthCheckDao.deleteByGroup(new GroupHealthCheckDo().setGroupId(groupKey));
            return;
        }
        groupHealthCheckDao.insert(C.toGroupHealthCheckDo(healthCheck)
                .setGroupId(groupKey)
                .setCreatedTime(new Date()));
    }

    private void syncLoadBalancingMethod(Long groupKey, LoadBalancingMethod loadBalancingMethod) throws DalException {
        if (loadBalancingMethod == null)
            return;
        groupLoadBalancingMethodDao.insert(C.toGroupLoadBalancingMethodDo(loadBalancingMethod)
                .setGroupId(groupKey)
                .setCreatedTime(new Date()));
    }

    private void syncGroupServers(Long groupKey, List<GroupServer> groupServers) throws DalException {
        if (groupServers == null || groupServers.size() == 0) {
            logger.warn("No group server is given when adding/update group with id " + groupKey);
            return;
        }
        List<GroupServerDo> oldList = groupServerDao.findAllByGroup(groupKey, GroupServerEntity.READSET_FULL);
        Map<String, GroupServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<GroupServerDo, String>() {
            @Override
            public String apply(GroupServerDo input) {
                return input.getGroupId() + input.getIp();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (GroupServer e : groupServers) {
            GroupServerDo old = oldMap.get(groupKey + e.getIp());
            if (old != null) {
                oldList.remove(old);
            }
            groupServerDao.insert(C.toGroupServerDo(e)
                    .setGroupId(groupKey)
                    .setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (GroupServerDo d : oldList) {
            groupServerDao.deleteByPK(new GroupServerDo().setId(d.getId()));
        }
    }
}
