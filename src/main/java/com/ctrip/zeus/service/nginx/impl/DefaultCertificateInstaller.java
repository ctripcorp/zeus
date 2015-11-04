package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.dal.core.CertificateDao;
import com.ctrip.zeus.dal.core.CertificateDo;
import com.ctrip.zeus.dal.core.CertificateEntity;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateInstaller;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by zhoumy on 2015/11/3.
 */
@Service("certificateInstaller")
public class DefaultCertificateInstaller implements CertificateInstaller {
    private CertificateConfig config = new CertificateConfig();

    @Resource
    private CertificateDao certificateDao;

    @Override
    public CertificateConfig getConfig() {
        return config;
    }

    @Override
    public String localInstall(Long vsId, Long certId) throws Exception {
        CertificateDo cert;
        if (certId == null ||
                (cert = certificateDao.findByPK(certId, CertificateEntity.READSET_FULL)) == null) {
            throw new ValidationException("Some error occurred when searching the certificate.");
        }
        File f = new File(config.getInstallDir(vsId));
        if (!f.exists()) {
            f.mkdirs();
        }
        OutputStream certos = new FileOutputStream(f.getPath() + "/ssl.crt", config.getWriteFileOption());
        OutputStream keyos = new FileOutputStream(f.getPath() + "/ssl.key", config.getWriteFileOption());
        try {
            certos.write(cert.getCert());
            keyos.write(cert.getKey());
            certos.flush();
            certos.flush();
        } finally {
            certos.close();
            keyos.close();
        }
        return cert.getDomain();
    }

    @Override
    public boolean exists(Long vsId) {
        String dir = config.getInstallDir(vsId);
        return new File(dir + "/ssl.crt").exists() && new File(dir + "/ssl.key").exists();
    }
}
