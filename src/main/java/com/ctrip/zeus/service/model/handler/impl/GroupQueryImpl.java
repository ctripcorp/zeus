package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("groupQuery")
public class GroupQueryImpl implements GroupQuery {
    @Resource
    private GroupDao groupDao;
    @Resource
    private GroupHealthCheckDao groupHealthCheckDao;
    @Resource
    private GroupLoadBalancingMethodDao groupLoadBalancingMethodDao;

    @Override
    public Group getById(Long id) throws Exception {
        List<GroupDo> l = groupDao.findAllByIds(new Long[]{id}, GroupEntity.READSET_FULL);
        if (l.size() == 0)
            return null;
        return createGroup(l.get(0));
    }

    private Group createGroup(GroupDo d) throws Exception {
        if (d == null)
            return null;
        Group group = C.toGroup(d);
        cascadeQuery(group);
        return group;
    }

    private void cascadeQuery(Group group) throws Exception {
        queryGroupHealthCheck(group);
        queryLoadBalancingMethod(group);
    }

    private void queryGroupHealthCheck(Group group) throws Exception {
        GroupHealthCheckDo d = groupHealthCheckDao.findByGroup(group.getId(), GroupHealthCheckEntity.READSET_FULL);
        if (d == null)
            return;
        HealthCheck e = C.toHealthCheck(d);
        group.setHealthCheck(e);
    }

    private void queryLoadBalancingMethod(Group group) throws Exception {
        GroupLoadBalancingMethodDo d = groupLoadBalancingMethodDao.findByGroup(group.getId(), GroupLoadBalancingMethodEntity.READSET_FULL);
        if (d == null)
            return;
        LoadBalancingMethod e = C.toLoadBalancingMethod(d);
        group.setLoadBalancingMethod(e);
    }
}