package com.ctrip.zeus.service.file;

import com.ctrip.zeus.page.entity.DefaultFile;

/**
 * Created by fanqq on 2016/8/22.
 */
public interface ErrorPageService {
    public void updateErrorPageFile(String code, byte[] file) throws Exception;

    public void installErrorPage(Long slbId, String code, Long version) throws Exception;

    public void installLocalErrorPage(String code, Long version) throws Exception;

    public DefaultFile getCurrentErrorPage(String code, Long slbId) throws Exception;

    public DefaultFile getCurrentErrorPage(String code, String ip) throws Exception;

    public byte[] getErrorPage(String code, Long version) throws Exception;

    public Long getMaxErrorPageVersion(String code) throws Exception;

    public void errorPageInit() throws Exception;
}
