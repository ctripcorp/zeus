package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
        String[] ips = new String[slb.getSlbServers().size()];
        for (int i = 0; i < ips.length; i++) {
            ips[i] = slb.getSlbServers().get(i).getIp();
        }
        for (SlbServer slbServer : slb.getSlbServers()) {
            Long slbId = null;//slbCriteriaQuery.queryBySlbServerIp(slbServer.getIp());
            if (!slbId.equals(0L) && !slbId.equals(slb.getId())) {
                throw new ValidationException("Slb server " + slbServer.getIp() + " is added to slb " + slbId + ". Unique server ip is required.");
            }
        }
    }

    @Override
    public void checkVersion(Slb target) throws Exception {
        SlbDo check = slbDao.findById(target.getId(), SlbEntity.READSET_FULL);
        if (check == null)
            throw new ValidationException("Slb with id " + target.getId() + " does not exist.");
        if (!target.getVersion().equals(check.getVersion()))
            throw new ValidationException("Newer Group version is detected.");
    }

    @Override
    public void removable(Long slbId) throws Exception {
        if (virtualServerCriteriaQuery.queryBySlbId(slbId).size() > 0)
            throw new ValidationException("Slb with id " + slbId + " cannot be deleted. Dependencies exist.");
    }
}
