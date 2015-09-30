package com.ctrip.zeus.service.query.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/8/27.
 */
@Component("slbCriteriaQuery")
public class DefaultSlbCriteriaQuery implements SlbCriteriaQuery {
    @Resource
    private SlbDao slbDao;
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RSlbSlbServerDao rSlbSlbServerDao;
    @Resource
    private RVsSlbDao rVsSlbDao;

    @Override
    public Long queryByName(String name) throws Exception {
        SlbDo s = slbDao.findByName(name, SlbEntity.READSET_FULL);
        return s == null ? 0L : s.getId();
    }

    @Override
    public Long queryBySlbServerIp(String ip) throws Exception {
        RelSlbSlbServerDo ss = rSlbSlbServerDao.findSlbByIp(ip, RSlbSlbServerEntity.READSET_FULL);
        return ss == null ? 0L : ss.getSlbId();
    }

    @Override
    public Long queryByVs(Long vsId) throws Exception {
        RelVsSlbDo vs = rVsSlbDao.findSlbByVs(vsId, RVsSlbEntity.READSET_FULL);
        return vs == null ? 0L : vs.getSlbId();
    }

    @Override
    public Set<Long> queryByGroups(Long[] groupIds) throws Exception {
        Set<Long> slbIds = new HashSet<>();
        List<Long> vsIds = new ArrayList<>();
        for (RelGroupVsDo relGroupVsDo : rGroupVsDao.findAllVsesByGroups(groupIds, RGroupVsEntity.READSET_FULL)) {
            vsIds.add(relGroupVsDo.getVsId());
        }
        for (RelVsSlbDo relVsSlbDo : rVsSlbDao.findSlbsByVses(vsIds.toArray(new Long[vsIds.size()]), RVsSlbEntity.READSET_FULL)) {
            slbIds.add(relVsSlbDo.getSlbId());
        }
        return slbIds;
    }

    @Override
    public Set<Long> queryAll() throws Exception {
        Set<Long> slbIds = new HashSet<>();
        for (SlbDo slbDo : slbDao.findAll(SlbEntity.READSET_FULL)) {
            slbIds.add(slbDo.getId());
        }
        return slbIds;
    }
}
