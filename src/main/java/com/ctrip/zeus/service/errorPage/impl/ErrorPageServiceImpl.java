package com.ctrip.zeus.service.errorPage.impl;

import com.ctrip.zeus.client.InstallDefaultPageClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.page.entity.DefaultPage;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.errorPage.ErrorPageService;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.startup.PreCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by fanqq on 2016/8/22.
 */
@Service("errorPageService")
public class ErrorPageServiceImpl implements ErrorPageService, PreCheck {

    @Resource
    DefaultPageFileDao defaultPageFileDao;
    @Resource
    DefaultPageActiveDao defaultPageActiveDao;
    @Resource
    ConfigHandler configHandler;
    @Resource
    DefaultPageServerActiveDao defaultPageServerActiveDao;
    @Resource
    SlbRepository slbRepository;
    @Resource
    private EntityFactory entityFactory;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String PAGE_TYPE = "errorPage";

    @Override
    public void updateErrorPageFile(String code, byte[] file) throws Exception {
        DefaultPageFileDo errorPageFileDo = new DefaultPageFileDo();
        errorPageFileDo.setKey(code).setFileData(file);
        DefaultPageFileDo max = defaultPageFileDao.findMaxVersionByKey(code, DefaultPageFileEntity.READSET_FULL);
        if (max == null) {
            errorPageFileDo.setVersion(1);
        } else {
            errorPageFileDo.setVersion(max.getVersion() + 1);
        }
        defaultPageFileDao.insert(errorPageFileDo);
        logger.info("Update Error page file success. Code: " + code + " Version:" + errorPageFileDo.getVersion());
    }

    @Override
    public void installErrorPage(Long slbId, String code, Long version) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }
        byte[] data = getErrorPage(code, version);
        if (data == null) {
            throw new ValidationException("Not Found page file by slbId. id:" + slbId + "and version:" + version);
        }

        for (SlbServer slbServer : slb.getSlbServers()) {
            InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).errorPage(code, version);
        }

        DefaultPageActiveDo errorPageActiveDo = new DefaultPageActiveDo();
        errorPageActiveDo.setKey(code).setVersion(version).setSlbId(slbId).setType(PAGE_TYPE);
        defaultPageActiveDao.insert(errorPageActiveDo);
    }

    @Override
    public void installLocalErrorPage(String code, Long version) throws Exception {
        DefaultPageFileDo errorPageFileDo = defaultPageFileDao.findByKeyAndVersion(code, version, DefaultPageFileEntity.READSET_FULL);
        if (errorPageFileDo == null) {
            throw new ValidationException("Error page file not found .Code:" + code + " ; version: " + version);
        }
        String errorPagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/conf/errorpage");
        String fileName = code + "page.html";
        doWrite(errorPagePath, fileName, errorPageFileDo.getFileData());

        DefaultPageServerActiveDo errorPageServerActiveDo = new DefaultPageServerActiveDo();
        errorPageServerActiveDo.setKey(code).setVersion(version).setServerIp(LocalInfoPack.INSTANCE.getIp());
        defaultPageServerActiveDao.insert(errorPageServerActiveDo);
        logger.info("Install Local Error Page Success. ServerIp:" + LocalInfoPack.INSTANCE.getIp() + " code:" + code + " version:" + version);
    }

    @Override
    public DefaultPage getCurrentErrorPage(String code, Long slbId) throws Exception {
        DefaultPageActiveDo errorPageActiveDo = defaultPageActiveDao.findByKeyAndSlbIdAndType(code, slbId, PAGE_TYPE, DefaultPageActiveEntity.READSET_FULL);
        if (errorPageActiveDo == null) {
            return null;
        } else {
            DefaultPage res = new DefaultPage().setCode(code).setVersion(errorPageActiveDo.getVersion());
            DefaultPageFileDo page = defaultPageFileDao.findByKeyAndVersion(code, errorPageActiveDo.getVersion(), DefaultPageFileEntity.READSET_FULL);
            return res.setErrprPageFile(new String(page.getFileData()));
        }
    }

    @Override
    public DefaultPage getCurrentErrorPage(String code, String ip) throws Exception {
        DefaultPageServerActiveDo errorPageActiveDo = defaultPageServerActiveDao.findByServerIpAndKey(code, ip, DefaultPageServerActiveEntity.READSET_FULL);
        if (errorPageActiveDo == null) {
            return null;
        } else {
            DefaultPage res = new DefaultPage().setCode(code).setVersion(errorPageActiveDo.getVersion());
            DefaultPageFileDo page = defaultPageFileDao.findByKeyAndVersion(code, errorPageActiveDo.getVersion(), DefaultPageFileEntity.READSET_FULL);
            return res.setErrprPageFile(new String(page.getFileData()));
        }
    }

    @Override
    public byte[] getErrorPage(String code, Long version) throws Exception {
        DefaultPageFileDo errorPageActiveDo;
        if (version != null && version > 0) {
            errorPageActiveDo = defaultPageFileDao.findByKeyAndVersion(code, version, DefaultPageFileEntity.READSET_FULL);
        } else {
            return null;
        }
        if (errorPageActiveDo == null) {
            throw new ValidationException("Not Found any error page version by code. code:" + code + "version:" + version);
        } else {
            return errorPageActiveDo.getFileData();
        }
    }

    @Override
    public Long getMaxErrorPageVersion(String code) throws Exception {
        DefaultPageFileDo errorPageActiveDo = defaultPageFileDao.findMaxVersionByKey(code, DefaultPageFileEntity.READSET_FULL);
        if (errorPageActiveDo == null) {
            throw new ValidationException("Not Found any error page version by code. code:" + code);
        } else {
            return errorPageActiveDo.getVersion();
        }
    }

    @Override
    public void errorPageInit() throws Exception {
        Long[] slbIds = entityFactory.getSlbIdsByIp(LocalInfoPack.INSTANCE.getIp(), SelectionMode.ONLINE_FIRST);
        if (slbIds == null || slbIds.length == 0) {
            logger.error("Not Found Slb Id by local ip.IP:" + LocalInfoPack.INSTANCE.getIp());
            return;
        }
        Long slbId = slbIds[0];

        List<DefaultPageActiveDo> list = defaultPageActiveDao.findBySlbIdAndType(slbId, PAGE_TYPE, DefaultPageActiveEntity.READSET_FULL);
        if (list == null || list.size() == 0) {
            return;
        }
        for (DefaultPageActiveDo e : list) {
            DefaultPageServerActiveDo tmp = defaultPageServerActiveDao.findByServerIpAndKey(e.getKey(), LocalInfoPack.INSTANCE.getIp(), DefaultPageServerActiveEntity.READSET_FULL);
            if (tmp == null || tmp.getVersion() != e.getVersion()) {
                installLocalErrorPage(e.getKey(), e.getVersion());
                logger.info("Error Page Init Success. Ip: " + LocalInfoPack.INSTANCE.getIp() + "Code:" + e.getKey() + " version:" + e.getVersion());
            }
        }
    }

    private void doWrite(String path, String fileName, byte[] content) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        FileOutputStream writer = null;
        try {
            File file = new File(path + File.separator + fileName);
            writer = new FileOutputStream(file);
            writer.write(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public boolean ready() {
        try {
            errorPageInit();
            return true;
        } catch (Exception e) {
            logger.error("Init Error Page Failed.", e);
            return false;
        }
    }
}
