package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/8/7.
 */
@Component("groupCriteriaQuery")
public class DefaultGroupCriteriaQuery implements GroupCriteriaQuery {
    @Resource
    private GroupDao groupDao;
    @Resource
    private GroupSlbDao groupSlbDao;

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
    public Set<Long> queryBySlbId(Long slbId) throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupSlbDo groupSlbDo : groupSlbDao.findAllBySlb(slbId, GroupSlbEntity.READSET_FULL)) {
            groupIds.add(groupSlbDo.getGroupId());
        }
        return groupIds;
    }

    @Override
    public Set<Long> queryByVsIds(Long[] vsIds) throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupSlbDo groupSlbDo : groupSlbDao.findAllByVirtualServers(vsIds, GroupSlbEntity.READSET_FULL)) {
            groupIds.add(groupSlbDo.getGroupId());
        }
        return groupIds;
    }
}
