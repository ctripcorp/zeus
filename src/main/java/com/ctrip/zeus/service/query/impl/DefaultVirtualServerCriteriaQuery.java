package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhoumy on 2015/9/11.
 */
@Component("virtualServerCriteriaQuery")
public class DefaultVirtualServerCriteriaQuery implements VirtualServerCriteriaQuery {
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private RVsSlbDao rVsSlbDao;
    @Resource
    private RVsDomainDao rVsDomainDao;

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbVirtualServerDo slbVirtualServerDo : slbVirtualServerDao.findAll(SlbVirtualServerEntity.READSET_FULL)) {
            result.add(slbVirtualServerDo.getId());
        }
        return result;
    }

    @Override
    public Set<Long> queryBySlbId(Long slbId) throws Exception {
        Set<Long> result = new HashSet<>();
        for (RelVsSlbDo relVsSlbDo : rVsSlbDao.findAllVsesBySlb(slbId, RVsSlbEntity.READSET_FULL)) {
            result.add(relVsSlbDo.getVsId());
        }
        return result;
    }

    @Override
    public Set<Long> queryByDomain(String domain) throws Exception {
        Set<Long> result = new HashSet<>();
        for (RelVsDomainDo relVsDomainDo : rVsDomainDao.findAllVsesByDomain(domain, RVsDomainEntity.READSET_FULL)) {
            result.add(relVsDomainDo.getVsId());
        }
        return result;
    }
}
