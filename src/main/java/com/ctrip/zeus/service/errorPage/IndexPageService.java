package com.ctrip.zeus.service.errorPage;

import com.ctrip.zeus.page.entity.DefaultPage;

/**
 * Created by fanqq on 2016/9/1.
 */
public interface IndexPageService {
    public void updateIndexPageFile(byte[] file) throws Exception;

    public void installIndexPage(Long slbId, Long version) throws Exception;

    public void installLocalIndexPage(Long version) throws Exception;

    public DefaultPage getCurrentIndexPage(Long slbId) throws Exception;

    public DefaultPage getCurrentIndexPage(String ip) throws Exception;

    public byte[] getIndexPage(Long version) throws Exception;

    public Long getMaxIndexPageVersion() throws Exception;

    public void indexPageInit() throws Exception;
}
