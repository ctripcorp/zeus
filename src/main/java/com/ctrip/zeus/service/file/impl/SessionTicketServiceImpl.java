package com.ctrip.zeus.service.file.impl;

import com.ctrip.zeus.client.InstallDefaultPageClient;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.page.entity.DefaultFile;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.file.FilesInDbService;
import com.ctrip.zeus.service.file.SessionTicketService;
import com.ctrip.zeus.service.model.SlbRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    final String FILE_NAME = "session_ticket";

    @Override
    public void addSessionTicketFile(byte[] content) throws Exception {
        filesInDbService.addFile(FILE_NAME, content);
    }

    @Override
    public Long getMaxVersion(Long slbId) throws Exception {
        return filesInDbService.getMaxIndexPageVersion(FILE_NAME);
    }

    @Override
    public void installSessionTicketFile(Long slbId, Long version) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        if (slb == null || slb.getSlbServers().size() == 0) {
            throw new ValidationException("Not Found slb by id. id:" + slbId);
        }
        byte[] data = getFile(version);
        if (data == null) {
            throw new ValidationException("Not Found file by slbId. id:" + slbId + "and version:" + version);
        }

        for (SlbServer slbServer : slb.getSlbServers()) {
            InstallDefaultPageClient.getClientByServerIp(slbServer.getIp()).sessionTicketFile(version);
        }
        filesInDbService.updateFileStatus(FILE_NAME, slbId, version);
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
    public DefaultFile getCurrentSessionTicketFile(Long slbId) throws Exception {
        return filesInDbService.getCurrentFile(FILE_NAME, slbId);
    }

    @Override
    public DefaultFile getCurrentSessionTicketFile(String ip) throws Exception {
        return filesInDbService.getCurrentFile(FILE_NAME, ip);
    }

    private byte[] getFile(Long version) throws Exception {
        return filesInDbService.getFile(FILE_NAME, version);
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
