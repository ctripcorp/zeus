package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("groupModelValidator")
public class DefaultGroupValidator implements GroupValidator {
    @Resource
    private RTrafficPolicyGroupDao rTrafficPolicyGroupDao;
    @Resource
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private GroupDao groupDao;

    @Override
    public boolean exists(Long targetId) throws Exception {
        return exists(targetId, false);
    }

    @Override
    public boolean exists(Long targetId, boolean virtual) throws Exception {
        boolean result = groupDao.findById(targetId, GroupEntity.READSET_FULL) != null;
        RelGroupVgDo value = rGroupVgDao.findByGroup(targetId, RGroupVgEntity.READSET_FULL);
        result &= (virtual ^ value == null);
        return result;
    }

    @Override
    public void validate(Group target) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public void checkVersionForUpdate(Group target) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(target.getId(), RGroupStatusEntity.READSET_FULL);
        if (check == null) {
            throw new ValidationException("Group that you try to update does not exist.");
        }
        if (check.getOfflineVersion() > target.getVersion()) {
            throw new ValidationException("Newer version is detected.");
        }
        if (check.getOfflineVersion() != target.getVersion()) {
            throw new ValidationException("Incompatible version.");
        }
    }

    @Override
    public void removable(Long targetId) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(targetId, RGroupStatusEntity.READSET_FULL);
        if (check == null) return;

        if (check.getOnlineVersion() != 0) {
            throw new ValidationException("Group that you tried to delete is still active.");
        }
        if (rTrafficPolicyGroupDao.findByGroup(targetId, RTrafficPolicyGroupEntity.READSET_FULL).size() > 0) {
            throw new ValidationException("Group that you tried to delete has one or more traffic policy dependency.");
        }
    }
}