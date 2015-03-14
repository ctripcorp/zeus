package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.model.ArchiveService;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
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
    public int archiveSlb(String name, String content) throws DalException {
        ArchiveSlbDo max = archiveSlbDao.findMaxVersionByName(name, ArchiveSlbEntity.READSET_FULL);
        int version = max.getVersion() + 1;
        ArchiveSlbDo d = new ArchiveSlbDo().setName(name).setContent(content).setVersion(version).setCreatedTime(new Date()).setLastModified(new Date());
        archiveSlbDao.insert(d);
        return d.getVersion();
    }

    @Override
    public int archiveApp(String name, String content) throws DalException {
        ArchiveAppDo max = archiveAppDao.findMaxVersionByName(name, ArchiveAppEntity.READSET_FULL);
        int version = max.getVersion() + 1;
        ArchiveAppDo d = new ArchiveAppDo().setName(name).setContent(content).setVersion(version).setCreatedTime(new Date()).setLastModified(new Date());
        archiveAppDao.insert(d);
        return d.getVersion();
    }

    @Override
    public String getSlb(String name, int version) throws DalException {
        return archiveSlbDao.findByNameAndVersion(name, version, ArchiveSlbEntity.READSET_FULL).getContent();
    }

    @Override
    public String getApp(String name, int version) throws DalException {
        return archiveAppDao.findByNameAndVersion(name, version, ArchiveAppEntity.READSET_FULL).getContent();
    }

    @Override
    public ArchiveSlbDo getMaxVersionSlb(String name) throws DalException {
        return archiveSlbDao.findMaxVersionByName(name, ArchiveSlbEntity.READSET_FULL);
    }

    @Override
    public ArchiveAppDo getMaxVersionApp(String name) throws DalException {
        return archiveAppDao.findMaxVersionByName(name, ArchiveAppEntity.READSET_FULL);
    }

    @Override
    public List<ArchiveSlbDo> getAllSlb(String name) throws DalException {
        return archiveSlbDao.findAllByName(name, ArchiveSlbEntity.READSET_FULL);
    }

    @Override
    public List<ArchiveAppDo> getAllApp(String name) throws DalException {
        return archiveAppDao.findAllByName(name, ArchiveAppEntity.READSET_FULL);
    }

}
