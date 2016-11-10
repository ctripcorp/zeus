package com.ctrip.zeus.service.nginx;

/**
 * Created by zhoumy on 2015/11/3.
 */
public interface CertificateInstaller {

    CertificateConfig getConfig();

    void installDefault(Long certId) throws Exception;

    String localInstall(Long vsId, Long certId) throws Exception;

    void localUninstall(Long vsId) throws Exception;

    void localBatchInstall(Long slbId, boolean overwriteIfExist) throws Exception;

    boolean defaultExists();

    boolean exists(Long vsId);
}
