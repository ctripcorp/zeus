package com.ctrip.zeus.service.file.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.FileActiveRMapper;
import com.ctrip.zeus.dao.mapper.FileDataMapper;
import com.ctrip.zeus.dao.mapper.FileServerActiveRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.service.file.FilesInDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2017/3/2.
 */
@Service("filesInDbService")
public class FilesInDbServiceImpl implements FilesInDbService {

    @Resource
    private FileDataMapper fileDataMapper;
    @Resource
    private FileActiveRMapper fileActiveRMapper;
    @Resource
    private FileServerActiveRMapper fileServerActiveRMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void addFile(String name, byte[] file) throws Exception {
        FileData newFile = new FileData();
        newFile.setFileData(file);
        newFile.setKey(name);
        List<FileData> fileDataList = fileDataMapper.selectByExample(new FileDataExample().createCriteria().andKeyEqualTo(name).example().orderBy("version desc").limit(1));
        if (fileDataList == null || fileDataList.isEmpty()) {
            newFile.setVersion(1L);
        } else {
            newFile.setVersion(fileDataList.get(0).getVersion() + 1L);
        }
        fileDataMapper.insert(newFile);
        logger.info("[Mybatis]Update file success. File: " + name + " Version:" + newFile.getVersion());
    }

    @Override
    public byte[] getFile(String name, Long version) throws Exception {
        if (version == null || version <= 0) {
            return null;
        }
        List<FileData> fileDataList = fileDataMapper.selectByExampleWithBLOBs(new FileDataExample().createCriteria().andKeyEqualTo(name).andVersionEqualTo(version).example());
        if (fileDataList == null || fileDataList.isEmpty()) {
            throw new ValidationException("[Mybatis]Not Found any file version. File:" + name + "version:" + version);
        } else {
            return fileDataList.get(0).getFileData();
        }
    }

    @Override
    public FileData getCurrentFile(String name, String type, Long slbId) throws Exception {
        if (slbId == null) {
            return null;
        }
        List<FileActiveR> activeInfo = fileActiveRMapper.selectByExample(new FileActiveRExample().createCriteria().andKeyEqualTo(name)
                .andSlbIdEqualTo(slbId).andTypeEqualTo(type).example());
        if (activeInfo == null || activeInfo.isEmpty()) {
            return null;
        } else {
            long version = activeInfo.get(0).getVersion();
            FileData file = fileDataMapper.selectOneByExampleWithBLOBs(new FileDataExample().createCriteria().andKeyEqualTo(name)
                    .andVersionEqualTo(version).example());
            return file;
        }
    }

    @Override
    public List<FileData> getCurrentFiles(String type, Long slbId) throws Exception {
        if (slbId == null) {
            return null;
        }
        List<FileActiveR> activeInfo = fileActiveRMapper.selectByExample(new FileActiveRExample().createCriteria().andTypeEqualTo(type)
                .andSlbIdEqualTo(slbId).example());
        if (activeInfo == null || activeInfo.isEmpty()) {
            return null;
        } else {
            List<FileData> res = new ArrayList<>();
            for (FileActiveR r : activeInfo) {
                FileData file = fileDataMapper.selectOneByExampleWithBLOBs(new FileDataExample().createCriteria().andKeyEqualTo(r.getKey())
                        .andVersionEqualTo(r.getVersion()).example());
                res.add(file);
            }
            return res;
        }
    }


    @Override
    public FileData getCurrentFile(String name, String ip) throws Exception {
        if (name == null || ip == null) {
            return null;
        }
        FileServerActiveR activeInfo = fileServerActiveRMapper.selectOneByExample(new FileServerActiveRExample().createCriteria()
                .andServerIpEqualTo(ip).andKeyEqualTo(name).example());
        if (activeInfo == null) {
            return null;
        } else {
            FileData file = fileDataMapper.selectOneByExampleWithBLOBs(new FileDataExample().createCriteria().andKeyEqualTo(activeInfo.getKey())
                    .andVersionEqualTo(activeInfo.getVersion()).example());
            return file;
        }
    }


    @Override
    public void updateFileStatus(String name, String ip, Long version) throws Exception {
        FileServerActiveR toUpdate = new FileServerActiveR();
        toUpdate.setKey(name);
        toUpdate.setServerIp(ip);
        toUpdate.setVersion(version);
        fileServerActiveRMapper.upsert(toUpdate);
        logger.info("[Mybatis]File-ip-version status updated success. ServerIp:" + ip + " File:" + name + " version:" + version);
    }

    @Override
    public void updateFileStatus(String name, Long slbId, String type, Long version) throws Exception {
        FileActiveR toUpdate = new FileActiveR();
        toUpdate.setKey(name);
        toUpdate.setSlbId(slbId);
        toUpdate.setType(type);
        toUpdate.setVersion(version);
        fileActiveRMapper.upsert(toUpdate);
        logger.info("[Mybatis]File-slb-version status updated success. slbId:" + slbId + " File:" + name + " version:" + version);
    }

    @Override
    public Long getMaxIndexPageVersion(String name) throws Exception {
        if (name == null) {
            return null;
        }
        FileData file = fileDataMapper.selectOneByExampleWithBLOBs(new FileDataExample().createCriteria().andKeyEqualTo(name).example()
                .orderBy("version desc").limit(1));
        if (file == null) {
            throw new ValidationException("[mybatis]Not Found any file version. File:" + name);
        } else {
            return file.getVersion();
        }
    }
}
