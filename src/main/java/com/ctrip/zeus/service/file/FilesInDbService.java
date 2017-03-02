package com.ctrip.zeus.service.file;

import com.ctrip.zeus.page.entity.DefaultFile;

/**
 * Created by fanqq on 2017/3/2.
 */
public interface FilesInDbService {
    void addFile(String name, byte[] file) throws Exception;

    byte[] getFile(String name, Long version) throws Exception;

    DefaultFile getCurrentFile(String name, Long slbId) throws Exception;

    DefaultFile getCurrentFile(String name, String ip) throws Exception;

    void updateFileStatus(String name, String ip, Long version) throws Exception;

    void updateFileStatus(String name, Long slbId, Long version) throws Exception;

    Long getMaxIndexPageVersion(String name) throws Exception;
}
