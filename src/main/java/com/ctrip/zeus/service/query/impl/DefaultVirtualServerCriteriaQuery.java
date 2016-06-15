package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.VersionUtils;
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
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RVsStatusDao rVsStatusDao;

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> result = new HashSet<>();
        for (SlbVirtualServerDo slbVirtualServerDo : slbVirtualServerDao.findAll(SlbVirtualServerEntity.READSET_IDONLY)) {
            result.add(slbVirtualServerDo.getId());
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> vsIds = queryAll();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds.toArray(new Long[vsIds.size()]), RVsStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getVsId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] vsIds, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds, RVsStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getVsId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long vsId, SelectionMode mode) throws Exception {
        RelVsStatusDo d = rVsStatusDao.findByVs(vsId, RVsStatusEntity.READSET_FULL);
        if (d == null) return new IdVersion[0];

        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < result.length && i < v.length; i++) {
            result[i] = new IdVersion(vsId, v[i]);
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryBySlbId(Long slbId) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsSlbDo d : rVsSlbDao.findAllBySlb(slbId, RVsSlbEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getVsVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryBySlbIds(Long[] slbIds) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsSlbDo d : rVsSlbDao.findAllBySlbs(slbIds, RVsSlbEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getVsVersion()));
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryByDomain(String domain) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsDomainDo d : rVsDomainDao.findAllByDomain(domain.toLowerCase(), RVsDomainEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getVsVersion()));
        }
        return result;
    }
}
