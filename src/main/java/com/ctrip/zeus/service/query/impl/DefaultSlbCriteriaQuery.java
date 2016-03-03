package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/8/27.
 */
@Component("slbCriteriaQuery")
public class DefaultSlbCriteriaQuery implements SlbCriteriaQuery {
    @Resource
    private SlbDao slbDao;
    @Resource
    private RSlbSlbServerDao rSlbSlbServerDao;
    @Resource
    private RVsSlbDao rVsSlbDao;
    @Resource
    private RSlbStatusDao rSlbStatusDao;

    @Override
    public Long queryByName(String name) throws Exception {
        SlbDo s = slbDao.findByName(name, SlbEntity.READSET_FULL);
        return s == null ? 0L : s.getId();
    }

    @Override
    public Set<IdVersion> queryByIdsAndMode(Long[] slbIds, SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelSlbStatusDo d : rSlbStatusDao.findBySlbs(slbIds, RSlbStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getSlbId(), v));
            }
        }
        return result;
    }

    @Override
    public IdVersion[] queryByIdAndMode(Long slbId, SelectionMode mode) throws Exception {
        RelSlbStatusDo d = rSlbStatusDao.findBySlb(slbId, RSlbStatusEntity.READSET_FULL);
        int[] v = VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion());

        IdVersion[] result = new IdVersion[v.length];
        for (int i = 0; i < result.length && i < v.length; i++) {
            result[i] = new IdVersion(slbId, v[i]);
        }
        return result;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> slbIds = new HashSet<>();
        for (SlbDo slbDo : slbDao.findAll(SlbEntity.READSET_FULL)) {
            slbIds.add(slbDo.getId());
        }
        return slbIds;
    }

    @Override
    public Set<IdVersion> queryAll(SelectionMode mode) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        Set<Long> slbIds = queryAll();
        for (RelSlbStatusDo d : rSlbStatusDao.findBySlbs(slbIds.toArray(new Long[slbIds.size()]), RSlbStatusEntity.READSET_FULL)) {
            for (int v : VersionUtils.getVersionByMode(mode, d.getOfflineVersion(), d.getOnlineVersion())) {
                result.add(new IdVersion(d.getSlbId(), v));
            }
        }
        return result;
    }

    @Override
    public Set<IdVersion> queryBySlbServerIp(String ip) throws Exception {
        Set<IdVersion> result = new HashSet<>();
        for (RelSlbSlbServerDo relSlbSlbServerDo : rSlbSlbServerDao.findByIp(ip, RSlbSlbServerEntity.READSET_FULL)) {
            result.add(new IdVersion(relSlbSlbServerDo.getSlbId(), relSlbSlbServerDo.getSlbVersion()));
        }
        return result;
    }

    @Override
    public Set<Long> queryByVs(IdVersion vsIdVersion) throws Exception {
        Set<Long> result = new HashSet<>();
        for (RelVsSlbDo relVsSlbDo : rVsSlbDao.findByVs(vsIdVersion.getId(), RVsSlbEntity.READSET_FULL)) {
            if (vsIdVersion.getVersion().equals(relVsSlbDo.getVsVersion()))
                result.add(relVsSlbDo.getSlbId());
        }
        return result;
    }

    @Override
    public Set<Long> queryByVses(IdVersion[] vsIdVersions) throws Exception {
        Set<Long> result = new HashSet<>();
        Map<IdVersion, Long> map = new HashMap();
        for (IdVersion vsIdVersion : vsIdVersions) {
            map.put(vsIdVersion, vsIdVersion.getId());
        }
        for (RelVsSlbDo relVsSlbDo : rVsSlbDao.findByVses(map.values().toArray(new Long[map.size()]), RVsSlbEntity.READSET_FULL)) {
            if (result.contains(relVsSlbDo.getSlbId()))
                continue;
            if (map.keySet().contains(new IdVersion(relVsSlbDo.getVsId(), relVsSlbDo.getVsVersion())))
                result.add(relVsSlbDo.getSlbId());
        }
        return result;
    }
}
