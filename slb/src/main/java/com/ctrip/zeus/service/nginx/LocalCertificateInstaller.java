package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhoumy on 2015/11/3.
 */
public interface LocalCertificateInstaller {

    CertificateConfig getConfig();

    void installDefault(Long certId) throws Exception;

    void installDefault(CertCertificateWithBLOBs cert) throws Exception;

    String localInstall(Long vsId, CertCertificateWithBLOBs cert) throws Exception;

    void localUninstall(Long vsId) throws Exception;

    void localBatchInstall(Set<Long> vsIds, Map<Long, String> rVsDomain, List<CertCertificateWithBLOBs> domainCerts, boolean overwriteIfExist) throws Exception;

    boolean defaultExists();

    boolean exists(Long vsId);

    void batchInstall(Long slbId, boolean overwriteIfExists) throws Exception;

    void batchInstall(Map<Long, CertCertificateWithBLOBs> vsCertMap, boolean overwriteIfExists) throws Exception;
}
