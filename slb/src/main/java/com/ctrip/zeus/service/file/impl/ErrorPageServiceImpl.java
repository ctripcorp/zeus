package com.ctrip.zeus.service.file.impl;

import com.ctrip.zeus.client.InstallDefaultPageClient;
import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.file.ErrorPageService;
import com.ctrip.zeus.service.file.FilesInDbService;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.startup.PreCheck;
import com.ctrip.zeus.util.EnvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

/**
 * Created by fanqq on 2016/8/22.
 */
@Service("errorPageService")
public class ErrorPageServiceImpl implements ErrorPageService, PreCheck {

    @Resource
    private ConfigHandler configHandler;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private FilesInDbService filesInDbService;
    @Resource
    LocalInfoService localInfoService;

    @Autowired
    private ConfigValueService configValueService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String PAGE_TYPE = "installErrorPage";

    @Override
    public void updateErrorPageFile(String code, byte[] file) throws Exception {
        filesInDbService.addFile(codeToFileName(code), file);
        logger.info("Update Error page file success. Code: " + code);
    }

    private String codeToFileName(String code) {
        return PAGE_TYPE + "_" + code;
    }

    @Override
    public boolean installErrorPage(Long slbId, String code, Long version) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }
        byte[] data = getErrorPage(code, version);
        if (data == null) {
            throw new ValidationException("Not Found page file by slbId. id:" + slbId + "and version:" + version);
        }

        // Install Error page
        Boolean succeed = true;
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).installErrorPage(code, data)) {
                succeed = false;
                break;
            }
        }
        if (succeed) {
            filesInDbService.updateFileStatus(codeToFileName(code), slbId, PAGE_TYPE, version);
            for (SlbServer slbServer : slb.getSlbServers()) {
                filesInDbService.updateFileStatus(codeToFileName(code), slbServer.getIp(), version);
            }
        }
        return succeed;
    }

    @Override
    public void installLocalErrorPage(String code, Long version) throws Exception {
        byte[] file = filesInDbService.getFile(codeToFileName(code), version);
        if (file == null) {
            throw new ValidationException("Error page file not found .Code:" + code + " ; version: " + version);
        }
        String errorPagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/conf/errorpage");
        String fileName = code + "page.html";
        doWrite(errorPagePath, fileName, file);

        filesInDbService.updateFileStatus(codeToFileName(code), LocalInfoPack.INSTANCE.getIp(), version);
        logger.info("Install Local Error Page Success. ServerIp:" + LocalInfoPack.INSTANCE.getIp() + " code:" + code + " version:" + version);
    }

    @Override
    public boolean installLocalErrorPage(byte[] content, String code) throws Exception {
        if (content == null || content.length==0) {
            throw new ValidationException("Error page file content is required");
        }

        String errorPagePath = configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/conf/errorpage");
        String fileName = code + "page.html";
        doWrite(errorPagePath, fileName, content);

        return true;
    }

    @Override
    public FileData getCurrentErrorPage(String code, Long slbId) throws Exception {
        return filesInDbService.getCurrentFile(codeToFileName(code), PAGE_TYPE, slbId);
    }

    @Override
    public FileData getCurrentErrorPage(String code, String ip) throws Exception {
        return filesInDbService.getCurrentFile(codeToFileName(code), ip);
    }

    @Override
    public byte[] getErrorPage(String code, Long version) throws Exception {
        return filesInDbService.getFile(codeToFileName(code), version);
    }

    @Override
    public Long getMaxErrorPageVersion(String code) throws Exception {
        return filesInDbService.getMaxIndexPageVersion(codeToFileName(code));
    }

    @Override
    public List<FileData> getCurrentFiles(Long slbId) throws Exception {
        return filesInDbService.getCurrentFiles(PAGE_TYPE, slbId);
    }

    @Override
    public void updateFileStatus(String code, String ip, Long version) throws Exception {
        filesInDbService.updateFileStatus(codeToFileName(code), ip, version);
    }

    @Override
    public void errorPageInit() throws Exception {
        String ip = LocalInfoPack.INSTANCE.getIp();
        Long slbId;

        if (EnvHelper.portal()) {
            Long[] slbIds = entityFactory.getSlbIdsByIp(LocalInfoPack.INSTANCE.getIp(), SelectionMode.ONLINE_FIRST);
            if (slbIds == null || slbIds.length == 0) {
                logger.error("Not Found Slb Id by local ip.IP:" + LocalInfoPack.INSTANCE.getIp());
                return;
            }
            slbId = slbIds[0];

            List<FileData> currentFiles = filesInDbService.getCurrentFiles(PAGE_TYPE, slbId);
            if (currentFiles == null || currentFiles.size() == 0) {
                return;
            }

            for (FileData e : currentFiles) {
                if (e == null || !e.getVersion().equals(e.getVersion())) {
                    installLocalErrorPage(fileNameToCode(e.getKey()), e.getVersion());
                    logger.info("Error Page Init Success. Ip: " + LocalInfoPack.INSTANCE.getIp() + "Code:"
                            + e.getKey() + " version:" + e.getVersion());
                }
            }
            return;
        }

        slbId = localInfoService.getLocalSlbId();
        if (slbId == null) {
            logger.error("Not Found Slb Id by local ip.IP:" + ip);
            return;
        }

        List<DefaultFile> files = InstallDefaultPageClient.
                getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).
                errorPageBySlbId(slbId);
        if (files == null || files.size() == 0) {
            return;
        }

        for (DefaultFile e : files) {
            String code = fileNameToCode(e.getName());
            DefaultFile file = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", "http://localhost:8099/")).errorPageDefaultFileByIp(code, ip);
            if (file == null || !file.getVersion().equals(e.getVersion())) {
                ByteArrayOutputStream fileData = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).getErrorPageFile(code, e.getVersion());
                if (fileData != null) {
                    boolean succeed = installLocalErrorPage(fileData.toByteArray(), code);
                    if (succeed) {
                        InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).errorPageSetStatus(ip, e.getVersion(), fileNameToCode(e.getName()));
                    }
                }
                logger.info("Error Page Init Success. Ip: " + LocalInfoPack.INSTANCE.getIp() + "Code:"
                        + fileNameToCode(e.getName()) + " version:" + e.getVersion());
            }
        }
    }

    private String fileNameToCode(String fileName) throws Exception {
        String result = null;
        if (fileName == null || fileName.isEmpty()) return null;
        if (fileName != null) {
            String[] parts = fileName.split("_");
            if (parts.length > 1) {
                result = parts[1];
            }
        }

        if (result != null) return result;
        throw new ValidationException("Failed to convert file name to code");
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