package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Archive;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.IOException;
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
        ArchiveSlbDo d = new ArchiveSlbDo().setName(slb.getName()).setContent(content).setVersion(slb.getVersion()).setCreatedTime(new Date()).setDataChangeLastTime(new Date());
        archiveSlbDao.insert(d);
        return d.getVersion();
    }

    @Override
    public int archiveGroup(Group app) throws Exception {
        String content = String.format(Group.XML, app);
        ArchiveGroupDo d = new ArchiveGroupDo().setName(app.getName()).setContent(content).setVersion(app.getVersion()).setCreatedTime(new Date()).setDataChangeLastTime(new Date());
        archiveGroupDao.insert(d);
        return d.getVersion();
    }

    @Override
    public int deleteSlbArchive(String slbName) throws Exception {
        ArchiveSlbDo d = new ArchiveSlbDo().setName(slbName);
        return archiveSlbDao.deleteBySlb(d);
    }

    @Override
    public int deleteGroupArchive(String appName) throws Exception {
        ArchiveGroupDo d = new ArchiveGroupDo().setName(appName);
        return archiveGroupDao.deleteByGroup(d);
    }

    @Override
    public Slb getSlb(String name, int version) throws Exception {
        String content = archiveSlbDao.findByNameAndVersion(name, version, ArchiveSlbEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(Slb.class, content);
    }

    @Override
    public Group getGroup(String name, int version) throws Exception {
        String content =  archiveGroupDao.findByNameAndVersion(name, version, ArchiveGroupEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(Group.class, content);
    }

    @Override
    public Slb getMaxVersionSlb(String name) throws Exception {
        String content =  archiveSlbDao.findMaxVersionByName(name, ArchiveSlbEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(Slb.class, content);
    }

    @Override
    public Group getMaxVersionGroup(String name) throws Exception {
        String content = archiveGroupDao.findMaxVersionByName(name, ArchiveGroupEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(Group.class, content);
    }

    @Override
    public List<Slb> getAllSlb(String name) throws Exception {
        List<ArchiveSlbDo> l =  archiveSlbDao.findAllByName(name, ArchiveSlbEntity.READSET_FULL);
        List<Slb> list = new ArrayList<>();
        for (ArchiveSlbDo d : l) {
            list.add(DefaultSaxParser.parseEntity(Slb.class, d.getContent()));
        }
        return list;
    }

    @Override
    public List<Group> getAllGroup(String name) throws Exception {
        List<ArchiveGroupDo> l = archiveGroupDao.findAllByName(name, ArchiveGroupEntity.READSET_FULL);
        List<Group> list = new ArrayList<>();
        for (ArchiveGroupDo d : l) {
            list.add(DefaultSaxParser.parseEntity(Group.class, d.getContent()));
        }
        return list;
    }

    @Override
    public Archive getLatestGroupArchive(String appName) throws Exception {
        ArchiveGroupDo aad = archiveGroupDao.findMaxVersionByName(appName, ArchiveGroupEntity.READSET_FULL);
        return C.toGroupArchive(aad);
    }

    @Override
    public Archive getLatestSlbArchive(String slbName) throws Exception {
        ArchiveSlbDo asd = archiveSlbDao.findMaxVersionByName(slbName, ArchiveSlbEntity.READSET_FULL);
        return C.toSlbArchive(asd);
    }

}