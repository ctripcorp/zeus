package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    @Resource
    private GroupServerDao groupServerDao;
    @Resource
    private GroupSlbDao groupSlbDao;
    @Resource
    private SlbDao slbDao;
    @Resource
    private SlbDomainDao slbDomainDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private SlbVipDao slbVipDao;


    @Override
    public Group get(String name) throws DalException {
        GroupDo d = groupDao.findByName(name, GroupEntity.READSET_FULL);
        return createGroup(d);
    }

    @Override
    public Group getById(long id) throws DalException {
        List<GroupDo> list = groupDao.findAllByIds(new long[]{id}, GroupEntity.READSET_FULL);
        if (list.size() == 0)
            return null;
        return createGroup(list.get(0));
    }

    @Override
    public Group getByAppId(String groupId) throws DalException {
        GroupDo d = groupDao.findByAppId(groupId, GroupEntity.READSET_FULL);
        return createGroup(d);
    }

    @Override
    public List<Group> getAll() throws DalException {
        List<Group> list = new ArrayList<>();
        for (GroupDo d : groupDao.findAll(GroupEntity.READSET_FULL)) {
            Group group = createGroup(d);
            if (group != null)
                list.add(group);
        }
        return list;
    }

    @Override
    public List<Group> getLimit(long fromId, int maxCount) throws DalException {
        List<Group> list = new ArrayList<>();
        for (GroupDo d : groupDao.findLimit(fromId, maxCount, GroupEntity.READSET_FULL)) {
            Group group = createGroup(d);
            if (group != null)
                list.add(group);
        }
        return list;
    }

    @Override
    public List<Group> getBySlbAndVirtualServer(String slbName, String virtualServerName) throws DalException {
        long slbId = slbDao.findByName(slbName, SlbEntity.READSET_FULL).getId();
        long vsId = slbVirtualServerDao.findBySlbAndName(slbId, virtualServerName, SlbVirtualServerEntity.READSET_FULL).getId();
        List<GroupSlbDo> l = groupSlbDao.findAllBySlbAndVirtualServer(slbId, vsId, GroupSlbEntity.READSET_FULL);
        int size = l.size();
        long[] names = new long[size];
        for (int i = 0; i < size; i++) {
            names[i] = l.get(i).getId();
        }
        List<Group> list = new ArrayList<>();
        for (GroupDo d : groupDao.findAllByIds(names, GroupEntity.READSET_FULL)) {
            list.add(createGroup(d));
        }
        return list;
    }

    @Override
    public List<String> getByGroupServer(String groupServerIp) throws DalException {
        List<String> groupNames = new ArrayList<>();
        for (GroupServerDo asd : groupServerDao.findAllByIp(groupServerIp, GroupServerEntity.READSET_FULL)) {
            GroupDo d = groupDao.findByPK(asd.getGroupId(), GroupEntity.READSET_FULL);
            groupNames.add(d.getName());
        }
        return groupNames;
    }

    @Override
    public List<String> getGroupServerIpsByGroup(long groupId) throws DalException {
        List<String> groupServers = new ArrayList<>();
        for (GroupServerDo asd : groupServerDao.findAllByGroup(groupId, GroupServerEntity.READSET_FULL)) {
            groupServers.add(asd.getIp());
        }
        return groupServers;
    }

    @Override
    public List<GroupServer> getGroupServersByGroup(long groupId) throws DalException {
        List<GroupServer> groupServers = new ArrayList<>();
        for (GroupServerDo asd : groupServerDao.findAllByGroup(groupId, GroupServerEntity.READSET_FULL)) {
            groupServers.add(C.toGroupServer(asd));
        }
        return groupServers;
    }

    private Group createGroup(GroupDo d) throws DalException {
        if (d == null)
            return null;
        Group group = C.toGroup(d);
        cascadeQuery(d, group);
        return group;
    }

    private void cascadeQuery(GroupDo d, Group group) throws DalException {
        queryGroupSlbs(d.getName(), group);
        queryGroupHealthCheck(d.getId(), group);
        queryLoadBalancingMethod(d.getId(), group);
        queryGroupServers(d.getId(), group);
    }

    private void queryGroupSlbs(String groupName, Group group) throws DalException {
        long groupId = groupDao.findByName(groupName, GroupEntity.READSET_FULL).getId();
        List<GroupSlbDo> list = groupSlbDao.findAllByGroup(groupId, GroupSlbEntity.READSET_FULL);
        for (GroupSlbDo d : list) {
            GroupSlb e = C.toGroupSlb(d);
            group.addGroupSlb(e);
            e.setSlbName(slbDao.findById(e.getSlbId(), SlbEntity.READSET_FULL).getName());
            querySlbVips(d.getSlbId(), e);
            queryVirtualServer(d.getSlbVirtualServerId(), e);
        }
    }

    private void querySlbVips(long slbId, GroupSlb groupSlb) throws DalException {
        List<SlbVipDo> list = slbVipDao.findAllBySlb(slbId, SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            groupSlb.addVip(e);
        }
    }

    private void queryVirtualServer(long slbVirtualServerId, GroupSlb groupSlb) throws DalException {
        SlbVirtualServerDo d = slbVirtualServerDao.findByPK(slbVirtualServerId, SlbVirtualServerEntity.READSET_FULL);
        if (d == null)
            return;
        VirtualServer e = C.toVirtualServer(d);
        groupSlb.setVirtualServer(e);
        querySlbDomains(d.getId(), e);
    }

    private void querySlbDomains(long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            Domain e = C.toDomain(d);
            virtualServer.addDomain(e);
        }
    }

    private void queryGroupHealthCheck(long groupKey, Group group) throws DalException {
        GroupHealthCheckDo d = groupHealthCheckDao.findByGroup(groupKey, GroupHealthCheckEntity.READSET_FULL);
        if (d == null)
            return;
        HealthCheck e = C.toHealthCheck(d);
        group.setHealthCheck(e);
    }

    private void queryLoadBalancingMethod(long groupKey, Group group) throws DalException {
        GroupLoadBalancingMethodDo d = groupLoadBalancingMethodDao.findByGroup(groupKey, GroupLoadBalancingMethodEntity.READSET_FULL);
        if (d == null)
            return;
        LoadBalancingMethod e = C.toLoadBalancingMethod(d);
        group.setLoadBalancingMethod(e);
    }

    private void queryGroupServers(long groupKey, Group group) throws DalException {
        List<GroupServerDo> list = groupServerDao.findAllByGroup(groupKey, GroupServerEntity.READSET_FULL);
        for (GroupServerDo d : list) {
            GroupServer e = C.toGroupServer(d);
            group.addGroupServer(e);
        }
    }
}
