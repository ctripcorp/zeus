package com.ctrip.zeus.service.file.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.page.entity.DefaultFile;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.file.FilesInDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2017/3/2.
 */
@Service("filesInDbService")
public class FilesInDbServiceImpl implements FilesInDbService {

    @Resource
    DefaultPageFileDao defaultPageFileDao;
    @Resource
    DefaultPageActiveDao defaultPageActiveDao;
    @Resource
    ConfigHandler configHandler;
    @Resource
    DefaultPageServerActiveDao defaultPageServerActiveDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void addFile(String name, byte[] file) throws Exception {
        DefaultPageFileDo indexPageFileDo = new DefaultPageFileDo();
        indexPageFileDo.setKey(name).setFileData(file);
        DefaultPageFileDo max = defaultPageFileDao.findMaxVersionByKey(name, DefaultPageFileEntity.READSET_FULL);
        if (max == null) {
            indexPageFileDo.setVersion(1);
        } else {
            indexPageFileDo.setVersion(max.getVersion() + 1);
        }
        defaultPageFileDao.insert(indexPageFileDo);
        logger.info("Update file success. File: " + name + " Version:" + indexPageFileDo.getVersion());
    }

    @Override
    public byte[] getFile(String name, Long version) throws Exception {
        DefaultPageFileDo indexPageActiveDo;
        if (version != null && version > 0) {
            indexPageActiveDo = defaultPageFileDao.findByKeyAndVersion(name, version, DefaultPageFileEntity.READSET_FULL);
        } else {
            return null;
        }
        if (indexPageActiveDo == null) {
            throw new ValidationException("Not Found any file version. File:" + name + "version:" + version);
        } else {
            return indexPageActiveDo.getFileData();
        }
    }

    @Override
    public DefaultFile getCurrentFile(String name, Long slbId) throws Exception {
        DefaultPageActiveDo indexPageActiveDo = defaultPageActiveDao.findByKeyAndSlbIdAndType(name, slbId, name, DefaultPageActiveEntity.READSET_FULL);
        if (indexPageActiveDo == null) {
            return null;
        } else {
            DefaultFile res = new DefaultFile().setName(name).setVersion(indexPageActiveDo.getVersion());
            DefaultPageFileDo page = defaultPageFileDao.findByKeyAndVersion(name, indexPageActiveDo.getVersion(), DefaultPageFileEntity.READSET_FULL);
            return res.setFile(new String(page.getFileData()));
        }
    }

    @Override
    public DefaultFile getCurrentFile(String name, String ip) throws Exception {
        DefaultPageServerActiveDo indexPageActiveDo = defaultPageServerActiveDao.findByServerIpAndKey(name, ip, DefaultPageServerActiveEntity.READSET_FULL);
        if (indexPageActiveDo == null) {
            return null;
        } else {
            DefaultFile res = new DefaultFile().setName(name).setVersion(indexPageActiveDo.getVersion());
            DefaultPageFileDo page = defaultPageFileDao.findByKeyAndVersion(name, indexPageActiveDo.getVersion(), DefaultPageFileEntity.READSET_FULL);
            return res.setFile(new String(page.getFileData()));
        }
    }

    @Override
    public void updateFileStatus(String name, String ip, Long version) throws Exception {
        DefaultPageServerActiveDo indexPageServerActiveDo = new DefaultPageServerActiveDo();
        indexPageServerActiveDo.setKey(name).setVersion(version).setServerIp(ip);
        defaultPageServerActiveDao.insert(indexPageServerActiveDo);
        logger.info("File-ip-version status updated success. ServerIp:" + ip + " File:" + name + " version:" + version);
    }

    @Override
    public void updateFileStatus(String name, Long slbId, Long version) throws Exception {
        DefaultPageActiveDo indexPageActiveDo = new DefaultPageActiveDo();
        indexPageActiveDo.setKey(name).setVersion(version).setSlbId(slbId).setType(name);
        defaultPageActiveDao.insert(indexPageActiveDo);
        logger.info("File-slb-version status updated success. slbId:" + slbId + " File:" + name + " version:" + version);
    }

    @Override
    public Long getMaxIndexPageVersion(String name) throws Exception {
        DefaultPageFileDo indexPageActiveDo = defaultPageFileDao.findMaxVersionByKey(name, DefaultPageFileEntity.READSET_FULL);
        if (indexPageActiveDo == null) {
            throw new ValidationException("Not Found any file version. File:" + name);
        } else {
            return indexPageActiveDo.getVersion();
        }
    }
}
