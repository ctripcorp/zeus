package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.handler.GroupSync;
import com.ctrip.zeus.service.model.handler.MultiRelMaintainer;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.*;

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
    private RGroupVgDao rGroupVgDao;
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private MultiRelMaintainer groupGsRelMaintainer;
    @Resource
    private MultiRelMaintainer groupVsRelMaintainer;

    @Override
    public void add(Group group) throws Exception {
        group.setVersion(1);
        GroupDo d = C.toGroupDo(0L, group);
        if (d.getAppId() == null) d.setAppId("000000");
        groupDao.insert(d);

        group.setId(d.getId());
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setVersion(group.getVersion())
                .setContent(ContentWriters.writeGroupContent(group))
                .setHash(VersionUtils.getHash(group.getId(), group.getVersion())));

        rGroupStatusDao.insertOrUpdate(new RelGroupStatusDo().setGroupId(group.getId()).setOfflineVersion(group.getVersion()));

        groupVsRelMaintainer.insert(group);
        groupGsRelMaintainer.insert(group);
    }

    @Override
    public void add(Group group, boolean isVirtual) throws Exception {
        add(group);
        if (isVirtual) relSyncVg(group);
    }

    @Override
    public void update(Group group) throws Exception {

        group.setVersion(group.getVersion() + 1);
        GroupDo d = C.toGroupDo(group.getId(), group);
        if (d.getAppId() == null) d.setAppId("000000");

        groupDao.updateById(d, GroupEntity.UPDATESET_FULL);

        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setVersion(group.getVersion())
                .setContent(ContentWriters.writeGroupContent(group))
                .setHash(VersionUtils.getHash(group.getId(), group.getVersion())));
        rGroupStatusDao.insertOrUpdate(new RelGroupStatusDo().setGroupId(group.getId()).setOfflineVersion(group.getVersion()));

        groupVsRelMaintainer.refreshOffline(group);
        groupGsRelMaintainer.refreshOffline(group);
    }

    @Override
    public void updateStatus(List<Group> groups) throws Exception {
        RelGroupStatusDo[] dos = new RelGroupStatusDo[groups.size()];
        for (int i = 0; i < dos.length; i++) {
            dos[i] = new RelGroupStatusDo().setGroupId(groups.get(i).getId()).setOnlineVersion(groups.get(i).getVersion());
        }

        Group[] array = groups.toArray(new Group[groups.size()]);
        groupVsRelMaintainer.refreshOnline(array);
        groupGsRelMaintainer.refreshOnline(array);

        rGroupStatusDao.updateOnlineVersionByGroup(dos, RGroupStatusEntity.UPDATESET_UPDATE_ONLINE_STATUS);
    }

    @Override
    public int delete(Long groupId) throws Exception {
        groupVsRelMaintainer.clear(groupId);
        groupGsRelMaintainer.clear(groupId);
        rGroupVgDao.deleteByGroup(new RelGroupVgDo().setGroupId(groupId));
        rGroupStatusDao.deleteAllByGroup(new RelGroupStatusDo().setGroupId(groupId));
        int count = groupDao.deleteById(new GroupDo().setId(groupId));
        archiveGroupDao.deleteByGroup(new ArchiveGroupDo().setGroupId(groupId));
        return count;
    }

    private void relSyncVg(Group group) throws DalException {
        rGroupVgDao.insert(new RelGroupVgDo().setGroupId(group.getId()));
    }
}