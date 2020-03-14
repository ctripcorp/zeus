package com.ctrip.zeus.service.file.impl;

import com.ctrip.zeus.client.InstallDefaultPageClient;
import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.file.FilesInDbService;
import com.ctrip.zeus.service.file.IndexPageService;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.startup.PreCheck;
import com.ctrip.zeus.util.EnvHelper;
import com.ctrip.zeus.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
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
    private ConfigHandler configHandler;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private FilesInDbService filesInDbService;
    @Resource
    private LocalInfoService localInfoService;
    @Resource
    private ConfigValueService configValueService;

    private final static String KEY = "index";
    private final static String PAGE_TYPE = "index";

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void updateIndexPageFile(byte[] file) throws Exception {
        filesInDbService.addFile(KEY, file);
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

        boolean succeed = true;
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).indexPage(version, data)) {
                succeed = false;
                break;
            }
        }
        if (succeed) {
            filesInDbService.updateFileStatus(KEY, slbId, PAGE_TYPE, version);
        } else {
            throw new NginxProcessingException("Not all slb server install index page successfully");
        }

    }

    @Override
    public void installLocalIndexPage(Long version) throws Exception {
        byte[] file = filesInDbService.getFile(KEY, version);
        if (file == null) {
            throw new ValidationException("Index page file not found .Code:" + KEY + " ; version: " + version);
        }
        String indexPagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/html");
        String fileName = "index.html";
        doWrite(indexPagePath, fileName, file);

        filesInDbService.updateFileStatus(KEY, LocalInfoPack.INSTANCE.getIp(), version);
        logger.info("Install Local Index Page Success. ServerIp:" + LocalInfoPack.INSTANCE.getIp() + " code:"
                + KEY + " version:" + version);
    }

    @Override
    public void installLocalIndexPage(byte[] content, Long version) throws Exception {
        if (content == null) {
            throw new ValidationException("Index page file shall not be empty");
        }
        String indexPagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/html");
        String fileName = "index.html";
        doWrite(indexPagePath, fileName, content);

        InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).indexPageSetStatus(LocalInfoPack.INSTANCE.getIp(), version);

        logger.info("Install Local Index Page Success. ServerIp:" + LocalInfoPack.INSTANCE.getIp() + " code:"
                + KEY + " version:" + version);
    }

    @Override
    public List<FileData> listCurrentIndexPage(Long slbId) throws Exception {
        return filesInDbService.getCurrentFiles(PAGE_TYPE, slbId);
    }

    @Override
    public FileData getCurrentIndexPage(String fileName, String ip) throws Exception {
        return filesInDbService.getCurrentFile(fileName, ip);
    }

    @Override
    public FileData getCurrentIndexPage(Long slbId) throws Exception {
        return filesInDbService.getCurrentFile(KEY, PAGE_TYPE, slbId);
    }

    @Override
    public FileData getCurrentIndexPage(String ip) throws Exception {
        return filesInDbService.getCurrentFile(KEY, ip);
    }

    @Override
    public byte[] getIndexPage(Long version) throws Exception {
        return filesInDbService.getFile(KEY, version);
    }

    @Override
    public Long getMaxIndexPageVersion() throws Exception {
        return filesInDbService.getMaxIndexPageVersion(KEY);
    }

    @Override
    public void updateFileStatus(String ip, Long version) throws Exception {
        filesInDbService.updateFileStatus(KEY, ip, version);
    }

    @Override
    public void indexPageInit() throws Exception {
        String ip = LocalInfoPack.INSTANCE.getIp();
        checkIndexPath();

        if (EnvHelper.portal()) {
            Long[] slbIds = entityFactory.getSlbIdsByIp(ip, SelectionMode.ONLINE_FIRST);
            if (slbIds == null || slbIds.length == 0) {
                logger.error("Not Found Slb Id by local ip.IP:" + ip);
                return;
            }
            Long slbId = slbIds[0];

            List<FileData> files = filesInDbService.getCurrentFiles(PAGE_TYPE, slbId);
            if (files == null || files.size() == 0) {
                return;
            }

            for (FileData e : files) {
                if (e == null || !e.getVersion().equals(e.getVersion())) {
                    installLocalIndexPage(e.getVersion());
                    logger.info("Index Page Init Success. Ip: " + ip + "Code:"
                            + e.getKey() + " version:" + e.getVersion());
                }
            }
            return;
        }
        Long slbId = localInfoService.getLocalSlbId();
        if (slbId == null) {
            logger.error("Not Found Slb Id by local ip.IP:" + ip);
            return;
        }
        List<DefaultFile> indexFiles = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).indexPageListFiles(slbId);
        if (indexFiles == null || indexFiles.size() == 0) {
            return;
        }

        // get and install on current server
        for (DefaultFile e : indexFiles) {
            DefaultFile indexFile = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", "http://localhost:8099/")).indexPageFile(e.getName(), ip);
            if (indexFile == null || !indexFile.getVersion().equals(e.getVersion())) {
                ByteArrayOutputStream fileData = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).indexPageFile(e.getVersion());
                installLocalIndexPage(fileData.toByteArray(), e.getVersion());
                logger.info("Index Page Init Success. Ip: " + ip + "Code:"
                        + e.getName() + " version:" + e.getVersion());
            }
        }
    }

    private void checkIndexPath() throws IOException {
        String pagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/html");
        File f = new File(pagePath);
        try {
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (SecurityException ex) {
        }

        if (!(f.canExecute() && f.canRead() && f.canWrite())) {
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
