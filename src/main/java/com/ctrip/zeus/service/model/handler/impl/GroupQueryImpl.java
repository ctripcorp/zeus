package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.GroupMemberRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("groupQuery")
public class GroupQueryImpl implements GroupQuery {
    @Resource
    private GroupSlbDao groupSlbDao;
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


    @Override
    public Group get(String name) throws Exception {
        GroupDo d = groupDao.findByName(name, GroupEntity.READSET_FULL);
        return createGroup(d);
    }

    @Override
    public Group getById(Long id) throws Exception {
        List<GroupDo> l = groupDao.findAllByIds(new Long[]{id}, GroupEntity.READSET_FULL);
        if (l.size() == 0)
            return null;
        return createGroup(l.get(0));
    }

    @Override
    public Group getByAppId(String groupId) throws Exception {
        GroupDo d = groupDao.findByAppId(groupId, GroupEntity.READSET_FULL);
        return createGroup(d);
    }

    @Override
    public List<Group> batchGet(Long[] ids) throws Exception {
        List<GroupDo> l = groupDao.findAllByIds(ids, GroupEntity.READSET_FULL);
        List<Group> result = new ArrayList<>();
        for (GroupDo groupDo : l) {
            Group g = createGroup(groupDo);
            if (g != null)
                result.add(g);
        }
        return result;
    }

    @Override
    public List<Group> getAll() throws Exception {
        List<Group> list = new ArrayList<>();
        for (GroupDo d : groupDao.findAll(GroupEntity.READSET_FULL)) {
            Group group = createGroup(d);
            if (group != null)
                list.add(group);
        }
        return list;
    }

    @Override
    public List<Group> getBySlb(Long slbId) throws Exception {
        Set<Long> visitedIds = new HashSet<>();
        for (GroupSlbDo groupSlbDo : groupSlbDao.findAllBySlb(slbId, GroupSlbEntity.READSET_FULL)) {
            if (visitedIds.contains(groupSlbDo.getSlbId()))
                continue;
            visitedIds.add(groupSlbDo.getSlbId());
        }
        if (visitedIds.size() == 0)
            return new ArrayList<>();
        return batchGet(visitedIds.toArray(new Long[visitedIds.size()]));
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
        for (GroupVirtualServer groupVirtualServer : virtualServerRepository.listGroupVsByGroups(new Long[]{group.getId()})) {
            group.addGroupVirtualServer(groupVirtualServer);
        }
        for (GroupServer server : groupMemberRepository.listGroupServersByGroup(group.getId())) {
            group.addGroupServer(server);
        }
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
