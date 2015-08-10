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
    private SlbDomainDao slbDomainDao;
    @Resource
    private GroupSlbDao groupSlbDao;

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> groupIds = new HashSet<>();
        for (GroupDo groupDo : groupDao.findAll(GroupEntity.READSET_FULL)) {
            groupIds.add(groupDo.getId());
        }
        return groupIds;
    }

    @Override
    public Set<Long> queryByDomain(String domain) throws Exception {
        List<SlbDomainDo> dlist = slbDomainDao.findAllByName(domain, SlbDomainEntity.READSET_FULL);
        Long[] vsIds = new Long[dlist.size()];
        for (int i = 0; i < dlist.size(); i++) {
            vsIds[i] = dlist.get(i).getSlbVirtualServerId();
        }
        Set<Long> groupIds = new HashSet<>();
        for (GroupSlbDo groupSlbDo : groupSlbDao.findAllByVirtualServers(vsIds, GroupSlbEntity.READSET_FULL)) {
            groupIds.add(groupSlbDo.getGroupId());
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
}
