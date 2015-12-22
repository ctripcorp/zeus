package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("groupGsRelMaintainer")
public class GroupGsRelMaintainer extends AbstractMultiRelMaintainer<RelGroupGsDo, GroupServer, Group> {
    @Resource
    private RGroupGsDao rGroupGsDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    @Override
    public List<RelGroupGsDo> getAll(Long id) throws Exception {
        return rGroupGsDao.findAllByGroup(id, RGroupGsEntity.READSET_FULL);
    }

    @Override
    public int getCurrentVersion(Group object) {
        return object.getVersion();
    }

    @Override
    public int getTargetVersion(RelGroupGsDo target) throws Exception {
        return target.getGroupVersion();
    }

    @Override
    public void updateByPrimaryKey(RelGroupGsDo[] values) throws Exception {
        rGroupGsDao.update(values, RGroupGsEntity.UPDATESET_FULL);
    }

    @Override
    public void insert(RelGroupGsDo[] values) throws Exception {
        rGroupGsDao.insert(values);
    }

    @Override
    public void deleteByPrimaryKey(RelGroupGsDo[] values) throws Exception {
        rGroupGsDao.delete(values);
    }

    @Override
    public Long getTargetId(Group object) throws Exception {
        return object.getId();
    }

    @Override
    public RelGroupGsDo getDo(Group object, GroupServer value) throws Exception {
        return new RelGroupGsDo().setGroupId(object.getId())
                .setGroupVersion(object.getVersion())
                .setIp(value.getIp());
    }

    @Override
    protected void reassign(Group object, RelGroupGsDo output, GroupServer input) throws Exception {
        output.setIp(input.getIp()).setGroupVersion(object.getVersion());
    }

    @Override
    public boolean currentRetained(Long id) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(id, RGroupStatusEntity.READSET_FULL);
        return check == null ? false : check.getOfflineVersion() == check.getOnlineVersion();
    }

    @Override
    public void relDelete(Long objectId) throws Exception {
        rGroupGsDao.deleteAllByGroup(new RelGroupGsDo().setGroupId(objectId));
    }
}
