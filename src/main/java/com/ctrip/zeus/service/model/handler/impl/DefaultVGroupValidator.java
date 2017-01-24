package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.GroupDao;
import com.ctrip.zeus.dal.core.GroupEntity;
import com.ctrip.zeus.dal.core.RGroupVgDao;
import com.ctrip.zeus.dal.core.RGroupVgEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.TrafficPolicy;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.VGroupValidator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

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
    public void validateForActivate(Group[] toBeActivatedItems, boolean escapedPathValidation) throws Exception {

    }

    @Override
    public void validateForDeactivate(Long[] toBeDeactivatedItems) throws Exception {

    }

    @Override
    public void checkVersionForUpdate(Group target) throws Exception {
        groupModelValidator.checkVersionForUpdate(target);
    }

    @Override
    public void removable(Long targetId) throws Exception {
        groupModelValidator.removable(targetId);
    }

    @Override
    public void validateForMerge(Long[] toBeMergedItems, Long vsId, Map<Long, Group> groupRef, Map<Long, TrafficPolicy> policyRef, boolean escapePathValidation) throws Exception {

    }

    @Override
    public void validate(Group target, boolean escapePathValidation) throws Exception {
        if (target.getName() == null || target.getName().isEmpty()) {
            throw new ValidationException("Group name is required.");
        }
        groupModelValidator.validateGroupVirtualServers(target, escapePathValidation);
        for (GroupVirtualServer groupVirtualServer : target.getGroupVirtualServers()) {
            if (groupVirtualServer.getRedirect() == null)
                throw new ValidationException("Redirect value is required.");
        }
    }
}
