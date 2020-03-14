package com.ctrip.zeus.service.file;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.model.page.DefaultFile;

import java.util.List;

/**
 * Created by fanqq on 2017/3/2.
 */
public interface FilesInDbService {
    void addFile(String name, byte[] file) throws Exception;

    byte[] getFile(String name, Long version) throws Exception;

    FileData getCurrentFile(String name, String type, Long slbId) throws Exception;

    List<FileData> getCurrentFiles(String type, Long slbId) throws Exception;

    FileData getCurrentFile(String name, String ip) throws Exception;

    void updateFileStatus(String name, String ip, Long version) throws Exception;

    void updateFileStatus(String name, Long slbId, String type, Long version) throws Exception;

    Long getMaxIndexPageVersion(String name) throws Exception;
}
