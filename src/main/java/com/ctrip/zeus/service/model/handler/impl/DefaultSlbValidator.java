package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/6/30.
 */
@Component("slbModelValidator")
public class DefaultSlbValidator implements SlbValidator {
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private GroupSlbDao groupSlbDao;

    @Override
    public void validate(Slb slb) throws Exception {
        if (slb == null || slb.getName() == null || slb.getName().isEmpty()) {
            throw new ValidationException("Slb with null value cannot be persisted.");
        }
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Slb without slb servers cannot be persisted.");
        }
        validateVirtualServer(slb.getVirtualServers());
    }


    @Override
    public void checkVirtualServerDependencies(Slb slb) throws Exception {
        Set<Long> deleted = new HashSet<>();
        for (SlbVirtualServerDo virtualServer : slbVirtualServerDao.findAllBySlb(slb.getId(), SlbVirtualServerEntity.READSET_FULL)) {
            deleted.add(virtualServer.getId());
        }
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            deleted.remove(virtualServer.getId());
        }
        for (Long vsId : deleted) {
            if (groupSlbDao.findAllByVirtualServer(vsId, GroupSlbEntity.READSET_FULL).size() > 0)
                throw new ValidationException("Virtual server with id " + vsId + "cannot be deleted. Dependencies exist.");
        }
    }

    @Override
    public void validateVirtualServer(List<VirtualServer> virtualServers) throws Exception {
        Set<String> existingHost = new HashSet<>();
        for (VirtualServer virtualServer : virtualServers) {
            for (Domain domain : virtualServer.getDomains()) {
                String key = domain.getName() + ":" + virtualServer.getPort();
                if (existingHost.contains(key))
                    throw new ValidationException("Duplicate domain and port combination is found: " + key);
                else
                    existingHost.add(key);
            }
        }
    }

    @Override
    public void removable(Slb slb) throws Exception {
        if (groupSlbDao.findAllBySlb(slb.getId(), GroupSlbEntity.READSET_FULL).size() > 0)
            throw new ValidationException("Slb with id " + slb.getId() + " cannot be deleted. Dependencies exist.");
    }
}
