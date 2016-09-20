package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.VersionUtils;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/12/22.
 */
@Component("groupVsRelMaintainer")
public class GroupVsRelMaintainer extends AbstractMultiRelMaintainer<RelGroupVsDo, GroupVirtualServer, Group> {
    @Resource
    private RGroupVsDao rGroupVsDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    public GroupVsRelMaintainer() {
        super(RelGroupVsDo.class, Group.class);
    }

    @Override
    public void updateByPrimaryKey(RelGroupVsDo[] values) throws Exception {
        rGroupVsDao.update(values, RGroupVsEntity.UPDATESET_FULL);
    }

    @Override
    protected IdVersion getIdxKey(RelGroupVsDo rel) throws Exception {
        return new IdVersion(rel.getGroupId(), rel.getGroupVersion());
    }

    @Override
    protected void setDo(Group object, GroupVirtualServer value, RelGroupVsDo target) {
        target.setGroupId(object.getId())
                .setVsId(value.getVirtualServer().getId())
                .setPath(value.getPath())
                .setPriority(value.getPriority())
                .setGroupVersion(object.getVersion());
    }

    @Override
    protected List<RelGroupVsDo> getRelsByObjectId(Group object) throws Exception {
        return rGroupVsDao.findAllByGroup(object.getId(), RGroupVsEntity.READSET_FULL);
    }

    @Override
    protected List<RelGroupVsDo> getRelsByObjectId(Long[] objectIds) throws Exception {
        return rGroupVsDao.findAllByGroups(objectIds, RGroupVsEntity.READSET_FULL);
    }

    @Override
    protected Integer[] getStatusByObjectId(Group object) throws Exception {
        RelGroupStatusDo d = rGroupStatusDao.findByGroup(object.getId(), RGroupStatusEntity.READSET_FULL);
        return new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()};
    }

    @Override
    protected Map<Long, Integer[]> getStatusByObjectId(Long[] objectIds) throws Exception {
        Map<Long, Integer[]> result = new HashMap<>();
        for (RelGroupStatusDo d : rGroupStatusDao.findByGroups(objectIds, RGroupStatusEntity.READSET_FULL)) {
            result.put(d.getGroupId(), new Integer[]{d.getOfflineVersion(), d.getOnlineVersion()});
        }
        return result;
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
    public void clear(Long objectId) throws Exception {
        rGroupVsDao.deleteAllByGroup(new RelGroupVsDo().setGroupId(objectId));
    }

    @Override
    public List<GroupVirtualServer> get(Group object) throws Exception {
        return object.getGroupVirtualServers();
    }
}