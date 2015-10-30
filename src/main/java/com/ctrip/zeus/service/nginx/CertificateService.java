package com.ctrip.zeus.service.nginx;

import java.io.InputStream;
import java.util.List;

/**
 * Created by zhoumy on 2015/10/29.
 */
public interface CertificateService {

    CertificateConfig getConfig();

    void cache(InputStream cert, InputStream key, Long vsId) throws Exception;

    void save(InputStream cert, InputStream key, String dir) throws Exception;

    void sendIfExist(Long vsId, List<String> ips) throws Exception;
}
