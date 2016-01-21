package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.ArchiveService;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    @Resource
    private ArchiveVsDao archiveVsDao;

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
    public VirtualServer getVirtualServer(Long vsId, int version) throws Exception {
        MetaVsArchiveDo d = archiveVsDao.findByVsAndVersion(vsId, version, ArchiveVsEntity.READSET_FULL);
        return d == null ? null : DefaultSaxParser.parseEntity(VirtualServer.class, d.getContent());
    }


    @Override
    public List<Group> listGroups(IdVersion[] keys) throws Exception {
        List<Group> result = new ArrayList<>();
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }
        for (ArchiveGroupDo d : archiveGroupDao.findAllByIdVersion(hashes, values, ArchiveGroupEntity.READSET_FULL)) {
            Group group = DefaultSaxParser.parseEntity(Group.class, d.getContent());
            result.add(group);
        }
        return result;
    }

    @Override
    public List<VirtualServer> listVirtualServers(IdVersion[] keys) throws Exception {
        List<VirtualServer> result = new ArrayList<>();
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }
        for (MetaVsArchiveDo d : archiveVsDao.findAllByIdVersion(hashes, values, ArchiveVsEntity.READSET_FULL)) {
            VirtualServer vs = DefaultSaxParser.parseEntity(VirtualServer.class, d.getContent());
            result.add(vs);
        }
        return result;
    }

    @Override
    public List<Slb> listSlbs(IdVersion[] keys) throws Exception {
        List<Slb> result = new ArrayList<>();
        Integer[] hashes = new Integer[keys.length];
        String[] values = new String[keys.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = keys[i].hashCode();
            values[i] = keys[i].toString();
        }
        for (ArchiveSlbDo d : archiveSlbDao.findAllByIdVersion(hashes, values, ArchiveSlbEntity.READSET_FULL)) {
            Slb slb = DefaultSaxParser.parseEntity(Slb.class, d.getContent());
            result.add(slb);
        }
        return result;
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

    @Override
    public Archive getVsArchive(Long vsId, int version) throws Exception {
        MetaVsArchiveDo d = archiveVsDao.findByVsAndVersion(vsId, version, ArchiveVsEntity.READSET_FULL);
        return C.toVsArchive(d);
    }
}