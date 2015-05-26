package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.support.C;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("appSync")
public class GroupSyncImpl implements GroupSync {
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

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public GroupDo add(Group app) throws DalException, ValidationException {
        validate(app);
        GroupDo d= C.toAppDo(app);
        d.setCreatedTime(new Date());
        d.setVersion(1);

        appDao.insert(d);
        cascadeSync(d, app);

        return d;
    }

    @Override
    public GroupDo update(Group app) throws DalException, ValidationException {
        validate(app);
        GroupDo check = appDao.findByName(app.getName(), GroupEntity.READSET_FULL);
        if (check.getVersion() > app.getVersion())
            throw new ValidationException("Newer Group version is detected.");

        GroupDo d= C.toGroupDo(app);
        appDao.updateByName(d, GroupEntity.UPDATESET_FULL);

        GroupDo updated = appDao.findByName(app.getName(), GroupEntity.READSET_FULL);
        d.setId(updated.getId());
        d.setVersion(updated.getVersion());
        cascadeSync(d, app);
        return d;
    }

    @Override
    public int delete(String name) throws DalException {
        GroupDo d = appDao.findByName(name, GroupEntity.READSET_FULL);
        if (d == null)
            return 0;
        appSlbDao.deleteByGroup(new GroupSlbDo().setGroupName(d.getName()));
        appServerDao.deleteByGroup(new GroupServerDo().setGroupId(d.getId()));
        appHealthCheckDao.deleteByGroup(new GroupHealthCheckDo().setGroupId(d.getId()));
        appLoadBalancingMethodDao.deleteByGroup(new GroupLoadBalancingMethodDo().setGroupId(d.getId()));

        return appDao.deleteByName(d);
    }

    private void validate(Group app) throws DalException, ValidationException {
        if (app == null) {
            throw new ValidationException("Group with null value cannot be persisted.");
        }
        if (!validateSlb(app))
            throw new ValidationException("Group with invalid slb data cannot be persisted.");
    }

    private boolean validateSlb(Group app) throws DalException {
        if (app.getGroupSlbs().size() == 0)
            return false;
        for (GroupSlb as : app.getGroupSlbs()) {
            if (slbDao.findByName(as.getSlbName(), SlbEntity.READSET_FULL) == null)
                return false;
        }
        return true;
    }

    private void cascadeSync(GroupDo d, Group app) throws DalException {
        syncGroupSlbs(app.getName(), app.getGroupSlbs());
        syncGroupHealthCheck(d.getId(), app.getHealthCheck());
        syncLoadBalancingMethod(d.getId(), app.getLoadBalancingMethod());
        syncGroupServers(d.getId(), app.getGroupServers());
    }

    private void syncGroupSlbs(String appName, List<GroupSlb> appSlbs) throws DalException {
        List<GroupSlbDo> oldList = appSlbDao.findAllByGroup(appName, GroupSlbEntity.READSET_FULL);
        Map<String, GroupSlbDo> oldMap = Maps.uniqueIndex(oldList, new Function<GroupSlbDo, String>() {
            @Override
            public String apply(GroupSlbDo input) {
                return input.getGroupName() + input.getSlbName() + input.getSlbVirtualServerName();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (GroupSlb e : appSlbs) {
            GroupSlbDo old = oldMap.get(appName + e.getSlbName() + e.getVirtualServer().getName());
            if (old != null) {
                oldList.remove(old);
            }
            appSlbDao.insert(C.toGroupSlbDo(e)
                    .setGroupName(appName)
                    .setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (GroupSlbDo d : oldList) {
            appSlbDao.deleteByPK(new GroupSlbDo().setId(d.getId()));
        }
    }

    private void syncGroupHealthCheck(long appKey, HealthCheck healthCheck) throws DalException {
        if (healthCheck == null) {
            logger.info("No health check method is found when adding/updating app with id " + appKey);
            return;
        }
        appHealthCheckDao.insert(C.toGroupHealthCheckDo(healthCheck)
                .setGroupId(appKey)
                .setCreatedTime(new Date()));
    }

    private void syncLoadBalancingMethod(long appKey, LoadBalancingMethod loadBalancingMethod) throws DalException {
        if (loadBalancingMethod == null)
            return;
        appLoadBalancingMethodDao.insert(C.toGroupLoadBalancingMethodDo(loadBalancingMethod)
                .setGroupId(appKey)
                .setCreatedTime(new Date()));
    }

    private void syncGroupServers(long appKey, List<GroupServer> appServers) throws DalException {
        if (appServers == null || appServers.size() == 0) {
            logger.warn("No app server is given when adding/update app with id " + appKey);
            return;
        }
        List<GroupServerDo> oldList = appServerDao.findAllByGroup(appKey, GroupServerEntity.READSET_FULL);
        Map<String, GroupServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<GroupServerDo, String>() {
            @Override
            public String apply(GroupServerDo input) {
                return input.getGroupId() + input.getIp();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (GroupServer e : appServers) {
            GroupServerDo old = oldMap.get(appKey + e.getIp());
            if (old != null) {
                oldList.remove(old);
            }
            appServerDao.insert(C.toGroupServerDo(e)
                    .setGroupId(appKey)
                    .setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (GroupServerDo d : oldList) {
            appServerDao.deleteByPK(new GroupServerDo().setId(d.getId()));
        }
    }
}
