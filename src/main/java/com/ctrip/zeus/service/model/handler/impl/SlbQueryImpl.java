package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("slbQuery")
public class SlbQueryImpl implements SlbQuery {
    @Resource
    private SlbDao slbDao;
    @Resource
    private SlbServerDao slbServerDao;
    @Resource
    private SlbVipDao slbVipDao;
    @Override
    
    public Slb getById(Long id) throws Exception {
        SlbDo d = slbDao.findByPK(id, SlbEntity.READSET_FULL);
        return createSlb(d);
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
