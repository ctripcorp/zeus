package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
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
@Component("groupGsRelMaintainer")
public class GroupGsRelMaintainer extends MultiRelMaintainerEx<RelGroupGsDo, GroupServer, Group> {
    @Resource
    private RGroupGsDao rGroupGsDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;

    public GroupGsRelMaintainer() {
        super(RelGroupGsDo.class, Group.class);
    }

    @Override
    public List<RelGroupGsDo> getAll(Long id) throws Exception {
        return rGroupGsDao.findAllByGroup(id, RGroupGsEntity.READSET_FULL);
    }

    @Override
    public void updateByPrimaryKey(RelGroupGsDo[] values) throws Exception {
        rGroupGsDao.update(values, RGroupGsEntity.UPDATESET_FULL);
    }

    @Override
    protected IdVersion getIdxKey(RelGroupGsDo rel) throws Exception {
        return new IdVersion(rel.getGroupId(), rel.getGroupVersion());
    }

    @Override
    protected void setDo(Group object, GroupServer value, RelGroupGsDo target) {
        target.setGroupId(object.getId())
                .setGroupVersion(object.getVersion())
                .setIp(value.getIp());
    }

    @Override
    protected List<RelGroupGsDo> getRelsByObjectId(Group object) throws Exception {
        return rGroupGsDao.findAllByGroup(object.getId(), RGroupGsEntity.READSET_FULL);
    }

    @Override
    protected List<RelGroupGsDo> getRelsByObjectId(Long[] objectIds) throws Exception {
        return rGroupGsDao.findAllByGroups(objectIds, RGroupGsEntity.READSET_FULL);
    }

    @Override
    protected Integer[] getStatusByObjectId(Group object) throws Exception {
        RelGroupStatusDo d = rGroupStatusDao.findByGroup(object.getId(), RGroupStatusEntity.READSET_FULL);
        return new Integer[]{d.getOfflineVersion(), d.getOfflineVersion()};
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
    public void insert(RelGroupGsDo[] values) throws Exception {
        rGroupGsDao.insert(values);
    }

    @Override
    public void deleteByPrimaryKey(RelGroupGsDo[] values) throws Exception {
        rGroupGsDao.delete(values);
    }

    @Override
    public void deleteRel(Long objectId) throws Exception {
        rGroupGsDao.deleteAllByGroup(new RelGroupGsDo().setGroupId(objectId));
    }

    @Override
    public void batchDeleteRel(Long[] objectIds) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public List<GroupServer> getRelations(Group object) throws Exception {
        return object.getGroupServers();
    }
}
