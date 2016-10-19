package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.ContentWriters;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2016/5/17.
 */
@Repository("archiveRepository")
public class ArchiveRepositoryImpl implements ArchiveRepository {
    @Resource
    private GroupDao groupDao;
    @Resource
    private GroupHistoryDao groupHistoryDao;
    @Resource
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private ArchiveVsDao archiveVsDao;

    @Override
    public void archiveGroup(Group group) throws Exception {
        groupHistoryDao.insert(new GroupHistoryDo().setGroupId(group.getId()).setGroupName(group.getName()));
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setHash(0).setVersion(0)
                .setContent(ContentWriters.writeGroupContent(group)));
    }

    @Override
    public void archiveSlb(Slb slb) throws Exception {
        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slb.getId()).setHash(0).setVersion(0)
                .setContent(ContentWriters.writeSlbContent(slb)));
    }

    @Override
    public void archiveVs(VirtualServer vs) throws Exception {
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vs.getId()).setHash(0).setVersion(0)
                .setContent(ContentWriters.writeVirtualServerContent(vs)));
    }

    @Override
    public Group getGroupArchive(Long id, int version) throws Exception {
        ArchiveGroupDo archiveGroupDo = archiveGroupDao.findByGroupAndVersion(id, version, ArchiveGroupEntity.READSET_FULL);
        return archiveGroupDo == null ? null : ContentReaders.readGroupContent(archiveGroupDo.getContent());
    }

    @Override
    public Group getGroupArchive(String name, int version) throws Exception {
        Long groupId;
        if (version == 0) {
            GroupHistoryDo d = groupHistoryDao.findByName(name, GroupHistoryEntity.READSET_FULL);
            if (d == null) return null;
            groupId = d.getGroupId();
        } else {
            GroupDo d = groupDao.findByName(name, GroupEntity.READSET_IDONLY);
            if (d == null) return null;
            groupId = d.getId();
        }

        ArchiveGroupDo archiveGroupDo = archiveGroupDao.findByGroupAndVersion(groupId, version, ArchiveGroupEntity.READSET_FULL);
        return archiveGroupDo == null ? null : ContentReaders.readGroupContent(archiveGroupDo.getContent());
    }

    @Override
    public Slb getSlbArchive(Long id, int version) throws Exception {
        ArchiveSlbDo archiveSlbDo = archiveSlbDao.findBySlbAndVersion(id, version, ArchiveSlbEntity.READSET_FULL);
        return archiveSlbDo == null ? null : ContentReaders.readSlbContent(archiveSlbDo.getContent());
    }

    @Override
    public VirtualServer getVsArchive(Long id, int version) throws Exception {
        MetaVsArchiveDo archiveVsDo = archiveVsDao.findByVsAndVersion(id, version, ArchiveVsEntity.READSET_FULL);
        return archiveVsDo == null ? null : ContentReaders.readVirtualServerContent(archiveVsDo.getContent());
    }
}
