package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.service.model.handler.SlbValidator;
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
    private SlbDomainDao slbDomainDao;
    @Resource
    private SlbServerDao slbServerDao;
    @Resource
    private SlbVipDao slbVipDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;

    @Resource
    private SlbValidator slbModelValidator;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void add(Slb slb) throws DalException, ValidationException {
        slbModelValidator.validate(slb);
        SlbDo d = C.toSlbDo(0L, slb);
        d.setCreatedTime(new Date());
        d.setVersion(1);

        slbDao.insert(d);
        Long id = d.getId();
        slb.setId(id);
        syncSlbVips(id, slb.getVips());
        syncSlbServers(id, slb.getSlbServers());
        addVirtualServer(id, slb.getVirtualServers());
    }

    @Override
    public void update(Slb slb) throws Exception {
        slbModelValidator.validate(slb);
        SlbDo check = slbDao.findById(slb.getId(), SlbEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Slb does not exist.");
        if (check.getVersion() > slb.getVersion())
            throw new ValidationException("Newer Slb version is detected.");
        if (slbModelValidator.modifiable(slb)) {
            SlbDo d = C.toSlbDo(slb.getId(), slb);
            slbDao.updateById(d, SlbEntity.UPDATESET_FULL);
            Long id = d.getId();
            syncSlbVips(id, slb.getVips());
            syncSlbServers(id, slb.getSlbServers());
            updateVirtualServer(id, slb.getVirtualServers());
            return;
        }
        throw new ValidationException(check.getName() + " cannot be updated. Dependency exists.");
    }

    @Override
    public int delete(Long slbId) throws Exception {
        SlbDo d = slbDao.findById(slbId, SlbEntity.READSET_FULL);
        if (d == null)
            return 0;
        if (slbModelValidator.removable(C.toSlb(d))) {
            slbVipDao.deleteBySlb(new SlbVipDo().setSlbId(slbId));
            slbServerDao.deleteBySlb(new SlbServerDo().setSlbId(slbId));
            for (SlbVirtualServerDo svsd : slbVirtualServerDao.findAllBySlb(slbId, SlbVirtualServerEntity.READSET_FULL)) {
                deleteSlbVirtualServer(svsd.getId());
            }
            return slbDao.deleteByPK(d);
        }
        throw new ValidationException(d.getName() + " cannot be deleted. Dependency exists.");
    }

    private void syncSlbVips(Long slbId, List<Vip> vips) throws DalException {
        if (vips == null || vips.size() == 0)
            return;
        List<SlbVipDo> oldList = slbVipDao.findAllBySlb(slbId, SlbVipEntity.READSET_FULL);
        Map<String, SlbVipDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbVipDo, String>() {
            @Override
            public String apply(SlbVipDo input) {
                return input.getIp();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (Vip e : vips) {
            SlbVipDo old = oldMap.get(e.getIp());
            if (old != null) {
                oldList.remove(old);
            }
            slbVipDao.insert(C.toSlbVipDo(e).setSlbId(slbId).setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (SlbVipDo d : oldList) {
            slbVipDao.deleteByPK(new SlbVipDo().setId(d.getId()));
        }
    }

    private void syncSlbServers(Long slbId, List<SlbServer> slbServers) throws DalException {
        if (slbServers == null || slbServers.size() == 0) {
            logger.warn("No slb server is given when adding/updating slb with id " + slbId);
            return;
        }
        List<SlbServerDo> oldList = slbServerDao.findAllBySlb(slbId, SlbServerEntity.READSET_FULL);
        Map<String, SlbServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbServerDo, String>() {
            @Override
            public String apply(SlbServerDo input) {
                return input.getIp();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (SlbServer e : slbServers) {
            SlbServerDo old = oldMap.get(e.getIp());
            if (old != null) {
                oldList.remove(old);
            }
            slbServerDao.insert(C.toSlbServerDo(e).setSlbId(slbId).setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (SlbServerDo d : oldList) {
            slbServerDao.deleteByPK(new SlbServerDo().setId(d.getId()));
        }
    }

    private void addVirtualServer(Long slbId, List<VirtualServer> virtualServers) throws DalException {
        if (virtualServers == null || virtualServers.size() == 0)
            return;
        for (VirtualServer e : virtualServers) {
            SlbVirtualServerDo d = C.toSlbVirtualServerDo(e.getId() == null ? 0L : e.getId(), e).setSlbId(slbId).setCreatedTime(new Date());
            slbVirtualServerDao.insert(d);
            syncSlbDomain(d.getId(), e.getDomains());
        }
    }

    private void updateVirtualServer(Long slbId, List<VirtualServer> virtualServers) throws DalException {
        List<SlbVirtualServerDo> oldList = slbVirtualServerDao.findAllBySlb(slbId, SlbVirtualServerEntity.READSET_FULL);
        Map<Long, SlbVirtualServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbVirtualServerDo, Long>() {
            @Override
            public Long apply(SlbVirtualServerDo input) {
                return input.getId();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (VirtualServer e : virtualServers) {
            SlbVirtualServerDo old = oldMap.get(e.getId());
            if (old != null) {
                oldList.remove(old);
            }
            SlbVirtualServerDo d = C.toSlbVirtualServerDo(e.getId() == null ? 0L : e.getId(), e).setSlbId(slbId).setCreatedTime(new Date());
            slbVirtualServerDao.insertOrUpdate(d);

            //Domain
            syncSlbDomain(d.getId(), e.getDomains());
        }

        //Remove unused ones.
        for (SlbVirtualServerDo d : oldList) {
            deleteSlbVirtualServer(d.getId());
        }
    }

    private void syncSlbDomain(Long slbVirtualServerId, List<Domain> domains) throws DalException {
        if (domains == null || domains.size() == 0)
            return;
        List<SlbDomainDo> oldList = slbDomainDao.findAllBySlbVirtualServer(slbVirtualServerId, SlbDomainEntity.READSET_FULL);
        Map<String, SlbDomainDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbDomainDo, String>() {
            @Override
            public String apply(SlbDomainDo input) {
                return input.getName();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (Domain e : domains) {
            SlbDomainDo old = oldMap.get(e.getName());
            if (old != null) {
                oldList.remove(old);
            }
            slbDomainDao.insert(C.toSlbDomainDo(e).setSlbVirtualServerId(slbVirtualServerId).setCreatedTime(new Date()));
        }

        //Remove unused ones.
        for (SlbDomainDo d : oldList) {
            slbDomainDao.deleteByPK(new SlbDomainDo().setId(d.getId()));
        }
    }

    private void deleteSlbVirtualServer(Long id) throws DalException {
        slbDomainDao.deleteAllBySlbVirtualServer(new SlbDomainDo().setSlbVirtualServerId(id));
        slbVirtualServerDao.deleteByPK(new SlbVirtualServerDo().setId(id));
    }
}
