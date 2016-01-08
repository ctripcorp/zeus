package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.service.model.VersionUtils;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("groupGsRelMaintainer")
public class GroupGsRelMaintainer extends MultiRelMaintainerEx<RelGroupGsDo, GroupServer, Group> {
    @Resource
    private RGroupGsDao rGroupGsDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    @Override
    public List<RelGroupGsDo> getAll(Long id) throws Exception {
        return rGroupGsDao.findAllByGroup(id, RGroupGsEntity.READSET_FULL);
    }

    @Override
    public int getTargetVersion(RelGroupGsDo target) throws Exception {
        return target.getGroupVersion();
    }

    @Override
    protected int getOnlineVersion(Long id) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(id, RGroupStatusEntity.READSET_FULL);
        return check == null ? 0 : check.getOnlineVersion();
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
                .setIp(value.getIp())
                .setHash(VersionUtils.getHash(object.getId(), object.getVersion()));
    }

    @Override
    protected void reassign(Group object, RelGroupGsDo output, GroupServer input) throws Exception {
        output.setIp(input.getIp()).setGroupVersion(object.getVersion());
    }

    @Override
    public void deleteRel(Long objectId) throws Exception {
        rGroupGsDao.deleteAllByGroup(new RelGroupGsDo().setGroupId(objectId));
    }

    @Override
    public void batchDeleteRel(Long[] objectIds) throws Exception {
        throw new NotImplementedException();
    }
}
