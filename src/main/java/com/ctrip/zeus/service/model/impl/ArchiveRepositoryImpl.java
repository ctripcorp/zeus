package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.ArchiveRepository;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.support.GenericSerializer;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2016/5/17.
 */
@Repository("archiveRepository")
public class ArchiveRepositoryImpl implements ArchiveRepository {
    @Resource
    private GroupHistoryDao groupHistoryDao;
    @Resource
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private SlbDao slbDao;
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;
    @Resource
    private ArchiveVsDao archiveVsDao;

    @Override
    public void archiveGroup(Group group) throws Exception {
        groupHistoryDao.insert(new GroupHistoryDo().setGroupId(group.getId()).setGroupName(group.getName()));
        archiveGroupDao.insert(new ArchiveGroupDo().setGroupId(group.getId()).setHash(0).setVersion(0)
                .setContent(GenericSerializer.writeJson(group, false)));
    }

    @Override
    public void archiveSlb(Slb slb) throws Exception {
        archiveSlbDao.insert(new ArchiveSlbDo().setSlbId(slb.getId()).setHash(0).setVersion(0)
                .setContent(GenericSerializer.writeJson(slb, false)));
    }

    @Override
    public void archiveVs(VirtualServer vs) throws Exception {
        archiveVsDao.insert(new MetaVsArchiveDo().setVsId(vs.getId()).setHash(0).setVersion(0)
                .setContent(GenericSerializer.writeJson(vs, false)));
    }

    @Override
    public Group getGroupArchive(Long id) throws Exception {
        GroupHistoryDo d = groupHistoryDao.findById(id, GroupHistoryEntity.READSET_FULL);
        if (d == null) return null;

        ArchiveGroupDo archiveGroupDo = archiveGroupDao.findByGroupAndVersion(id, 0, ArchiveGroupEntity.READSET_FULL);
        return archiveGroupDo == null ? null : ContentReaders.readGroupContent(archiveGroupDo.getContent());
    }

    @Override
    public Group getGroupArchive(String name) throws Exception {
        GroupHistoryDo d = groupHistoryDao.findByName(name, GroupHistoryEntity.READSET_FULL);
        if (d == null) return null;

        ArchiveGroupDo archiveGroupDo = archiveGroupDao.findByGroupAndVersion(d.getGroupId(), 0, ArchiveGroupEntity.READSET_FULL);
        return archiveGroupDo == null ? null : ContentReaders.readGroupContent(archiveGroupDo.getContent());
    }

    @Override
    public Slb getSlbArchive(Long id) throws Exception {
        SlbDo d = slbDao.findById(id, SlbEntity.READSET_FULL);
        if (d != null) return null;

        ArchiveSlbDo archiveSlbDo = archiveSlbDao.findBySlbAndVersion(id, 0, ArchiveSlbEntity.READSET_FULL);
        return archiveSlbDo == null ? null : ContentReaders.readSlbContent(archiveSlbDo.getContent());
    }

    @Override
    public VirtualServer getVsArchive(Long id) throws Exception {
        List<SlbVirtualServerDo> d = slbVirtualServerDao.findAllByIds(new Long[]{id}, SlbVirtualServerEntity.READSET_FULL);
        if (d != null) return null;

        MetaVsArchiveDo archiveVsDo = archiveVsDao.findByVsAndVersion(id, 0, ArchiveVsEntity.READSET_FULL);
        return archiveVsDo == null ? null : ContentReaders.readVirtualServerContent(archiveVsDo.getContent());
    }
}
