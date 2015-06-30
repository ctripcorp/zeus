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
    private GroupSlbDao groupSlbDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    
    @Override
    public void validate(Slb slb) throws ValidationException {
        if (slb == null || slb.getName() == null || slb.getName().isEmpty()) {
            throw new ValidationException("Slb with null value cannot be persisted.");
        }
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Slb with invalid server data cannot be persisted.");
        }
        Set<String> existingHost = new HashSet<>();
        for (VirtualServer virtualServer : slb.getVirtualServers()) {
            for (Domain domain : virtualServer.getDomains()) {
                String key = domain.getName() + ":" + virtualServer.getPort();
                if (existingHost.contains(key))
                    throw new ValidationException("Duplicate domain and port is found: " + key);
                else
                    existingHost.add(key);
            }
        }
    }

    @Override
    public boolean removable(Slb slb) throws Exception {
        return groupSlbDao.findAllBySlb(slb.getId(), GroupSlbEntity.READSET_FULL).size() == 0;
    }

    @Override
    public boolean modifiable(Slb slb) throws Exception {
        List<SlbVirtualServerDo> l = slbVirtualServerDao.findAllBySlb(slb.getId(), SlbVirtualServerEntity.READSET_FULL);
        Set<Long> deleted = new HashSet<>();
        for (SlbVirtualServerDo d : l) {
            deleted.add(d.getId());
        }
        for (VirtualServer vs : slb.getVirtualServers()) {
            deleted.remove(vs.getId());
        }
        for (Long d : deleted) {
            if (groupSlbDao.findAllByVirtualServer(d, GroupSlbEntity.READSET_FULL).size() > 0)
                return false;
        }
        return true;
    }
}
