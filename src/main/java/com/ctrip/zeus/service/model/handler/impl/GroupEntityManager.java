package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/9/23.
 */
@Component("groupEntityManager")
public class GroupEntityManager implements GroupSync {
    @Resource
    private GroupDao groupDao;
    @Resource
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private RGroupVsDao rGroupVsDao;

    @Override
    public void add(Group group) throws Exception {
        group.setVersion(1);
        GroupDo d = C.toGroupDo(0L, group);
        groupDao.insert(d);
        group.setId(d.getId());
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setVersion(group.getVersion()).setContent(ContentWriters.writeGroupContent(group)));
        relSyncVs(group, true);
    }

    @Override
    public void update(Group group) throws Exception {
        GroupDo check = groupDao.findById(group.getId(), GroupEntity.READSET_FULL);
        if (check.getVersion() > group.getVersion())
            throw new ValidationException("Newer Group version is detected.");
        group.setVersion(group.getVersion() + 1);

        GroupDo d = C.toGroupDo(group.getId(), group);
        groupDao.updateById(d, GroupEntity.UPDATESET_FULL);
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setVersion(group.getVersion()).setContent(ContentWriters.writeGroupContent(group)));
        relSyncVs(group, false);
    }

    @Override
    public void updateVersion(Long[] groupIds) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int delete(Long groupId) throws Exception {
        rGroupVsDao.deleteAllByGroup(new RelGroupVsDo().setGroupId(groupId));
        return groupDao.deleteById(new GroupDo().setId(groupId));
    }

    @Override
    public List<Long> port(Group[] groups) throws Exception {
        List<Long> fails = new ArrayList<>();
        for (Group group : groups) {
            try {
                relSyncVs(group, true);
            } catch (Exception ex) {
                fails.add(group.getId());
            }
        }
        return fails;
    }

    @Override
    public void port(Group group) throws Exception {
        relSyncVs(group, false);
    }

    private void relSyncVs(Group group, boolean isnew) throws DalException {
        if (isnew) {
            for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                VirtualServer vs = groupVirtualServer.getVirtualServer();
                rGroupVsDao.insertOrUpdate(new RelGroupVsDo().setGroupId(group.getId()).setVsId(vs.getId()).setPath(groupVirtualServer.getPath()));
            }
            return;
        }
        List<RelGroupVsDo> originVses = rGroupVsDao.findAllVsesByGroup(group.getId(), RGroupVsEntity.READSET_FULL);
        if (originVses.size() == 0) {
            relSyncVs(group, true);
        }
        // most common case
        if (group.getGroupVirtualServers().size() == 1 && originVses.size() == 1) {
            if (!group.getGroupVirtualServers().get(0).getVirtualServer().getId().equals(originVses.get(0).getVsId())) {
                rGroupVsDao.deleteByVsAndGroup(originVses.get(0));
                GroupVirtualServer gvs = group.getGroupVirtualServers().get(0);
                rGroupVsDao.insertOrUpdate(new RelGroupVsDo().setGroupId(group.getId()).setVsId(gvs.getVirtualServer().getId()).setPath(gvs.getPath()));
            }
        } else {
            relUpdateVsMultiple(originVses, group);
        }
    }

    private void relUpdateVsMultiple(List<RelGroupVsDo> originVses, Group group) throws DalException {
        List<GroupVirtualServer> newVses = group.getGroupVirtualServers();
        // O(n)
        long[] originVsIds = new long[originVses.size()];
        long[] newVsIds = new long[newVses.size()];
        for (int i = 0; i < originVsIds.length; i++) {
            originVsIds[i] = originVses.get(i).getVsId();
        }
        for (int i = 0; i < newVsIds.length; i++) {
            newVsIds[i] = newVses.get(i).getVirtualServer().getId();
        }

        List<Long> removing = new ArrayList<>();
        ArraysUniquePicker.pick(originVsIds, newVsIds, removing, new ArrayList<Long>(), null);

        for (Long rId : removing) {
            rGroupVsDao.deleteByVsAndGroup(new RelGroupVsDo().setVsId(rId).setGroupId(group.getId()));
        }
        for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
            VirtualServer vs = groupVirtualServer.getVirtualServer();
            rGroupVsDao.insertOrUpdate(new RelGroupVsDo().setGroupId(group.getId()).setVsId(vs.getId()).setPath(groupVirtualServer.getPath()));
        }
    }
}