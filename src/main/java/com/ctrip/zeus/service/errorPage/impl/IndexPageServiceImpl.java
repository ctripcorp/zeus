package com.ctrip.zeus.service.errorPage.impl;

import com.ctrip.zeus.client.InstallDefaultPageClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.page.entity.DefaultPage;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.errorPage.IndexPageService;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.startup.PreCheck;
import com.ctrip.zeus.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by fanqq on 2016/9/1.
 */
@Service("indexPageService")
public class IndexPageServiceImpl implements IndexPageService, PreCheck {


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

    private final String KEY = "index";
    private final String PAGE_TYPE = "index";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void updateIndexPageFile(byte[] file) throws Exception {
        DefaultPageFileDo indexPageFileDo = new DefaultPageFileDo();
        indexPageFileDo.setKey(KEY).setFileData(file);
        DefaultPageFileDo max = defaultPageFileDao.findMaxVersionByKey(KEY, DefaultPageFileEntity.READSET_FULL);
        if (max == null) {
            indexPageFileDo.setVersion(1);
        } else {
            indexPageFileDo.setVersion(max.getVersion() + 1);
        }
        defaultPageFileDao.insert(indexPageFileDo);
        logger.info("Update Index page file success. Code: " + KEY + " Version:" + indexPageFileDo.getVersion());
    }

    @Override
    public void installIndexPage(Long slbId, Long version) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }
        byte[] data = getIndexPage(version);
        if (data == null) {
            throw new ValidationException("Not Found page file by slbId. id:" + slbId + "and version:" + version);
        }

        for (SlbServer slbServer : slb.getSlbServers()) {
            InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).indexPage(version);
        }

        DefaultPageActiveDo indexPageActiveDo = new DefaultPageActiveDo();
        indexPageActiveDo.setKey(KEY).setVersion(version).setSlbId(slbId).setType(PAGE_TYPE);
        defaultPageActiveDao.insert(indexPageActiveDo);

    }

    @Override
    public void installLocalIndexPage(Long version) throws Exception {
        DefaultPageFileDo indexPageFileDo = defaultPageFileDao.findByKeyAndVersion(KEY, version, DefaultPageFileEntity.READSET_FULL);
        if (indexPageFileDo == null) {
            throw new ValidationException("Index page file not found .Code:" + KEY + " ; version: " + version);
        }
        String indexPagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/html");
        String fileName = "index.html";
        doWrite(indexPagePath, fileName, indexPageFileDo.getFileData());

        DefaultPageServerActiveDo indexPageServerActiveDo = new DefaultPageServerActiveDo();
        indexPageServerActiveDo.setKey(KEY).setVersion(version).setServerIp(LocalInfoPack.INSTANCE.getIp());
        defaultPageServerActiveDao.insert(indexPageServerActiveDo);
        logger.info("Install Local Index Page Success. ServerIp:" + LocalInfoPack.INSTANCE.getIp() + " code:" + KEY + " version:" + version);
    }

    @Override
    public DefaultPage getCurrentIndexPage(Long slbId) throws Exception {
        DefaultPageActiveDo indexPageActiveDo = defaultPageActiveDao.findByKeyAndSlbIdAndType(KEY, slbId, PAGE_TYPE, DefaultPageActiveEntity.READSET_FULL);
        if (indexPageActiveDo == null) {
            return null;
        } else {
            DefaultPage res = new DefaultPage().setCode(KEY).setVersion(indexPageActiveDo.getVersion());
            DefaultPageFileDo page = defaultPageFileDao.findByKeyAndVersion(KEY, indexPageActiveDo.getVersion(), DefaultPageFileEntity.READSET_FULL);
            return res.setErrprPageFile(new String(page.getFileData()));
        }
    }

    @Override
    public DefaultPage getCurrentIndexPage(String ip) throws Exception {
        DefaultPageServerActiveDo indexPageActiveDo = defaultPageServerActiveDao.findByServerIpAndKey(KEY, ip, DefaultPageServerActiveEntity.READSET_FULL);
        if (indexPageActiveDo == null) {
            return null;
        } else {
            DefaultPage res = new DefaultPage().setCode(KEY).setVersion(indexPageActiveDo.getVersion());
            DefaultPageFileDo page = defaultPageFileDao.findByKeyAndVersion(KEY, indexPageActiveDo.getVersion(), DefaultPageFileEntity.READSET_FULL);
            return res.setErrprPageFile(new String(page.getFileData()));
        }
    }

    @Override
    public byte[] getIndexPage(Long version) throws Exception {
        DefaultPageFileDo indexPageActiveDo;
        if (version != null && version > 0) {
            indexPageActiveDo = defaultPageFileDao.findByKeyAndVersion(KEY, version, DefaultPageFileEntity.READSET_FULL);
        } else {
            return null;
        }
        if (indexPageActiveDo == null) {
            throw new ValidationException("Not Found any index page version. code:" + KEY + "version:" + version);
        } else {
            return indexPageActiveDo.getFileData();
        }
    }

    @Override
    public Long getMaxIndexPageVersion() throws Exception {
        DefaultPageFileDo indexPageActiveDo = defaultPageFileDao.findMaxVersionByKey(KEY, DefaultPageFileEntity.READSET_FULL);
        if (indexPageActiveDo == null) {
            throw new ValidationException("Not Found any index page version. code:" + KEY);
        } else {
            return indexPageActiveDo.getVersion();
        }
    }

    @Override
    public void indexPageInit() throws Exception {
        checkIndexPath();
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
                installLocalIndexPage(e.getVersion());
                logger.info("Error Page Init Success. Ip: " + LocalInfoPack.INSTANCE.getIp() + "Code:" + e.getKey() + " version:" + e.getVersion());
            }
        }
    }

    private void checkIndexPath() throws IOException {
       String pagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/html");
        File f = new File(pagePath);
        try {
            if (!f.exists()){
                f.mkdirs();
            }
        } catch (SecurityException ex) {
        }

        if (f.canExecute() && f.canRead() && f.canWrite()) {
            // go through to install default
        } else {
            final String chown = "sudo chown -R deploy.deploy " + f.getPath();
            try {
                Process p = Runtime.getRuntime().exec(chown);
                p.waitFor();
                logger.info(IOUtils.inputStreamStringify(p.getInputStream()));
                logger.error(IOUtils.inputStreamStringify(p.getErrorStream()));
            } catch (IOException e) {
                logger.error("Fail to execute command {}.", chown, e);
                return;
            } catch (InterruptedException e) {
                return;
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
            indexPageInit();
            return true;
        } catch (Exception e) {
            logger.error("Init Error Page Failed.", e);
            return false;
        }
    }
}
