package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.support.C;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("slbSync")
public class SlbSyncImpl implements SlbSync {
    @Resource
    private SlbDao slbDao;
    @Resource
    private SlbServerDao slbServerDao;
    @Resource
    private SlbVipDao slbVipDao;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Long add(Slb slb) throws Exception {
        SlbDo d = C.toSlbDo(0L, slb);
        d.setCreatedTime(new Date()).setVersion(1);
        slbDao.insert(d);
        syncSlbVips(d.getId(), slb.getVips());
        syncSlbServers(d.getId(), slb.getSlbServers());
        return d.getId();
    }

    @Override
    public Long update(Slb slb) throws Exception {
        SlbDo check = slbDao.findById(slb.getId(), SlbEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Slb does not exist.");
        if (check.getVersion() > slb.getVersion())
            throw new ValidationException("Newer Slb version is detected.");
        SlbDo d = C.toSlbDo(slb.getId(), slb);
        slbDao.updateById(d, SlbEntity.UPDATESET_FULL);
        syncSlbVips(d.getId(), slb.getVips());
        syncSlbServers(d.getId(), slb.getSlbServers());
        return d.getId();
    }

    @Override
    public void updateVersion(Long slbId) throws Exception {
        slbDao.updateById(slbDao.findById(slbId, SlbEntity.READSET_FULL), SlbEntity.UPDATESET_FULL);
    }

    @Override
    public int delete(Long slbId) throws Exception {
        slbVipDao.deleteBySlb(new SlbVipDo().setSlbId(slbId));
        slbServerDao.deleteBySlb(new SlbServerDo().setSlbId(slbId));
        return slbDao.deleteByPK(new SlbDo().setId(slbId));
    }

    private void syncSlbVips(Long slbId, List<Vip> vips) throws DalException {
        List<SlbVipDo> originVips = slbVipDao.findAllBySlb(slbId, SlbVipEntity.READSET_FULL);
        Map<String, SlbVipDo> uniqueCheck = Maps.uniqueIndex(
                originVips, new Function<SlbVipDo, String>() {
                    @Override
                    public String apply(SlbVipDo input) {
                        return input.getIp();
                    }
                });
        for (Vip e : vips) {
            SlbVipDo originVip = uniqueCheck.get(e.getIp());
            if (originVip != null) {
                originVips.remove(originVip);
            }
            slbVipDao.insert(C.toSlbVipDo(slbId, e).setCreatedTime(new Date()));
        }
        for (SlbVipDo d : originVips) {
            slbVipDao.deleteByPK(d);
        }
    }

    private void syncSlbServers(Long slbId, List<SlbServer> slbServers) throws DalException {
        List<SlbServerDo> originServers = slbServerDao.findAllBySlb(slbId, SlbServerEntity.READSET_FULL);
        Map<String, SlbServerDo> uniqueCheck = Maps.uniqueIndex(
                originServers, new Function<SlbServerDo, String>() {
                    @Override
                    public String apply(SlbServerDo input) {
                        return input.getIp();
                    }
                });
        for (SlbServer e : slbServers) {
            SlbServerDo originServer = uniqueCheck.get(e.getIp());
            if (originServer != null) {
                originServers.remove(originServer);
            }
            slbServerDao.insert(C.toSlbServerDo(slbId, e).setCreatedTime(new Date()));
        }
        for (SlbServerDo d : originServers) {
            slbServerDao.deleteByPK(d);
        }
    }
}
