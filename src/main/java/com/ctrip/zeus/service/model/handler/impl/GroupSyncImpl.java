package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.GroupMemberRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.support.C;
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
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupMemberRepository groupMemberRepository;
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
        cascadeSync(group);
        d.setVersion(d.getVersion() + 1);
        return d;
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupModelValidator.removable(groupId);
        groupHealthCheckDao.deleteByGroup(new GroupHealthCheckDo().setGroupId(groupId));
        groupLoadBalancingMethodDao.deleteByGroup(new GroupLoadBalancingMethodDo().setGroupId(groupId));
        virtualServerRepository.batchDeleteGroupVirtualServers(groupId);
        groupMemberRepository.removeGroupServer(groupId, null);
        return groupDao.deleteById(new GroupDo().setId(groupId));
    }

    private void cascadeSync(Group group) throws Exception {
        syncGroupHealthCheck(group.getId(), group.getHealthCheck());
        syncLoadBalancingMethod(group.getId(), group.getLoadBalancingMethod());
        syncGroupServers(group.getId(), group.getGroupServers());
        virtualServerRepository.updateGroupVirtualServers(group.getId(), group.getGroupVirtualServers());
    }

    private void syncGroupHealthCheck(Long groupKey, HealthCheck healthCheck) throws DalException {
        if (healthCheck == null) {
            logger.info("No health check method is found when adding/updating group with id " + groupKey);
            groupHealthCheckDao.deleteByGroup(new GroupHealthCheckDo().setGroupId(groupKey));
        } else {
            groupHealthCheckDao.insert(C.toGroupHealthCheckDo(healthCheck)
                    .setGroupId(groupKey));
        }
    }

    private void syncLoadBalancingMethod(Long groupKey, LoadBalancingMethod loadBalancingMethod) throws DalException {
        if (loadBalancingMethod == null)
            return;
        groupLoadBalancingMethodDao.insert(C.toGroupLoadBalancingMethodDo(loadBalancingMethod)
                .setGroupId(groupKey)
                .setCreatedTime(new Date()));
    }

    private void syncGroupServers(Long groupId, List<GroupServer> groupServers) throws Exception {
        Set<String> originIps = new HashSet<>(groupMemberRepository.listGroupServerIpsByGroup(groupId));
        Set<String> inputIps = new HashSet<>();
        for (GroupServer groupServer : groupServers) {
            inputIps.add(groupServer.getIp());
            if (originIps.contains(groupServer.getIp()))
                groupMemberRepository.updateGroupServer(groupId, groupServer);
            else
                groupMemberRepository.addGroupServer(groupId, groupServer);
        }
        originIps.removeAll(inputIps);
        for (String originIp : originIps) {
            groupMemberRepository.removeGroupServer(groupId, originIp);
        }
    }
}
