package com.ctrip.zeus.startup.impl;

import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.nginx.LocalCertificateInstaller;
import com.ctrip.zeus.service.nginx.impl.CertificateServiceImpl;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.startup.PreCheck;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.util.EnvHelper;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by zhoumy on 2016/8/9.
 */
@Component("certificatePreCheck")
public class CertificatePreCheck implements PreCheck {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private CertificateService certificateService;
    @Resource
    private LocalCertificateInstaller localCertificateInstaller;
    @Resource
    private LocalInfoService localInfoService;
    @Resource
    private PropertyService propertyService;

    private DynamicStringProperty apiUrl = DynamicPropertyFactory.getInstance().getStringProperty("agent.api.host", "http://localhost:8099");
    private final CertificateServiceImpl.CertSyncClient certClient = new CertificateServiceImpl.CertSyncClient(apiUrl.get());


    @Override
    public boolean ready() {
        Long slbId;
        try {
            slbId = localInfoService.getLocalSlbIdWithRetry();
        } catch (Exception e) {
            logger.warn("Fail to get slbId through localInfoService.getLocalSlbIdWithRetry. Local ip: " + localInfoService.getLocalIp() + ", isPortal: " + EnvHelper.portal());
            return false;
        }
        try {
            if (!localCertificateInstaller.defaultExists()) {
                if (EnvHelper.portal()) {
                    // query default certificate from db
                    try {
                        CertCertificateWithBLOBs defaultCert = certificateService.getDefaultCert(slbId);
                        localCertificateInstaller.installDefault(defaultCert);
                    } catch (Exception e) {
                        logger.error("Fail to install default certificate.", e);
                        return false;
                    }
                } else {
                    // request install default
                    try {
                        CertCertificateWithBLOBs certificate = certClient.getDefaultCertificate(slbId);
                        if (certificate != null) {
                            localCertificateInstaller.installDefault(certificate);
                        }
                    } catch (Exception e) {
                        logger.error("Fail to install default certificate.", e);
                        return false;
                    }
                }
            }

            if (slbId == null || slbId == 0) {
                logger.warn("could get local slb id");
                return true;
            }


            if (EnvHelper.portal()) {
                localCertificateInstaller.batchInstall(slbId, false);
            } else {
                Map<Long, CertCertificateWithBLOBs> vsCertMap = certClient.getCertsBySlbId(slbId);
                localCertificateInstaller.batchInstall(vsCertMap, false);
            }
            return true;
        } catch (Exception e) {
            logger.error("Fail to remoteInstall vs ssl certificate.", e);
            return false;
        }
    }
}