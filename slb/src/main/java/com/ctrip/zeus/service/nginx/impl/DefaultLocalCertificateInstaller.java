package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.model.Certificate;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.nginx.LocalCertificateInstaller;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.util.IOUtils;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhoumy on 2015/11/3.
 */

/**
 * Created by zhoumy on 2015/11/3.
 */
@Service("certificateInstaller")
public class DefaultLocalCertificateInstaller implements LocalCertificateInstaller {
    private static Logger logger = LoggerFactory.getLogger(DefaultLocalCertificateInstaller.class);


    @Resource
    private ConfigHandler configHandler;
    @Autowired
    private CertificateService certificateService;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    @Autowired
    private ConfigValueService configValueService;

    private final CertificateConfig config = new CertificateConfig();

    @Override
    public CertificateConfig getConfig() {
        return config;
    }

    @Override
    public void installDefault(CertCertificateWithBLOBs cert) throws Exception {
        createSslPath();

        String defaultPath = config.getDefaultCertInstallDir();
        File f = new File(defaultPath);
        if (!f.exists()) {
            f.mkdirs();
        }

        if (cert == null) {
            logger.error("Could not find default certificate to install.");
            return;
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
    public void installDefault(Long certId) throws Exception {
        Certificate cert = certificateService.getByCertId(certId, true);
        installDefault(C.toCertCertificateWithBlobs(cert));
    }

    private void createSslPath() {
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

        if (!(f.canExecute() && f.canRead() && f.canWrite())) {
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
    }

    @Override
    public String localInstall(Long vsId, CertCertificateWithBLOBs cert) throws Exception {
        writeCertificates(vsId, cert);
        return cert.getDomain();
    }

    @Override
    public void localUninstall(Long vsId) throws IOException {
        File f = new File(config.getInstallDir(vsId));
        if (!f.exists())
            return;
        File[] subFiles = f.listFiles();
        if (subFiles != null) {
            for (File subFile : subFiles) {
                if (!subFile.delete())
                    throw new IOException("Unable to delete file " + subFile.getName());
            }
        }
        if (!f.delete())
            throw new IOException("Unable to directory " + f.getName());
    }

    @Override
    public void localBatchInstall(Set<Long> next, Map<Long, String> rVsDomain,
                                  List<CertCertificateWithBLOBs> domainCerts,
                                  boolean overwriteIfExist) throws Exception {
        String rootPath = config.getInstallDir(0L);
        rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));

        File f = new File(rootPath);
        if (!f.exists()) throw new IOException("Certificate root path " + rootPath + " does not exist.");
        if (!overwriteIfExist) {
            Set<Long> curr = new HashSet<>();
            File[] list = f.listFiles();
            if (list != null) {
                for (File c : list) {
                    if ("default".equals(c.getName())) continue;
                    try {
                        curr.add(Long.parseLong(c.getName()));
                    } catch (Exception ex) {
                    }
                }
            }
            next.removeAll(curr);
        }
        if (next.size() == 0) return;

        if (!configHandler.getEnable("cert.localbatch.install.new.version", true)) {
            Map<String, CertCertificateWithBLOBs> rDomainCert = new HashMap<>();
            //TODO find excluded cert
            for (CertCertificateWithBLOBs d : domainCerts) {
                rDomainCert.put(d.getDomain().toLowerCase(), d);
            }

            if (rVsDomain.size() > rDomainCert.size()) {
                Set<String> range = new HashSet<>(rVsDomain.values());
                range.removeAll(rDomainCert.keySet());
                if (range.size() > 0)
                    throw new ValidationException("Missing certificates of virtual servers: " + Joiner.on(",").join(range) + ".");
            }

            for (Map.Entry<Long, String> e : rVsDomain.entrySet()) {
                CertCertificateWithBLOBs d = rDomainCert.get(e.getValue());
                writeCertificates(e.getKey(), d);
            }
        } else {
            CertificateServiceImpl.CertSyncClient syncClient = new CertificateServiceImpl.CertSyncClient(configHandler.getStringValue("agent.api.host", configValueService.getAgentApi()));
            HashMap<Long, CertCertificateWithBLOBs> vsIdCertMap = syncClient.requestCerts(next);
            if (vsIdCertMap == null || vsIdCertMap.size() != next.size()) {
                throw new ValidationException("Missing certificates of virtual servers: " + next.removeAll(vsIdCertMap.keySet()));
            }
            ArrayList<Long> nexts = new ArrayList<>(next);
            for (int i = 0; i < nexts.size(); i++) {
                Long vsId = new Long(nexts.get(i));
                if (!vsIdCertMap.containsKey(vsId)) {
                    throw new ValidationException("Missing certificates of virtual servers: " + vsId);
                }
                writeCertificates(vsId, vsIdCertMap.get(vsId));
            }
        }
    }

    @Override
    public boolean defaultExists() {
        String dir = config.getDefaultCertInstallDir();
        return new File(dir + "/ssl.crt").exists() && new File(dir + "/ssl.key").exists();
    }

    @Override
    public boolean exists(Long vsId) {
        String dir = config.getInstallDir(vsId);
        return new File(dir + "/ssl.crt").exists() && new File(dir + "/ssl.key").exists();
    }

    private void writeCertificates(Long vsId, CertCertificateWithBLOBs cert) throws IOException {
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
    }

    @Override
    public void batchInstall(Long slbId, boolean overwriteIfExists) throws Exception {
        // get certs from db and batch install into disk
        Set<IdVersion> keys = virtualServerCriteriaQuery.queryBySlbId(slbId);
        Set<Long> candidates = keys.stream().map(IdVersion::getId).collect(Collectors.toSet());
        candidates.retainAll(virtualServerCriteriaQuery.queryBySsl(true));

        Map<Long, Certificate> vsCertMap = certificateService.getActivatedCerts(candidates.toArray(new Long[candidates.size()]));
        Map<Long, CertCertificateWithBLOBs> vsCertBlobMap = new HashMap<>(vsCertMap.size());
        for (Map.Entry<Long, Certificate> longCertificateEntry : vsCertMap.entrySet()) {
            vsCertBlobMap.put(longCertificateEntry.getKey(), C.toCertCertificateWithBlobs(longCertificateEntry.getValue()));
        }
        batchInstall(vsCertBlobMap, overwriteIfExists);
    }

    @Override
    public void batchInstall(Map<Long, CertCertificateWithBLOBs> vsCertMap, boolean overwriteIfExists) throws Exception {
        if (vsCertMap != null) {
            for (Map.Entry<Long, CertCertificateWithBLOBs> vsCertPair : vsCertMap.entrySet()) {
                if (!overwriteIfExists && exists(vsCertPair.getKey())) {
                    continue;
                }

                localInstall(vsCertPair.getKey(), vsCertPair.getValue());
            }
        }
    }
}

