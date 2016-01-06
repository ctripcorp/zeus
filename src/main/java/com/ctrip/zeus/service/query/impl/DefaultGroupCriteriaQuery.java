package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.model.ModelMode;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.IdVersion;
import com.ctrip.zeus.service.query.VersionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/8/7.
 */
@Component("groupCriteriaQuery")
public class DefaultGroupCriteriaQuery implements GroupCriteriaQuery {
    @Resource
    private GroupDao groupDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RGroupGsDao rGroupGsDao;
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    @Override
    public Long queryByName(String name) throws Exception {
        GroupDo g = groupDao.findByName(name, GroupEntity.READSET_FULL);
        return g == null ? 0L : g.getId();
    }


    @Override
    public Set<Long> queryByAppId(String appId) throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupDo groupDo : groupDao.findByAppId(appId, GroupEntity.READSET_FULL)) {
            groupIds.add(groupDo.getId());
        }
        return groupIds;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupDo groupDo : groupDao.findAll(GroupEntity.READSET_FULL)) {
            groupIds.add(groupDo.getId());
        }
        return groupIds;
    }

    @Override
    public Set<Long> queryAllVGroups() throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (RelGroupVgDo relGroupVgDo : rGroupVgDao.findAll(RGroupVgEntity.READSET_FULL)) {
            groupIds.add(relGroupVgDo.getGroupId());
        }
        return groupIds;
    }

    @Override
    public Set<IdVersion> queryAll(ModelMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> groupIds = queryAll();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds.toArray(new Long[groupIds.size()]), RGroupStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryAllVGroups(ModelMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> groupIds = queryAllVGroups();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(groupIds.toArray(new Long[groupIds.size()]), RGroupStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getGroupId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByVsId(Long vsId) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelGroupVsDo relGroupVsDo : rGroupVsDao.findAllByVs(vsId, RGroupVsEntity.READSET_FULL)) {
            result.add(new IdVersion(relGroupVsDo.getGroupId(), relGroupVsDo.getGroupVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByVsIds(Long[] vsIds) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelGroupVsDo relGroupVsDo : rGroupVsDao.findAllByVses(vsIds, RGroupVsEntity.READSET_FULL)) {
            result.add(new IdVersion(relGroupVsDo.getGroupId(), relGroupVsDo.getGroupVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByGroupServerIp(String ip) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelGroupGsDo relGroupGsDo : rGroupGsDao.findAllByIp(ip, RGroupGsEntity.READSET_FULL)) {
            result.add(new IdVersion(relGroupGsDo.getGroupId(), relGroupGsDo.getGroupVersion()));
        }
        return result;
    }
}
