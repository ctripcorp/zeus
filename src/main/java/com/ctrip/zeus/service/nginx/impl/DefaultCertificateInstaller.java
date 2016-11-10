package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateInstaller;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.ctrip.zeus.util.IOUtils;
import com.ctrip.zeus.util.S;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;

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
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    private final CertificateConfig config = new CertificateConfig();

    @Override
    public CertificateConfig getConfig() {
        return config;
    }

    @Override
    public void installDefault(Long certId) throws Exception {
        createSslPath();

        String defaultPath = config.getDefaultCertInstallDir();
        File f = new File(defaultPath);
        if (!f.exists()) {
            f.mkdirs();
        }

        CertificateDo cert = certificateDao.findById(certId, CertificateEntity.READSET_FULL);
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
    }

    @Override
    public String localInstall(Long vsId, Long certId) throws Exception {
        CertificateDo cert;
        if (certId == null || (cert = certificateDao.findByPK(certId, CertificateEntity.READSET_FULL)) == null) {
            throw new ValidationException("Some error occurred when searching the certificate.");
        }

        writeCertificates(vsId, cert);
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
    public void localBatchInstall(Long slbId, boolean overwriteIfExist) throws Exception {
        Set<IdVersion> searchKey = virtualServerCriteriaQuery.queryBySlbId(slbId);
        Set<Long> next = new HashSet<>();
        for (IdVersion sk : searchKey) {
            next.add(sk.getId());
        }
        next.retainAll(virtualServerCriteriaQuery.queryBySsl(true));

        if (next.size() == 0) return;

        String rootPath = config.getInstallDir(0L);
        rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));

        File f = new File(rootPath);
        if (!f.exists()) throw new IOException("Certificate root path " + rootPath + " does not exist.");

        if (!overwriteIfExist) {
            Set<Long> curr = new HashSet<>();
            for (File c : f.listFiles()) {
                if ("default".equals(c.getName())) continue;
                try {
                    curr.add(Long.parseLong(c.getName()));
                } catch (Exception ex) {
                }
            }
            next.removeAll(curr);
        }
        if (next.size() == 0) return;

        Set<IdVersion> searchKeys = virtualServerCriteriaQuery.queryByIdsAndMode(next.toArray(new Long[next.size()]), SelectionMode.ONLINE_FIRST);
        Map<Long, String> rVsDomain = new HashMap<>();
        for (VirtualServer vs : virtualServerRepository.listAll(searchKeys.toArray(new IdVersion[searchKeys.size()]))) {
            if (vs.getDomains().size() == 1) {
                rVsDomain.put(vs.getId(), vs.getDomains().get(0).getName().toLowerCase());
            }
            if (vs.getDomains().size() > 1) {
                String[] dd = new String[vs.getDomains().size()];
                for (int i = 0; i < vs.getDomains().size(); i++) {
                    dd[i] = vs.getDomains().get(i).getName().toLowerCase();
                }
                Arrays.sort(dd);
                rVsDomain.put(vs.getId(), Joiner.on("|").join(dd));
            }
        }

        Map<String, CertificateDo> rDomainCert = new HashMap<>();
        //TODO find excluded cert
        for (CertificateDo d : certificateDao.findAllMaxByDomainAndState(rVsDomain.values().toArray(new String[rVsDomain.size()]), CertificateConfig.ONBOARD, CertificateEntity.READSET_FULL)) {
            rDomainCert.put(d.getDomain().toLowerCase(), d);
        }

        if (rVsDomain.size() > rDomainCert.size()) {
            Set<String> range = new HashSet<>(rVsDomain.values());
            range.removeAll(rDomainCert.keySet());
            if (range.size() > 0)
                throw new ValidationException("Missing certificates of virtual servers: " + Joiner.on(",").join(range) + ".");
        }

        for (Map.Entry<Long, String> e : rVsDomain.entrySet()) {
            CertificateDo d = rDomainCert.get(e.getValue());
            writeCertificates(e.getKey(), d);
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

    private void writeCertificates(Long vsId, CertificateDo cert) throws IOException, DalException {
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
        rCertificateSlbServerDao.insertOrUpdateCert(new RelCertSlbServerDo().setVsId(vsId).setIp(S.getIp()).setCertId(cert.getId()));
    }
}
