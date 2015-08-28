package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
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

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Long add(Group group) throws Exception {
        GroupDo d = C.toGroupDo(0L, group);
        d.setCreatedTime(new Date()).setVersion(1);
        groupDao.insert(d);
        group.setId(d.getId());
        cascadeSync(group);
        return d.getId();
    }

    @Override
    public Long update(Group group) throws Exception {
        GroupDo check = groupDao.findById(group.getId(), GroupEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Group with id " + group.getId() + "does not exist.");
        if (check.getVersion() > group.getVersion())
            throw new ValidationException("Newer Group version is detected.");
        GroupDo d = C.toGroupDo(group.getId(), group);
        groupDao.updateById(d, GroupEntity.UPDATESET_FULL);
        group.setId(d.getId());
        cascadeSync(group);
        return d.getId();
    }

    @Override
    public void updateVersion(Long[] groupIds) throws Exception {
        List<GroupDo> groupDos = groupDao.findAllByIds(groupIds, GroupEntity.READSET_FULL);
        groupDao.updateById(groupDos.toArray(new GroupDo[groupDos.size()]), GroupEntity.UPDATESET_FULL);
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupHealthCheckDao.deleteByGroup(new GroupHealthCheckDo().setGroupId(groupId));
        groupLoadBalancingMethodDao.deleteByGroup(new GroupLoadBalancingMethodDo().setGroupId(groupId));
        return groupDao.deleteById(new GroupDo().setId(groupId));
    }

    private void cascadeSync(Group group) throws Exception {
        syncGroupHealthCheck(group.getId(), group.getHealthCheck());
        syncLoadBalancingMethod(group.getId(), group.getLoadBalancingMethod());
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
}
