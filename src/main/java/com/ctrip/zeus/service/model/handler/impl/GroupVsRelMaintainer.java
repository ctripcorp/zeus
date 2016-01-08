package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.model.VersionUtils;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("groupVsRelMaintainer")
public class GroupVsRelMaintainer extends MultiRelMaintainerEx<RelGroupVsDo, GroupVirtualServer, Group> {
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    @Override
    public List<RelGroupVsDo> getAll(Long id) throws Exception {
        return rGroupVsDao.findAllVsesByGroup(id, RGroupVsEntity.READSET_FULL);
    }

    @Override
    public int getTargetVersion(RelGroupVsDo target) throws Exception {
        return target.getGroupVersion();
    }

    @Override
    protected int getOnlineVersion(Long id) throws Exception {
        RelGroupStatusDo check = rGroupStatusDao.findByGroup(id, RGroupStatusEntity.READSET_FULL);
        return check == null ? 0 : check.getOnlineVersion();
    }

    @Override
    public void updateByPrimaryKey(RelGroupVsDo[] values) throws Exception {
        rGroupVsDao.update(values, RGroupVsEntity.UPDATESET_FULL);
    }

    @Override
    public void insert(RelGroupVsDo[] values) throws Exception {
        rGroupVsDao.insert(values);
    }

    @Override
    public void deleteByPrimaryKey(RelGroupVsDo[] values) throws Exception {
        rGroupVsDao.delete(values);
    }

    @Override
    public Long getTargetId(Group object) throws Exception {
        return object.getId();
    }

    @Override
    public RelGroupVsDo getDo(Group object, GroupVirtualServer value) throws Exception {
        return new RelGroupVsDo().setGroupId(object.getId())
                .setVsId(value.getVirtualServer().getId())
                .setPath(value.getPath())
                .setGroupVersion(object.getVersion())
                .setHash(VersionUtils.getHash(object.getId(), object.getVersion()));
    }

    @Override
    protected void reassign(Group object, RelGroupVsDo output, GroupVirtualServer input) throws Exception {
        output.setVsId(input.getVirtualServer().getId())
                .setPath(input.getPath())
                .setGroupVersion(object.getVersion());
    }

    @Override
    public void deleteRel(Long objectId) throws Exception {
        rGroupVsDao.deleteAllByGroup(new RelGroupVsDo().setGroupId(objectId));
    }

    @Override
    public void batchDeleteRel(Long[] objectIds) throws Exception {
        throw new NotImplementedException();
    }
}