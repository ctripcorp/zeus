package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.model.handler.SlbValidator;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhoumy on 2015/6/30.
 */
@Component("slbModelValidator")
public class DefaultSlbValidator implements SlbValidator {
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private SlbDao slbDao;

    @Override
    public boolean exists(Long targetId) throws Exception {
        return slbDao.findById(targetId, SlbEntity.READSET_FULL) != null;
    }

    @Override
    public void validate(Slb slb) throws Exception {
        if (slb == null || slb.getName() == null || slb.getName().isEmpty()) {
            throw new ValidationException("Slb with null value cannot be persisted.");
        }
        if (slb.getSlbServers() == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Slb without slb servers cannot be persisted.");
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
        if (groupCriteriaQuery.queryBySlbId(slbId).size() > 0)
            throw new ValidationException("Slb with id " + slbId + " cannot be deleted. Dependencies exist.");
    }
}
