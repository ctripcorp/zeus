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
    public Group getById(Long id) throws DalException {
        List<GroupDo> l = groupDao.findAllByIds(new Long[]{id}, GroupEntity.READSET_FULL);
        if (l.size() == 0)
            return null;
        return createGroup(l.get(0));
    }

    @Override
    public Group getByAppId(String groupId) throws DalException {
        GroupDo d = groupDao.findByAppId(groupId, GroupEntity.READSET_FULL);
        return createGroup(d);
    }

    @Override
    public Long[] batchGetByNames(String[] names) throws DalException {
        List<GroupDo> l = groupDao.findAllByNames(names, GroupEntity.READSET_FULL);
        if (l.size() == 0) {
            return null;
        }
        Long[] ids = new Long[l.size()];
        for (int i = 0; i < l.size(); i++) {
            ids[i] = l.get(i).getId();
        }
        return ids;
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
    public List<Group> getLimit(Long fromId, int maxCount) throws DalException {
        List<Group> list = new ArrayList<>();
        for (GroupDo d : groupDao.findLimit(fromId, maxCount, GroupEntity.READSET_FULL)) {
            Group group = createGroup(d);
            if (group != null)
                list.add(group);
        }
        return list;
    }

    @Override
    public List<Group> getByVirtualServer(Long virtualServerId) throws DalException {
        List<GroupSlbDo> l = groupSlbDao.findAllByVirtualServer(virtualServerId, GroupSlbEntity.READSET_FULL);
        Long[] ids = new Long[l.size()];
        for (int i = 0; i < l.size(); i++) {
            ids[i] = l.get(i).getId();
        }
        List<Group> list = new ArrayList<>();
        for (GroupDo d : groupDao.findAllByIds(ids, GroupEntity.READSET_FULL)) {
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
    public List<String> getGroupServerIpsByGroup(Long groupId) throws DalException {
        List<String> groupServers = new ArrayList<>();
        for (GroupServerDo asd : groupServerDao.findAllByGroup(groupId, GroupServerEntity.READSET_FULL)) {
            groupServers.add(asd.getIp());
        }
        return groupServers;
    }

    @Override
    public List<GroupServer> getGroupServersByGroup(Long groupId) throws DalException {
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
        cascadeQuery(group);
        return group;
    }

    private void cascadeQuery(Group group) throws DalException {
        queryGroupSlbs(group);
        queryGroupHealthCheck(group);
        queryLoadBalancingMethod(group);
        queryGroupServers(group);
    }

    private void queryGroupSlbs(Group group) throws DalException {
        List<GroupSlbDo> list = groupSlbDao.findAllByGroup(group.getId(), GroupSlbEntity.READSET_FULL);
        for (GroupSlbDo d : list) {
            GroupSlb e = C.toGroupSlb(d);
            group.addGroupSlb(e);
            e.setSlbName(slbDao.findById(e.getSlbId(), SlbEntity.READSET_FULL).getName());
            querySlbVips(e);
            queryVirtualServer(d.getSlbVirtualServerId(), e);
        }
    }

    private void querySlbVips(GroupSlb groupSlb) throws DalException {
        List<SlbVipDo> list = slbVipDao.findAllBySlb(groupSlb.getSlbId(), SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            groupSlb.addVip(e);
        }
    }

    private void queryVirtualServer(Long slbVirtualServerId, GroupSlb groupSlb) throws DalException {
        SlbVirtualServerDo d = slbVirtualServerDao.findByPK(slbVirtualServerId, SlbVirtualServerEntity.READSET_FULL);
        if (d == null)
            return;
        VirtualServer e = C.toVirtualServer(d);
        groupSlb.setVirtualServer(e);
        querySlbDomains(d.getId(), e);
    }

    private void querySlbDomains(Long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            Domain e = C.toDomain(d);
            virtualServer.addDomain(e);
        }
    }

    private void queryGroupHealthCheck(Group group) throws DalException {
        GroupHealthCheckDo d = groupHealthCheckDao.findByGroup(group.getId(), GroupHealthCheckEntity.READSET_FULL);
        if (d == null)
            return;
        HealthCheck e = C.toHealthCheck(d);
        group.setHealthCheck(e);
    }

    private void queryLoadBalancingMethod(Group group) throws DalException {
        GroupLoadBalancingMethodDo d = groupLoadBalancingMethodDao.findByGroup(group.getId(), GroupLoadBalancingMethodEntity.READSET_FULL);
        if (d == null)
            return;
        LoadBalancingMethod e = C.toLoadBalancingMethod(d);
        group.setLoadBalancingMethod(e);
    }

    private void queryGroupServers(Group group) throws DalException {
        List<GroupServerDo> list = groupServerDao.findAllByGroup(group.getId(), GroupServerEntity.READSET_FULL);
        for (GroupServerDo d : list) {
            GroupServer e = C.toGroupServer(d);
            group.addGroupServer(e);
        }
    }
}
