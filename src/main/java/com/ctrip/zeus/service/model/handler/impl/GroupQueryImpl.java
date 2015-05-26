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
    private GroupDao appDao;
    @Resource
    private GroupHealthCheckDao appHealthCheckDao;
    @Resource
    private GroupLoadBalancingMethodDao appLoadBalancingMethodDao;
    @Resource
    private GroupServerDao appServerDao;
    @Resource
    private GroupSlbDao appSlbDao;
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
        GroupDo d = appDao.findByName(name, GroupEntity.READSET_FULL);
        return createGroup(d);
    }

    @Override
    public Group getById(long id) throws DalException {
        List<GroupDo> list = appDao.findAllByIds(new long[]{id}, GroupEntity.READSET_FULL);
        if (list.size() == 0)
            return null;
        return createGroup(list.get(0));
    }

    @Override
    public Group getByAppId(String appId) throws DalException {
        GroupDo d = appDao.findByAppId(appId, GroupEntity.READSET_FULL);
        return createGroup(d);
    }

    @Override
    public List<Group> getAll() throws DalException {
        List<Group> list = new ArrayList<>();
        for (GroupDo d : appDao.findAll(GroupEntity.READSET_FULL)) {
            Group app = createGroup(d);
            if (app != null)
                list.add(app);
        }
        return list;
    }

    @Override
    public List<Group> getLimit(long fromId, int maxCount) throws DalException {
        List<Group> list = new ArrayList<>();
        for (GroupDo d : appDao.findLimit(fromId, maxCount, GroupEntity.READSET_FULL)) {
            Group app = createGroup(d);
            if (app != null)
                list.add(app);
        }
        return list;
    }

    @Override
    public List<Group> getBySlbAndVirtualServer(String slbName, String virtualServerName) throws DalException {
        List<GroupSlbDo> l = appSlbDao.findAllBySlbAndVirtualServer(slbName, virtualServerName, GroupSlbEntity.READSET_FULL);
        int size = l.size();
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = l.get(i).getGroupName();
        }

        List<Group> list = new ArrayList<>();
        for (GroupDo d : appDao.findAllByNames(names, GroupEntity.READSET_FULL)) {
            list.add(createGroup(d));
        }
        return list;
    }

    @Override
    public List<String> getByGroupServer(String appServerIp) throws DalException {
        List<String> appNames = new ArrayList<>();
        for (GroupServerDo asd : appServerDao.findAllByIp(appServerIp, GroupServerEntity.READSET_FULL)) {
            GroupDo d = appDao.findByPK(asd.getGroupId(), GroupEntity.READSET_FULL);
            appNames.add(d.getName());
        }
        return appNames;
    }

    @Override
    public List<String> getGroupServersByGroup(String appName) throws DalException {
        GroupDo d = appDao.findByName(appName, GroupEntity.READSET_FULL);
        if (d == null)
            return null;
        List<String> appServers = new ArrayList<>();
        for (GroupServerDo asd : appServerDao.findAllByGroup(d.getId(), GroupServerEntity.READSET_FULL)) {
            appServers.add(asd.getIp());
        }
        return appServers;
    }

    @Override
    public List<GroupServer> listGroupServersByGroup(String appName) throws DalException {
        GroupDo d = appDao.findByName(appName, GroupEntity.READSET_FULL);
        if (d == null)
            return null;
        List<GroupServer> appServers = new ArrayList<>();
        for (GroupServerDo asd : appServerDao.findAllByGroup(d.getId(), GroupServerEntity.READSET_FULL)) {
            appServers.add(C.toGroupServer(asd));
        }
        return appServers;
    }


    private Group createGroup(GroupDo d) throws DalException {
        if (d == null)
            return null;
        Group app = C.toGroup(d);
        cascadeQuery(d, app);
        return app;
    }

    private void cascadeQuery(GroupDo d, Group app) throws DalException {
        queryGroupSlbs(d.getName(), app);
        queryGroupHealthCheck(d.getId(), app);
        queryLoadBalancingMethod(d.getId(), app);
        queryGroupServers(d.getId(), app);
    }

    private void queryGroupSlbs(String appName, Group app) throws DalException {
        List<GroupSlbDo> list = appSlbDao.findAllByGroup(appName, GroupSlbEntity.READSET_FULL);
        for (GroupSlbDo d : list) {
            GroupSlb e = C.toGroupSlb(d);
            app.addGroupSlb(e);
            querySlbVips(d.getSlbName(), e);
            queryVirtualServer(d.getSlbName(), d.getSlbVirtualServerName(), e);
        }
    }

    private void querySlbVips(String slbName, GroupSlb appSlb) throws DalException {
        SlbDo sd = slbDao.findByName(slbName, SlbEntity.READSET_FULL);
        List<SlbVipDo> list = slbVipDao.findAllBySlb(sd.getId(), SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            appSlb.addVip(e);
        }
    }

    private void queryVirtualServer(String slbName, String slbVirtualServerName, GroupSlb appSlb) throws DalException {
        SlbDo slbDo = slbDao.findByName(slbName, SlbEntity.READSET_FULL);
        SlbVirtualServerDo d = slbVirtualServerDao.findAllBySlbAndName(slbDo.getId(), slbVirtualServerName, SlbVirtualServerEntity.READSET_FULL);
        appSlb.setSlbName(slbDo.getName());
        if (d == null)
            return;
        VirtualServer e = C.toVirtualServer(d);
        appSlb.setVirtualServer(e);
        querySlbDomains(d.getId(), e);
    }

    private void querySlbDomains(long slbVirtualServerId, VirtualServer virtualServer) throws DalException {
        List<SlbDomainDo> list = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        for (SlbDomainDo d : list) {
            Domain e = C.toDomain(d);
            virtualServer.addDomain(e);
        }
    }

    private void queryGroupHealthCheck(long appKey, Group app) throws DalException {
        GroupHealthCheckDo d = appHealthCheckDao.findByGroup(appKey, GroupHealthCheckEntity.READSET_FULL);
        if (d == null)
            return;
        HealthCheck e = C.toHealthCheck(d);
        app.setHealthCheck(e);
    }

    private void queryLoadBalancingMethod(long appKey, Group app) throws DalException {
        GroupLoadBalancingMethodDo d = appLoadBalancingMethodDao.findByGroup(appKey, GroupLoadBalancingMethodEntity.READSET_FULL);
        if (d == null)
            return;
        LoadBalancingMethod e = C.toLoadBalancingMethod(d);
        app.setLoadBalancingMethod(e);
    }

    private void queryGroupServers(long appKey, Group app) throws DalException {
        List<GroupServerDo> list = appServerDao.findAllByGroup(appKey, GroupServerEntity.READSET_FULL);
        for (GroupServerDo d : list) {
            GroupServer e = C.toGroupServer(d);
            app.addGroupServer(e);
        }
    }
}
