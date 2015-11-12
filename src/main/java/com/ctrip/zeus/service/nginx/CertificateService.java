package com.ctrip.zeus.service.nginx;

import java.io.InputStream;
import java.util.List;

/**
 * Created by zhoumy on 2015/10/29.
 */
public interface CertificateService {
//    Certificate get(Long vsId);

    Long getCertificateOnBoard(String[] domains) throws Exception;

    Long upload(InputStream cert, InputStream key, String domain, boolean state) throws Exception;

    Long upgrade(InputStream cert, InputStream key, String domain, boolean state) throws Exception;

    void install(Long vsId, List<String> ips, Long certId) throws Exception;

    void uninstallIfRecalled(Long vsId, List<String> ips) throws Exception;
}