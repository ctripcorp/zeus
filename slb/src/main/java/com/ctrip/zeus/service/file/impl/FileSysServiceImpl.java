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
import com.ctrip.zeus.service.file.FileSysService;
import com.ctrip.zeus.service.file.FilesInDbService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.startup.PreCheck;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.EnvHelper;
import com.ctrip.zeus.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by fanqq on 2017/10/17.
 */
@Service("fileSysService")
public class FileSysServiceImpl implements FileSysService, PreCheck {

    @Resource
    private FilesInDbService filesInDbService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private LocalInfoService localInfoService;
    @Autowired
    private ConfigValueService configValueService;

    private final String TYPE = "F_File";

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void updateFile(String fileName, byte[] file) throws Exception {
        assertFileNameNull(fileName);
        filesInDbService.addFile(fileName, file);
    }

    @Override
    public boolean installFile(Long slbId, String fileName, Long version) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }
        assertFileNameNull(fileName);
        byte[] data = filesInDbService.getFile(fileName, version);
        if (data == null) {
            throw new ValidationException("Not Found file by slbId. id:" + slbId + "and version:" + version
                    + "and file " + fileName);
        }

        boolean succeed = true;
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).installFile(fileName, data)) {
                succeed = false;
                break;
            }
        }
        if (succeed) {
            filesInDbService.updateFileStatus(fileName, slbId, TYPE, version);
            for (SlbServer slbServer : slb.getSlbServers()) {
                filesInDbService.updateFileStatus(fileName, slbServer.getIp(), version);
            }
        }

        return succeed;
    }

    @Override
    public void installLocalFile(String fileName, byte[] data) throws Exception {
        assertFileNameNull(fileName);
        if (data == null) {
            throw new ValidationException("Not found File.File:" + fileName);
        }
        String path = configHandler.getStringValue("file.root.path", "/opt/app/nginx/conf/file");
        doWrite(path, fileName, data);
        return;
    }

    @Override
    public void installLocalFile(String fileName, Long version) throws Exception {
        assertFileNameNull(fileName);
        byte[] content = filesInDbService.getFile(fileName, version);
        if (content == null) {
            throw new ValidationException("Not found Session Ticket Key File.File:" + fileName + " ; version: " + version);
        }
        String path = configHandler.getStringValue("file.root.path", "/opt/app/nginx/conf/file");
        doWrite(path, fileName, content);
        filesInDbService.updateFileStatus(fileName, LocalInfoPack.INSTANCE.getIp(), version);
    }

    @Override
    public FileData getCurrentFile(Long slbId, String fileName) throws Exception {
        assertFileNameNull(fileName);
        return filesInDbService.getCurrentFile(fileName, TYPE, slbId);
    }

    @Override
    public FileData getCurrentFile(String ip, String fileName) throws Exception {
        assertFileNameNull(fileName);
        return filesInDbService.getCurrentFile(fileName, ip);
    }

    @Override
    public List<FileData> getCurrentFiles(Long slbId) throws Exception {
        return filesInDbService.getCurrentFiles(TYPE, slbId);
    }

    @Override
    public byte[] getFile(String fileName, Long version) throws Exception {
        assertFileNameNull(fileName);
        return filesInDbService.getFile(fileName, version);
    }

    @Override
    public Long getMaxFileVersion(String fileName) throws Exception {
        assertFileNameNull(fileName);
        return filesInDbService.getMaxIndexPageVersion(fileName);
    }

    @Override
    public void updateFileStatus(String name, String ip, Long version) throws Exception {
        AssertUtils.assertNotNull(name, "name is null");
        AssertUtils.assertNotNull(ip, "ip is null");
        AssertUtils.assertNotNull(version, "version is null");
        filesInDbService.updateFileStatus(name, ip, version);
    }

    @Override
    public void fileInit() throws Exception {
        String path = configHandler.getStringValue("file.root.path", "/opt/app/nginx/conf/file");
        checkIndexPath(path);

        Long slbId = localInfoService.getLocalSlbId();
        String ip = localInfoService.getLocalIp();
        if (slbId == null) {
            logger.error("Not Found Slb Id by local ip.IP:" + ip);
            return;
        }


        if (EnvHelper.portal()) {
            List<FileData> list = filesInDbService.getCurrentFiles(TYPE, slbId);
            if (list == null || list.size() == 0) {
                return;
            }
            for (FileData e : list) {
                FileData serverFile = getCurrentFile(localInfoService.getLocalIp(), e.getKey());
                if (serverFile == null || !serverFile.getVersion().equals(e.getVersion())) {
                    installLocalFile(e.getKey(), e.getVersion());
                }
            }

            return;
        }

        List<DefaultFile> list = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).fileBySlbId(slbId);
        if (list == null || list.size() == 0) {
            return;
        }
        for (DefaultFile e : list) {
            DefaultFile serverFile = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).fileByIpAndFileName(localInfoService.getLocalIp(), e.getName());
            if (serverFile == null || !serverFile.getVersion().equals(e.getVersion())) {
                ByteArrayOutputStream fileData = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).getFileData(e.getName(), e.getVersion());
                if (fileData != null) {
                    installLocalFile(e.getName(), fileData.toByteArray());
                    InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).fileSetStatus(ip, e.getVersion(), e.getName());
                }
            }
        }
    }

    private void assertFileNameNull(String fileName) throws ValidationException {
        if (fileName == null) {
            throw new ValidationException("File Name is null.");
        }
    }

    private void doWrite(String path, String fileName, byte[] content) throws IOException {
        checkIndexPath(path);
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

    private void checkIndexPath(String path) throws IOException {
        File f = new File(path);
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

    @Override
    public boolean ready() {
        try {
            fileInit();
            return true;
        } catch (Exception e) {
            logger.error("Init Failed.", e);
            return false;
        }
    }
}
