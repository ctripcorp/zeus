package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.ArchiveService;

import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
@Component("archiveService")
public class ArchiveServiceImpl implements ArchiveService {
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private ArchiveGroupDao archiveGroupDao;

    @Override
    public int archiveSlb(Slb slb) throws Exception {
        String content = String.format(Slb.XML, slb);
        ArchiveSlbDo d = new ArchiveSlbDo().setSlbId(slb.getId()).setContent(content).setVersion(slb.getVersion()).setCreatedTime(new Date()).setDataChangeLastTime(new Date());
        archiveSlbDao.insert(d);
        return d.getVersion();
    }

    @Override
    public int archiveGroup(Group group) throws Exception {
        String content = String.format(Group.XML, group);
        ArchiveGroupDo d = new ArchiveGroupDo().setGroupId(group.getId()).setContent(content).setVersion(group.getVersion()).setCreatedTime(new Date()).setDataChangeLastTime(new Date());
        archiveGroupDao.insert(d);
        return d.getVersion();
    }

    @Override
    public int deleteSlbArchive(Long slbId) throws Exception {
        ArchiveSlbDo d = new ArchiveSlbDo().setSlbId(slbId);
        return archiveSlbDao.deleteBySlb(d);
    }

    @Override
    public int deleteGroupArchive(Long groupId) throws Exception {
        ArchiveGroupDo d = new ArchiveGroupDo().setGroupId(groupId);
        return archiveGroupDao.deleteByGroup(d);
    }

    @Override
    public Slb getSlb(Long slbId, int version) throws Exception {
        ArchiveSlbDo d = archiveSlbDao.findBySlbAndVersion(slbId, version, ArchiveSlbEntity.READSET_FULL);
        return d == null ? null : DefaultSaxParser.parseEntity(Slb.class, d.getContent());
    }

    @Override
    public Group getGroup(Long groupId, int version) throws Exception {
        ArchiveGroupDo d = archiveGroupDao.findByGroupAndVersion(groupId, version, ArchiveGroupEntity.READSET_FULL);
        return d == null ? null : DefaultSaxParser.parseEntity(Group.class, d.getContent());
    }

    @Override
    public Slb getLatestSlb(Long slbId) throws Exception {
        ArchiveSlbDo d = archiveSlbDao.findMaxVersionBySlb(slbId, ArchiveSlbEntity.READSET_FULL);
        return d == null ? null : DefaultSaxParser.parseEntity(Slb.class, d.getContent());
    }

    @Override
    public Group getLatestGroup(Long groupId) throws Exception {
        ArchiveGroupDo d = archiveGroupDao.findMaxVersionByGroup(groupId, ArchiveGroupEntity.READSET_FULL);
        return d == null ? null : DefaultSaxParser.parseEntity(Group.class, d.getContent());
    }

    @Override
    public List<Slb> getLatestSlbs(Long[] slbIds) throws Exception {
        List<Slb> slbs = new ArrayList<>();
        for (ArchiveSlbDo archiveSlbDo : archiveSlbDao.findMaxVersionBySlbs(slbIds, ArchiveSlbEntity.READSET_FULL)) {
            try {
                Slb slb = DefaultSaxParser.parseEntity(Slb.class, archiveSlbDo.getContent());
                slbs.add(slb);
            } catch (Exception ex) {
                slbs.add(new Slb().setId(archiveSlbDo.getId()));
            }
        }
        return slbs;
    }

    @Override
    public List<Group> getLatestGroups(Long[] groupIds) throws Exception {
        List<Group> groups = new ArrayList<>();
        for (ArchiveGroupDo archiveGroupDo : archiveGroupDao.findMaxVersionByGroups(groupIds, ArchiveGroupEntity.READSET_FULL)) {
            try {
                Group group = DefaultSaxParser.parseEntity(Group.class, archiveGroupDo.getContent());
                groups.add(group);
            } catch (Exception ex) {
                groups.add(new Group().setId(archiveGroupDo.getId()));
            }
        }
        return groups;
    }

    @Override
    public List<Slb> getAllSlb(Long slbId) throws Exception {
        List<ArchiveSlbDo> l = archiveSlbDao.findAllBySlb(slbId, ArchiveSlbEntity.READSET_FULL);
        List<Slb> list = new ArrayList<>();
        for (ArchiveSlbDo d : l) {
            list.add(DefaultSaxParser.parseEntity(Slb.class, d.getContent()));
        }
        return list;
    }

    @Override
    public List<Group> getAllGroup(Long groupId) throws Exception {
        List<ArchiveGroupDo> l = archiveGroupDao.findAllByGroup(groupId, ArchiveGroupEntity.READSET_FULL);
        List<Group> list = new ArrayList<>();
        for (ArchiveGroupDo d : l) {
            list.add(DefaultSaxParser.parseEntity(Group.class, d.getContent()));
        }
        return list;
    }

    @Override
    public Archive getLatestSlbArchive(Long slbId) throws Exception {
        ArchiveSlbDo asd = archiveSlbDao.findMaxVersionBySlb(slbId, ArchiveSlbEntity.READSET_FULL);
        return C.toSlbArchive(asd);
    }

    @Override
    public Archive getLatestGroupArchive(Long groupId) throws Exception {
        ArchiveGroupDo aad = archiveGroupDao.findMaxVersionByGroup(groupId, ArchiveGroupEntity.READSET_FULL);
        return C.toGroupArchive(aad);
    }

    @Override
    public Archive getSlbArchive(Long slbId, int version) throws Exception {
        ArchiveSlbDo archive = archiveSlbDao.findBySlbAndVersion(slbId, version, ArchiveSlbEntity.READSET_FULL);
        return C.toSlbArchive(archive);
    }

    @Override
    public Archive getGroupArchive(Long groupId, int version) throws Exception {
        ArchiveGroupDo archive = archiveGroupDao.findByGroupAndVersion(groupId, version, ArchiveGroupEntity.READSET_FULL);
        return C.toGroupArchive(archive);
    }
}