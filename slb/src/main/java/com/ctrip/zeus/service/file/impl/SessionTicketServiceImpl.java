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
import com.ctrip.zeus.service.file.FilesInDbService;
import com.ctrip.zeus.service.file.SessionTicketService;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.util.EnvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by fanqq on 2017/3/2.
 */
@Service("sessionTicketService")
public class SessionTicketServiceImpl implements SessionTicketService {
    @Resource
    FilesInDbService filesInDbService;
    @Resource
    SlbRepository slbRepository;
    @Resource
    ConfigHandler configHandler;
    @Autowired
    ConfigValueService configValueService;
    @Resource
    private LocalInfoService localInfoService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String FILE_NAME = "session_ticket";

    @Override
    public void addSessionTicketFile(byte[] content) throws Exception {
        filesInDbService.addFile(FILE_NAME, content);
    }

    @Override
    public Long getMaxVersion(Long slbId) throws Exception {
        return filesInDbService.getMaxIndexPageVersion(FILE_NAME);
    }

    @Override
    public boolean installSessionTicketFile(Long slbId, Long version) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }
        byte[] data = getFile(version);
        if (data == null) {
            throw new ValidationException("Not Found file by slbId. id:" + slbId + "and version:" + version);
        }

        boolean succeed = true;
        for (SlbServer slbServer : slb.getSlbServers()) {
            if (!InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).sessionTicketFile(data)) {
                succeed = false;
                break;
            }
        }
        if (succeed) {
            filesInDbService.updateFileStatus(FILE_NAME, slbId, FILE_NAME, version);
            for (SlbServer slbServer : slb.getSlbServers()) {
                filesInDbService.updateFileStatus(FILE_NAME, slbServer.getIp(), version);
            }
        }

        return succeed;
    }

    @Override
    public void installLocalSessionTicketFile(Long version) throws Exception {
        byte[] content = getFile(version);
        if (content == null) {
            throw new ValidationException("Not found Session Ticket Key File.File:" + FILE_NAME + " ; version: " + version);
        }
        String path = configHandler.getStringValue("session.ticket.key.path", "/opt/app/nginx/conf/ticket");
        String fileName = configHandler.getStringValue("session.ticket.key.file", "sessionTicket.key");
        doWrite(path, fileName, content);
        filesInDbService.updateFileStatus(FILE_NAME, LocalInfoPack.INSTANCE.getIp(), version);
    }

    @Override
    public boolean installLocalSessionTicketFile(byte[] content) throws Exception {
        if (content == null) {
            throw new ValidationException("file content shall not be empty");
        }
        String path = configHandler.getStringValue("session.ticket.key.path", "/opt/app/nginx/conf/ticket");
        String fileName = configHandler.getStringValue("session.ticket.key.file", "sessionTicket.key");
        doWrite(path, fileName, content);
        return true;
    }

    @Override
    public FileData getCurrentSessionTicketFile(Long slbId) throws Exception {
        return filesInDbService.getCurrentFile(FILE_NAME, FILE_NAME, slbId);
    }

    @Override
    public FileData getCurrentSessionTicketFile(String ip) throws Exception {
        return filesInDbService.getCurrentFile(FILE_NAME, ip);
    }

    @Override
    public void sessionTicketFileStartInit() throws Exception {
        Long slbId = localInfoService.getLocalSlbId();
        String ip = localInfoService.getLocalIp();

        if (EnvHelper.portal()) {
            FileData slbFile = getCurrentSessionTicketFile(slbId);
            FileData ipFile = getCurrentSessionTicketFile(ip);
            if (slbFile != null && ipFile == null) {
                installLocalSessionTicketFile(slbFile.getVersion());
            }
            return;
        }

        if (slbId == null || ip == null) {
            logger.error("Not Found Slb Id by local ip.IP:" + ip);
            return;
        }

        // call api to get default files for current slb and ip
        HashMap<String, DefaultFile> defaultFileHashMap = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", "http://localhost:8099/")).sessionTicketDefaultFiles(slbId, ip);
        if (defaultFileHashMap == null || defaultFileHashMap.size() == 0)
            return;
        if (defaultFileHashMap.get("slb") != null) {
            DefaultFile defaultFile = defaultFileHashMap.get("slb");
            DefaultFile ipFile = defaultFileHashMap.get("ip");
            if (ipFile == null || !ipFile.getVersion().equals(defaultFile.getVersion())) {
                ByteArrayOutputStream byteArrayOutputStream = InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", "http://localhost:8099/")).sessionTicketFile(defaultFile.getVersion());
                if (byteArrayOutputStream != null) {
                    boolean succeed = installLocalSessionTicketFile(byteArrayOutputStream.toByteArray());
                    if (succeed) {
                        InstallDefaultPageClient.getClientByHost(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi())).sessionTicketSetStatus(ip, defaultFileHashMap.get("slb").getVersion());
                    }
                }
            }
        }
    }

    @Override
    public byte[] getFile(Long version) throws Exception {
        return filesInDbService.getFile(FILE_NAME, version);
    }

    @Override
    public void updateFileStatus(String ip, Long version) throws Exception {
        filesInDbService.updateFileStatus(FILE_NAME, ip, version);
    }

    @Override
    public void updateFileStatus(Long slbId, Long version) throws Exception {
        filesInDbService.updateFileStatus(FILE_NAME, slbId, FILE_NAME, version);
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
}
