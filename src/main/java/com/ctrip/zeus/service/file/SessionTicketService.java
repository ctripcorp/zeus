package com.ctrip.zeus.service.file;

import com.ctrip.zeus.page.entity.DefaultFile;

/**
 * Created by fanqq on 2017/3/2.
 */
public interface SessionTicketService {
    void addSessionTicketFile(byte[] content) throws Exception;

    Long getMaxVersion(Long slbId) throws Exception;

    void installSessionTicketFile(Long slbId, Long version) throws Exception;

    void installLocalSessionTicketFile(Long version) throws Exception;

    DefaultFile getCurrentSessionTicketFile(Long slbId) throws Exception;

    DefaultFile getCurrentSessionTicketFile(String ip) throws Exception;
}
