package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.handler.ArchiveService;
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
    private ArchiveAppDao archiveAppDao;

    @Override
    public int archiveSlb(Slb slb) throws DalException {
        String content = String.format(Slb.XML, slb);
        ArchiveSlbDo d = new ArchiveSlbDo().setName(slb.getName()).setContent(content).setVersion(slb.getVersion()).setCreatedTime(new Date()).setLastModified(new Date());
        archiveSlbDao.insert(d);
        return d.getVersion();
    }

    @Override
    public int archiveApp(App app) throws DalException {
        String content = String.format(App.XML, app);
        ArchiveAppDo d = new ArchiveAppDo().setName(app.getName()).setContent(content).setVersion(app.getVersion()).setCreatedTime(new Date()).setLastModified(new Date());
        archiveAppDao.insert(d);
        return d.getVersion();
    }

    @Override
    public int deleteSlbArchive(String slbName) throws DalException {
        ArchiveSlbDo d = new ArchiveSlbDo().setName(slbName);
        return archiveSlbDao.deleteBySlb(d);
    }

    @Override
    public int deleteAppArchive(String appName) throws DalException {
        ArchiveAppDo d = new ArchiveAppDo().setName(appName);
        return archiveAppDao.deleteByApp(d);
    }

    @Override
    public Slb getSlb(String name, int version) throws DalException, IOException, SAXException {
        String content = archiveSlbDao.findByNameAndVersion(name, version, ArchiveSlbEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(Slb.class, content);
    }

    @Override
    public App getApp(String name, int version) throws DalException, IOException, SAXException {
        String content =  archiveAppDao.findByNameAndVersion(name, version, ArchiveAppEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(App.class, content);
    }

    @Override
    public Slb getMaxVersionSlb(String name) throws DalException, IOException, SAXException {
        String content =  archiveSlbDao.findMaxVersionByName(name, ArchiveSlbEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(Slb.class, content);
    }

    @Override
    public App getMaxVersionApp(String name) throws DalException, IOException, SAXException {
        String content = archiveAppDao.findMaxVersionByName(name, ArchiveAppEntity.READSET_FULL).getContent();
        return DefaultSaxParser.parseEntity(App.class, content);
    }

    @Override
    public List<Slb> getAllSlb(String name) throws DalException, IOException, SAXException {
        List<ArchiveSlbDo> l =  archiveSlbDao.findAllByName(name, ArchiveSlbEntity.READSET_FULL);
        List<Slb> list = new ArrayList<>();
        for (ArchiveSlbDo d : l) {
            list.add(DefaultSaxParser.parseEntity(Slb.class, d.getContent()));
        }
        return list;
    }

    @Override
    public List<App> getAllApp(String name) throws DalException, IOException, SAXException {
        List<ArchiveAppDo> l = archiveAppDao.findAllByName(name, ArchiveAppEntity.READSET_FULL);
        List<App> list = new ArrayList<>();
        for (ArchiveAppDo d : l) {
            list.add(DefaultSaxParser.parseEntity(App.class, d.getContent()));
        }
        return list;
    }

}
