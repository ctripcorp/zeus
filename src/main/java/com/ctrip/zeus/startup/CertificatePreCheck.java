package com.ctrip.zeus.startup;

import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.nginx.CertificateInstaller;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2016/8/9.
 */
@Component("certificatePreCheck")
public class CertificatePreCheck implements PreCheck {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Resource
    private CertificateService certificateService;
    @Resource
    private CertificateInstaller certificateInstaller;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;

    @Override
    public boolean ready() {
        if (!certificateInstaller.defaultExists()) {
            try {
                Long certId = certificateService.getCertificateOnBoard("_localhost");
                certificateInstaller.installDefault(certId);
            } catch (Exception e) {
                logger.error("Fail to install default certificate.", e);
                return false;
            }
        }

        long slbId = 0L;
        try {
            int v = -1;
            for (IdVersion key : slbCriteriaQuery.queryBySlbServerIp(LocalInfoPack.INSTANCE.getIp())) {
                if (key.getVersion() > v) {
                    slbId = key.getId();
                }
            }
        } catch (Exception e) {
        }

        try {
            if (slbId != 0L) {
                certificateInstaller.localBatchInstall(slbId, false);
            }
            return true;
        } catch (Exception e) {
            logger.error("Fail to install vs ssl certificate.", e);
            return false;
        }
    }
}

