package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.GroupDao;
import com.ctrip.zeus.dal.core.GroupEntity;
import com.ctrip.zeus.dal.core.RGroupVgDao;
import com.ctrip.zeus.dal.core.RGroupVgEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VGroupValidator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/11/23.
 */
@Component("vGroupValidator")
public class DefaultVGroupValidator implements VGroupValidator {
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private GroupDao groupDao;
    @Resource
    private GroupValidator groupModelValidator;

    @Override
    public boolean exists(Long targetId) throws Exception {
        return groupDao.findById(targetId, GroupEntity.READSET_FULL) != null
                && rGroupVgDao.findByGroup(targetId, RGroupVgEntity.READSET_FULL) != null;
    }

    @Override
    public void validate(Group target) throws Exception {
        validate(target, false);
    }

    @Override
    public void checkVersion(Group target) throws Exception {
        groupModelValidator.checkVersion(target);
    }

    @Override
    public void removable(Long targetId) throws Exception {
        groupModelValidator.removable(targetId);
    }

    @Override
    public void validate(Group target, boolean escapePathValidation) throws Exception {
        if (target.getName() == null || target.getName().isEmpty()) {
            throw new ValidationException("Group name is required.");
        }
        groupModelValidator.validateGroupVirtualServers(target.getId(), target.getGroupVirtualServers(), escapePathValidation);
        for (GroupVirtualServer groupVirtualServer : target.getGroupVirtualServers()) {
            if (groupVirtualServer.getRedirect() == null)
                throw new ValidationException("Redirect value is required.");
        }
    }
}
