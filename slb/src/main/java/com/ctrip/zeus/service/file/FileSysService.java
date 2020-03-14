package com.ctrip.zeus.service.file;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.model.page.DefaultFile;

import java.util.List;

/**
 * Created by fanqq on 2017/10/17.
 */
public interface FileSysService {

    void updateFile(String fileName, byte[] file) throws Exception;

    boolean installFile(Long slbId, String fileName, Long version) throws Exception;

    void installLocalFile(String fileName, byte[] data) throws Exception;

    void installLocalFile(String fileName, Long version) throws Exception;

    FileData getCurrentFile(Long slbId, String fileName) throws Exception;

    FileData getCurrentFile(String ip, String fileName) throws Exception;

    List<FileData> getCurrentFiles(Long slbId) throws Exception;

    byte[] getFile(String fileName, Long version) throws Exception;

    Long getMaxFileVersion(String fileName) throws Exception;

    void updateFileStatus(String name, String ip, Long version) throws Exception;

    void fileInit() throws Exception;
}
