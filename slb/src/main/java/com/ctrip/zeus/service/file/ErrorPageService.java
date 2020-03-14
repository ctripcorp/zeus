package com.ctrip.zeus.service.file;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.model.page.DefaultFile;

import java.util.List;

/**
 * Created by fanqq on 2016/8/22.
 */
public interface ErrorPageService {
    void updateErrorPageFile(String code, byte[] file) throws Exception;

    boolean installErrorPage(Long slbId, String code, Long version) throws Exception;

    void installLocalErrorPage(String code, Long version) throws Exception;

    boolean installLocalErrorPage(byte[] content, String code) throws Exception;

    FileData getCurrentErrorPage(String code, Long slbId) throws Exception;

    FileData getCurrentErrorPage(String code, String ip) throws Exception;

    byte[] getErrorPage(String code, Long version) throws Exception;

    Long getMaxErrorPageVersion(String code) throws Exception;

    List<FileData> getCurrentFiles(Long slbId) throws Exception;

    void updateFileStatus(String code, String ip, Long version) throws Exception;

    void errorPageInit() throws Exception;
}
