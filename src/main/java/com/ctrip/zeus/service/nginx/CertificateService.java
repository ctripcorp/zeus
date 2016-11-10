package com.ctrip.zeus.service.nginx;

import java.io.InputStream;
import java.util.List;

/**
 * Created by zhoumy on 2015/10/29.
 */
public interface CertificateService {

    Long getCertificateOnBoard(String domain) throws Exception;

    Long update(Long certId, boolean state) throws Exception;

    Long upload(InputStream cert, InputStream key, String domain, boolean state) throws Exception;

    Long upgrade(InputStream cert, InputStream key, String domain, boolean state) throws Exception;

    void installDefault(Long certId, List<String> ips, boolean overwriteIfExist) throws Exception;

    void install(Long vsId, List<String> ips, Long certId, boolean overwriteIfExist) throws Exception;

    void install(Long slbId, List<String> ips, boolean overwriteIfExist) throws Exception;

    void replaceAndInstall(Long prevCertId, Long currCertId) throws Exception;

    void uninstall(Long vsId, List<String> ips) throws Exception;
}