package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.model.ModelMode;
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
        for (SlbVirtualServerDo slbVirtualServerDo : slbVirtualServerDao.findAll(SlbVirtualServerEntity.READSET_FULL)) {
            result.add(slbVirtualServerDo.getId());
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryAll(ModelMode mode) throws Exception {
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
    public Set<IdVersion> queryByIdsAndMode(Long[] vsIds, ModelMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelVsStatusDo d : rVsStatusDao.findByVses(vsIds, RVsStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getVsId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long vsId, ModelMode mode) throws Exception {
        IdVersion[] result = new IdVersion[2];
        RelVsStatusDo d = rVsStatusDao.findByVs(vsId, RVsStatusEntity.READSET_FULL);
        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion());
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
    public Set<IdVersion> queryByGroupIds(IdVersion[] groupKeys) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Integer[] hashes = new Integer[groupKeys.length];
        String[] values = new String[groupKeys.length];
        for (int i = 0; i < groupKeys.length; i++) {
            hashes[i] = groupKeys[i].hashCode();
            values[i] = groupKeys[i].toString();
        }
        for (RelGroupVsDo d : rGroupVsDao.findAllByGroupAndVersion(hashes, values, RGroupVsEntity.READSET_FULL)) {
            result.add(new IdVersion(d.getVsId(), d.getGroupVersion()));
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
