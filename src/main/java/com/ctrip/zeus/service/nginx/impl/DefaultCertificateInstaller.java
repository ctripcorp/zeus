package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateInstaller;
import com.ctrip.zeus.util.IOUtils;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by zhoumy on 2015/11/3.
 */
@Service("certificateInstaller")
public class DefaultCertificateInstaller implements CertificateInstaller {
    private static Logger logger = LoggerFactory.getLogger(DefaultCertificateInstaller.class);

    @Resource
    private CertificateDao certificateDao;
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;

    private final CertificateConfig config = new CertificateConfig();
    private final String localhost = "_localhost";

    @PostConstruct
    private void init() {
        String ownerPath = config.getInstallDir(0L);
        ownerPath = ownerPath.substring(0, ownerPath.lastIndexOf("/"));
        File f = new File(ownerPath);
        boolean created = false;
        try {
            if (!f.exists()) created = f.mkdirs();
        } catch (SecurityException ex) {
        }
        if (!created) {
            logger.warn("Fail to create dir " + f.getPath() + " with default ownership.");
            if (!f.exists()) {
                final String create = "sudo mkdir -p " + f.getPath();
                try {
                    Process p = Runtime.getRuntime().exec(create);
                    p.waitFor();
                    logger.info(IOUtils.inputStreamStringify(p.getInputStream()));
                    logger.error(IOUtils.inputStreamStringify(p.getErrorStream()));
                } catch (IOException e) {
                    logger.error("Fail to execute command {}.", create, e);
                    return;
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        if (f.canExecute() && f.canRead() && f.canWrite()) {
            // go through to install default
        } else {
            final String chown = "sudo chown -R deploy.deploy " + f.getPath();
            try {
                Process p = Runtime.getRuntime().exec(chown);
                p.waitFor();
                logger.info(IOUtils.inputStreamStringify(p.getInputStream()));
                logger.error(IOUtils.inputStreamStringify(p.getErrorStream()));
            } catch (IOException e) {
                logger.error("Fail to execute command {}.", chown, e);
                return;
            } catch (InterruptedException e) {
                return;
            }
        }

        try {
            installDefault();
        } catch (Exception ex) {
            logger.error("Fail to install the default certificate.", ex);
        }
    }

    @Override
    public CertificateConfig getConfig() {
        return config;
    }

    @Override
    public void installDefault() throws Exception {
        CertificateDo cert = certificateDao.findMaxByDomainAndState(localhost, CertificateConfig.ONBOARD, CertificateEntity.READSET_FULL);
        if (cert == null) {
            logger.error("Could not find default certificate to install.");
            return;
        }

        String defaultPath = config.getInstallDir(0L);
        defaultPath = defaultPath.substring(0, defaultPath.lastIndexOf("/")) + "/default";
        File f = new File(defaultPath);
        if (f.exists()) {
            logger.info(defaultPath + " exists. No need to install default cert.");
            return;
        } else {
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
        rCertificateSlbServerDao.insertOrUpdateCert(
                new RelCertSlbServerDo().setVsId(vsId).setIp(S.getIp()).setCertId(certId));
        return cert.getDomain();
    }

    @Override
    public void localUninstall(Long vsId) throws IOException {
        File f = new File(config.getInstallDir(vsId));
        if (!f.exists())
            return;
        File[] subFiles = f.listFiles();
        for (File subFile : subFiles) {
            if (!subFile.delete())
                throw new IOException("Unable to delete file " + subFile.getName());
        }
        if (!f.delete())
            throw new IOException("Unable to directory " + f.getName());
    }

    @Override
    public boolean exists(Long vsId) {
        String dir = config.getInstallDir(vsId);
        return new File(dir + "/ssl.crt").exists() && new File(dir + "/ssl.key").exists();
    }
}
