package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("slbQuery")
public class SlbQueryImpl implements SlbQuery {
    @Resource
    private GroupSlbDao groupSlbDao;
    @Resource
    private SlbDao slbDao;
    @Resource
    private SlbServerDao slbServerDao;
    @Resource
    private SlbVipDao slbVipDao;

    @Override
    public Slb get(String slbName) throws Exception {
        SlbDo d = slbDao.findByName(slbName, SlbEntity.READSET_FULL);
        return createSlb(d);
    }

    @Override
    public Slb getById(Long id) throws Exception {
        SlbDo d = slbDao.findByPK(id, SlbEntity.READSET_FULL);
        return createSlb(d);
    }

    @Override
    public Slb getBySlbServer(String slbServerIp) throws Exception {
        SlbServerDo ss = slbServerDao.findByIp(slbServerIp, SlbServerEntity.READSET_FULL);
        return ss == null ? null : getById(ss.getSlbId());
    }

    @Override
    public List<Slb> batchGet(Long[] ids) throws Exception {
        List<Slb> result = new ArrayList<>();
        for (SlbDo slbDo : slbDao.findAllByIds(ids, SlbEntity.READSET_FULL)) {
            Slb s = createSlb(slbDo);
            if (s == null)
                continue;
            result.add(s);
        }
        return result;
    }

    @Override
    public List<Slb> getAll() throws Exception {
        List<Slb> list = new ArrayList<>();
        for (SlbDo d : slbDao.findAll(SlbEntity.READSET_FULL)) {
            Slb slb = createSlb(d);
            if (slb != null)
                list.add(slb);
        }
        return list;
    }

    @Override
    public List<Slb> getByGroups(Long[] groupIds) throws Exception {
        Set<Long> visitedIds = new HashSet<>();
        for (GroupSlbDo groupSlbDo : groupSlbDao.findAllByGroups(groupIds, GroupSlbEntity.READSET_FULL)) {
            if (visitedIds.contains(groupSlbDo.getSlbId()))
                continue;
            visitedIds.add(groupSlbDo.getSlbId());
        }
        if (visitedIds.size() == 0)
            return new ArrayList<>();
        return batchGet(visitedIds.toArray(new Long[visitedIds.size()]));
    }

    private Slb createSlb(SlbDo d) throws Exception {
        if (d == null)
            return null;
        if (d.getName() == null || d.getName().isEmpty())
            return null;
        Slb slb = C.toSlb(d);
        cascadeQuery(slb);
        return slb;
    }

    private void cascadeQuery(Slb slb) throws Exception {
        querySlbVips(slb);
        querySlbServers(slb);
    }

    private void querySlbVips(Slb slb) throws DalException {
        List<SlbVipDo> list = slbVipDao.findAllBySlb(slb.getId(), SlbVipEntity.READSET_FULL);
        for (SlbVipDo d : list) {
            Vip e = C.toVip(d);
            slb.addVip(e);
        }
    }

    private void querySlbServers(Slb slb) throws DalException {
        List<SlbServerDo> list = slbServerDao.findAllBySlb(slb.getId(), SlbServerEntity.READSET_FULL);
        for (SlbServerDo d : list) {
            SlbServer e = C.toSlbServer(d);
            slb.addSlbServer(e);
        }
    }
}
