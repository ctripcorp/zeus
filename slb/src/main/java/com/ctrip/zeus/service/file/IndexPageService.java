package com.ctrip.zeus.service.file;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.model.page.DefaultFile;

import java.util.List;

/**
 * Created by fanqq on 2016/9/1.
 */
public interface IndexPageService {
    void updateIndexPageFile(byte[] file) throws Exception;

    void installIndexPage(Long slbId, Long version) throws Exception;

    void installLocalIndexPage(Long version) throws Exception;

    void installLocalIndexPage(byte[] content, Long version) throws Exception;

    List<FileData> listCurrentIndexPage(Long slbId) throws Exception;

    FileData getCurrentIndexPage(String fileName, String ip) throws Exception;

    FileData getCurrentIndexPage(Long slbId) throws Exception;

    FileData getCurrentIndexPage(String ip) throws Exception;

    byte[] getIndexPage(Long version) throws Exception;

    Long getMaxIndexPageVersion() throws Exception;

    void updateFileStatus(String ip, Long version) throws Exception;

    void indexPageInit() throws Exception;
}
