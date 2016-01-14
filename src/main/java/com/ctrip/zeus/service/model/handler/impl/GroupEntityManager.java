package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
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
    @Resource
    private RGroupGsDao rGroupGsDao;
    @Resource
    private RGroupVgDao rGroupVgDao;

    @Override
    public void add(Group group) throws Exception {
        group.setVersion(1);
        GroupDo d = C.toGroupDo(0L, group);
        if (d.getAppId() == null)
            // if app id is null, it must be virtual group
            d.setAppId("VirtualGroup");
        groupDao.insert(d);
        group.setId(d.getId());
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setVersion(group.getVersion()).setContent(ContentWriters.writeGroupContent(group)));
        relSyncVs(group, true);
        relSyncGs(group, true);
    }

    @Override
    public void add(Group group, boolean isVirtual) throws Exception {
        add(group);
        if (isVirtual)
            relSyncVg(group);
    }

    @Override
    public void update(Group group) throws Exception {
        GroupDo check = groupDao.findById(group.getId(), GroupEntity.READSET_FULL);
        if (check.getVersion() > group.getVersion())
            throw new ValidationException("Newer Group version is detected.");
        group.setVersion(group.getVersion() + 1);

        GroupDo d = C.toGroupDo(group.getId(), group).setAppId("VirtualGroup");
        groupDao.updateById(d, GroupEntity.UPDATESET_FULL);
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setVersion(group.getVersion()).setContent(ContentWriters.writeGroupContent(group)));
        relSyncVs(group, false);
        relSyncGs(group, false);
    }

    @Override
    public void updateVersion(Long[] groupIds) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int delete(Long groupId) throws Exception {
        rGroupVsDao.deleteAllByGroup(new RelGroupVsDo().setGroupId(groupId));
        rGroupGsDao.deleteAllByGroup(new RelGroupGsDo().setGroupId(groupId));
        rGroupVgDao.deleteByPK(new RelGroupVgDo().setGroupId(groupId));
        int count = groupDao.deleteById(new GroupDo().setId(groupId));
        archiveGroupDao.deleteByGroup(new ArchiveGroupDo().setGroupId(groupId));
        return count;
    }

    @Override
    public List<Long> port(Group[] groups) throws Exception {
        List<Long> fails = new ArrayList<>();
        for (Group group : groups) {
            try {
                relSyncGs(group, false);
                relSyncVs(group, false);
            } catch (Exception ex) {
                fails.add(group.getId());
            }
        }
        return fails;
    }

    @Override
    public void port(Group group) throws Exception {
        relSyncGs(group, false);
        relSyncVs(group, false);
    }

    private void relSyncGs(Group group, boolean isnew) throws DalException {
        if (isnew) {
            RelGroupGsDo[] dos = new RelGroupGsDo[group.getGroupServers().size()];
            for (int i = 0; i < dos.length; i++) {
                dos[i] = new RelGroupGsDo().setGroupId(group.getId()).setIp(group.getGroupServers().get(i).getIp());
            }
            rGroupGsDao.insert(dos);
            return;
        }
        List<RelGroupGsDo> originGses = rGroupGsDao.findAllByGroup(group.getId(), RGroupGsEntity.READSET_FULL);
        if (originGses.size() == 0) {
            relSyncGs(group, true);
            return;
        }
        List<GroupServer> newGses = group.getGroupServers();
        String[] originGsIps = new String[originGses.size()];
        String[] newGsIps = new String[newGses.size()];
        for (int i = 0; i < originGsIps.length; i++) {
            originGsIps[i] = originGses.get(i).getIp();
        }
        for (int i = 0; i < newGsIps.length; i++) {
            newGsIps[i] = newGses.get(i).getIp();
        }
        List<String> removing = new ArrayList<>();
        List<String> adding = new ArrayList<>();
        ArraysUniquePicker.pick(originGsIps, newGsIps, removing, adding);

        RelGroupGsDo[] dos = new RelGroupGsDo[removing.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelGroupGsDo().setGroupId(group.getId()).setIp(removing.get(i));
        }
        rGroupGsDao.deleteByGroupAndIp(dos);

        dos = new RelGroupGsDo[adding.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelGroupGsDo().setGroupId(group.getId()).setIp(adding.get(i));
        }
        rGroupGsDao.insert(dos);
    }

    private void relSyncVg(Group group) throws DalException {
        rGroupVgDao.insert(new RelGroupVgDo().setGroupId(group.getId()));
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
            } else {
                GroupVirtualServer gvs = group.getGroupVirtualServers().get(0);
                RelGroupVsDo d = rGroupVsDao.findByGroupAndVs(group.getId(), gvs.getVirtualServer().getId(), RGroupVsEntity.READSET_FULL);
                d.setPath(gvs.getPath());
                rGroupVsDao.updateByPK(d, RGroupVsEntity.UPDATESET_FULL);
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
        ArraysUniquePicker.pick(originVsIds, newVsIds, removing, new ArrayList<Long>());

        for (Long rId : removing) {
            rGroupVsDao.deleteByVsAndGroup(new RelGroupVsDo().setVsId(rId).setGroupId(group.getId()));
        }
        for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
            VirtualServer vs = groupVirtualServer.getVirtualServer();
            rGroupVsDao.insertOrUpdate(new RelGroupVsDo().setGroupId(group.getId()).setVsId(vs.getId()).setPath(groupVirtualServer.getPath()));
        }
    }
}