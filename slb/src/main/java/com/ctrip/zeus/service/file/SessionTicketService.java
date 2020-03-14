package com.ctrip.zeus.service.file;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.model.page.DefaultFile;

/**
 * Created by fanqq on 2017/3/2.
 */
public interface SessionTicketService {
    void addSessionTicketFile(byte[] content) throws Exception;

    Long getMaxVersion(Long slbId) throws Exception;

    /*
    * Portal
    * Call slb servers to install session ticket
    * */
    boolean installSessionTicketFile(Long slbId, Long version) throws Exception;

    /*
    * Agent
    * Agent install session ticket file
    * */
    boolean installLocalSessionTicketFile(byte[] content) throws Exception;

    /*
    * Portal
    * Precheck
    * */
    void installLocalSessionTicketFile(Long version) throws Exception;

    FileData getCurrentSessionTicketFile(Long slbId) throws Exception;

    FileData getCurrentSessionTicketFile(String ip) throws Exception;

    byte[] getFile(Long version) throws Exception;

    void updateFileStatus(String ip, Long version) throws Exception;

    void updateFileStatus(Long slbId, Long version) throws Exception;

    void sessionTicketFileStartInit() throws Exception;
}
