package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zhoumy on 2015/6/30.
 */
@Component("slbModelValidator")
public class DefaultSlbValidator implements SlbValidator {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbDao slbDao;
    @Resource
    private RSlbStatusDao rSlbStatusDao;

    @Override
    public boolean exists(Long targetId) throws Exception {
        return slbDao.findById(targetId, SlbEntity.READSET_FULL) != null;
    }

    @Override
    public void validate(Slb slb) throws Exception {
        if (slb.getName() == null || slb.getName().isEmpty()) {
            throw new ValidationException("Slb name is required.");
        }
        if (slb.getVips() == null || slb.getVips().size() == 0) {
            throw new ValidationException("Slb vip is required.");
        }
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Slb without slb servers cannot be created.");
        }
        Long nameCheck = slbCriteriaQuery.queryByName(slb.getName());
        if (!nameCheck.equals(0L) && !nameCheck.equals(slb.getId())) {
            throw new ValidationException("Duplicate name " + slb.getName() + " is found at slb " + nameCheck + ".");
        }

        Set<String> ips = new HashSet<>();
        // check if any other slb version who has the server ip is still in effect.
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!ips.add(slbServer.getIp())) {
                throw new ValidationException("Duplicate ip " + slbServer.getIp() + " is found.");
            }

            Set<IdVersion> range = slbCriteriaQuery.queryBySlbServerIp(slbServer.getIp());
            Set<Long> check = new HashSet<>();
            Iterator<IdVersion> iter = range.iterator();
            while (iter.hasNext()) {
                Long e = iter.next().getId();
                if (e.equals(slb.getId())) {
                    iter.remove();
                } else {
                    check.add(e);
                }
            }
            if (check.size() == 0)
                continue;
            check.remove(slb.getId());
            if (check.size() > 0) {
                throw new ValidationException("Slb server " + slbServer.getIp() + " exists in the system. Unique server ip is required.");
            }
        }
    }

    @Override
    public void checkVersionForUpdate(Slb target) throws Exception {
        SlbDo check = slbDao.findById(target.getId(), SlbEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Slb with id " + target.getId() + " does not exist.");
        if (!target.getVersion().equals(check.getVersion()))
            throw new ValidationException("Newer Group version is detected.");
    }

    @Override
    public void removable(Long slbId) throws Exception {
        if (virtualServerCriteriaQuery.queryBySlbId(slbId).size() > 0) {
            throw new ValidationException("Slb with id " + slbId + " cannot be deleted. Dependencies exist.");
        }
        if (rSlbStatusDao.findBySlb(slbId, RSlbStatusEntity.READSET_FULL).getOnlineVersion() != 0) {
            throw new ValidationException("Slb must be deactivated before deletion.");
        }
    }
}
