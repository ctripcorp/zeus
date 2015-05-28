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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
@Component("slbSync")
public class SlbSyncImpl implements SlbSync {
    @Resource
    private GroupSlbDao appSlbDao;
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

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void add(Slb slb) throws DalException, ValidationException {
        validate(slb);
        SlbDo d = C.toSlbDo(0L, slb);
        d.setCreatedTime(new Date());
        d.setVersion(1);

        slbDao.insert(d);
        slb.setId(d.getId());
        cascadeSync(slb);
    }

    @Override
    public void update(Slb slb) throws DalException, ValidationException {
        validate(slb);
        SlbDo check = slbDao.findById(slb.getId(), SlbEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Slb does not exist.");
        if (check.getVersion() > slb.getVersion())
            throw new ValidationException("Newer Slb version is detected.");
        SlbDo d = C.toSlbDo(slb.getId(), slb);
        slbDao.updateById(d, SlbEntity.UPDATESET_FULL);
        cascadeSync(slb);
    }

    @Override
    public int delete(Long slbId) throws DalException, ValidationException {
        SlbDo d = slbDao.findById(slbId, SlbEntity.READSET_FULL);
        if (d == null)
            return 0;
        if(removable(d)) {
            slbVipDao.deleteBySlb(new SlbVipDo().setSlbId(d.getId()));
            slbServerDao.deleteBySlb(new SlbServerDo().setSlbId(d.getId()));
            for (SlbVirtualServerDo svsd : slbVirtualServerDao.findAllBySlb(d.getId(), SlbVirtualServerEntity.READSET_FULL)) {
                deleteSlbVirtualServer(svsd.getId());
            }
            return slbDao.deleteByPK(d);
        }
        throw new ValidationException(d.getName() + " cannot be deleted. Dependency exists");
    }

    private void validate(Slb slb) throws ValidationException {
        if (slb == null || slb.getName() == null || slb.getName().isEmpty()) {
            throw new ValidationException("Slb with null value cannot be persisted.");
        }
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Slb with invalid server data cannot be persisted.");
        }
    }

    private boolean removable(SlbDo d) throws DalException {
        List<GroupSlbDo> list = appSlbDao.findAllBySlb(d.getId(), GroupSlbEntity.READSET_FULL);
        if (list.size() == 0)
            return true;
        return false;
    }

    private void cascadeSync(Slb slb) throws DalException {
        syncSlbVips(slb.getId(), slb.getVips());
        syncSlbServers(slb.getId(), slb.getSlbServers());
        syncVirtualServers(slb.getId(), slb.getVirtualServers());
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

    private void syncVirtualServers(Long slbId, List<VirtualServer> virtualServers) throws DalException {
        if (virtualServers == null || virtualServers.size() == 0)
            return;
        List<SlbVirtualServerDo> oldList = slbVirtualServerDao.findAllBySlb(slbId,SlbVirtualServerEntity.READSET_FULL);
        Map<String, SlbVirtualServerDo> oldMap = Maps.uniqueIndex(oldList, new Function<SlbVirtualServerDo, String>() {
            @Override
            public String apply(SlbVirtualServerDo input) {
                return input.getName();
            }
        });

        //Update existed if necessary, and insert new ones.
        for (VirtualServer e : virtualServers) {
            SlbVirtualServerDo old = oldMap.get(e.getName());
            if (old != null) {
                oldList.remove(old);
            }
            SlbVirtualServerDo d = C.toSlbVirtualServerDo(0L, e).setSlbId(slbId).setCreatedTime(new Date());
            slbVirtualServerDao.insert(d);

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
