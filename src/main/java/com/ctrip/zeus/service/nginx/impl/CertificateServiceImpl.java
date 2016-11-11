package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.util.IOUtils;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("certificateService")
public class CertificateServiceImpl implements CertificateService {
    @Resource
    private CertificateDao certificateDao;
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Long getCertificateOnBoard(String domain) throws Exception {
        String[] searchKey = domain.split("\\|");
        Arrays.sort(searchKey);
        domain = Joiner.on("|").join(searchKey);

        CertificateDo value = certificateDao.findMaxByDomainAndState(domain, CertificateConfig.ONBOARD, CertificateEntity.READSET_FULL);
        if (value == null) {
            throw new ValidationException("Cannot find corresponding certificate referring domain " + domain + ".");
        }
        return value.getId();
    }

    @Override
    public Long update(Long certId, boolean state) throws Exception {
        CertificateDo c = certificateDao.findById(certId, CertificateEntity.READSET_FULL);
        if (c == null) {
            throw new ValidationException("certificate cannot be found with the given id=" + certId + ".");
        }
        CertificateDo prev = certificateDao.findMaxByDomainAndState(c.getDomain(), state, CertificateEntity.READSET_ID_VERSION);

        c.setState(state);
        certificateDao.updateStateById(c, CertificateEntity.UPDATESET_FULL);

        return prev == null ? 0L : prev.getId();
    }

    @Override
    public Long upload(InputStream cert, InputStream key, String domain, boolean state) throws Exception {
        if (cert == null || key == null) {
            throw new ValidationException("Cert or key file is null.");
        }
        String[] searchKey = domain.split("\\|");
        Arrays.sort(searchKey);
        domain = Joiner.on("|").join(searchKey);

        List<CertificateDo> certs = certificateDao.findAllByDomain(domain, CertificateEntity.READSET_ID_VERSION);
        if (certs.size() > 0) {
            throw new ValidationException("Certificate exists. Duplicate upload request is rejected. Reference domain=" + domain + ".");
        }
        CertificateDo d = new CertificateDo().setCert(IOUtils.getBytes(cert)).setKey(IOUtils.getBytes(key)).setDomain(domain).setState(state).setVersion(1);
        certificateDao.insert(d);
        return d.getId();
    }

    @Override
    public Long upgrade(InputStream cert, InputStream key, String domain, boolean state) throws Exception {
        if (cert == null || key == null) {
            throw new ValidationException("Cert or key file is null.");
        }
        String[] searchKey = domain.split("\\|");
        Arrays.sort(searchKey);
        domain = Joiner.on("|").join(searchKey);

        List<CertificateDo> certs = certificateDao.findAllByDomain(domain, CertificateEntity.READSET_ID_VERSION);
        if (certs.size() == 0) {
            throw new ValidationException("No history has found. No need to upgrade. Reference domain=" + domain + ".");
        }
        int maxVersion = -1;
        for (CertificateDo c : certs) {
            int v = c.getVersion();
            maxVersion = maxVersion < v ? v : maxVersion;
        }
        CertificateDo d = new CertificateDo().setCert(IOUtils.getBytes(cert)).setKey(IOUtils.getBytes(key)).setDomain(domain).setState(state).setVersion(maxVersion + 1);
        certificateDao.insert(d);
        return d.getId();
    }

    @Override
    public void installDefault(final Long certId, List<String> ips, final boolean overwriteIfExist) throws Exception {
        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (String ip : ips) {
                reqQueue.add(new CertManageTask(ip, null, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Long[] args) {
                        return c.requestInstallDefault(certId, overwriteIfExist);
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            boolean succ = true;
            String message = "";
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message += String.format("%s(%s)", tr.ip, tr.success);
            }
            if (!succ) {
                throw new Exception("Fail to install default certificate on slb servers. " + message);
            }
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void install(final Long vsId, List<String> ips, final Long certId, boolean overwriteIfExist) throws Exception {
        Set<String> check = new HashSet<>();
        if (!overwriteIfExist) {
            for (RelCertSlbServerDo d : rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL)) {
                check.add(d.getIp() + "#" + vsId + "#" + d.getCertId());
            }
        }

        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final String ip : ips) {
                if (check.contains(ip + "#" + vsId + "#" + certId)) {
                    continue;
                }
                reqQueue.add(new CertManageTask(ip, new Long[]{vsId, certId}, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Long[] args) {
                        return c.requestInstall(args[0], args[1]);
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            boolean succ = true;
            String message = "";
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message += String.format("%s(%s)", tr.ip, tr.success);
            }
            if (!succ) {
                throw new Exception("Fail to install certificate on slb servers. " + message);
            }
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void install(final Long slbId, List<String> ips, final boolean overwriteIfExist) throws Exception {
        if (slbId == null || slbId.equals(0L) || ips.size() == 0) return;

        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final String ip : ips) {
                reqQueue.add(new CertManageTask(ip, new Long[]{slbId}, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Long[] args) {
                        return c.requestBatchInstall(args[0], overwriteIfExist);
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            boolean succ = true;
            String message = "";
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message += String.format("%s(%s)", tr.ip, tr.success);
            }
            if (!succ) {
                throw new Exception("Fail to install certificate on slb servers. " + message);
            }
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void replaceAndInstall(Long prevCertId, Long currCertId) throws Exception {
        if (currCertId == null || currCertId == 0L) {
            throw new ValidationException("Certificate to replace is empty.");
        }

        Map<Long, Set<String>> reinstallingServers = new HashMap<>();
        if (prevCertId != null && prevCertId != 0L) {
            for (RelCertSlbServerDo e : rCertificateSlbServerDao.findByCert(prevCertId, RCertificateSlbServerEntity.READSET_FULL)) {
                Set<String> v = reinstallingServers.get(e.getVsId());
                if (v == null) {
                    v = new HashSet<>();
                    reinstallingServers.put(e.getVsId(), v);
                }
                v.add(e.getIp());
            }
        }
        for (RelCertSlbServerDo e : rCertificateSlbServerDao.findByCert(currCertId, RCertificateSlbServerEntity.READSET_FULL)) {
            Set<String> v = reinstallingServers.get(e.getVsId());
            if (v == null) {
                v = new HashSet<>();
                reinstallingServers.put(e.getVsId(), v);
            }
            v.add(e.getIp());
        }

        for (Map.Entry<Long, Set<String>> e : reinstallingServers.entrySet()) {
            install(e.getKey(), new ArrayList<>(e.getValue()), currCertId, true);
        }
    }

    @Override
    public void uninstall(final Long vsId, List<String> ips) throws Exception {
        Map<String, RelCertSlbServerDo> abandoned = new HashMap<>();
        for (RelCertSlbServerDo d : rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL)) {
            if (ips.contains(d.getIp()))
                abandoned.put(d.getIp(), d);
        }

        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final Map.Entry<String, RelCertSlbServerDo> entry : abandoned.entrySet()) {
                reqQueue.add(new CertManageTask(entry.getKey(), new Long[]{vsId}, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Long[] args) {
                        return c.requestUninstall(args[0]);
                    }
                }));
                for (FutureTask futureTask : reqQueue) {
                    executor.execute(futureTask);
                }
            }
            boolean succ = true;
            String message = "";
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message += String.format("%s(%s)", tr.ip, tr.success);
            }

            if (!succ) {
                throw new Exception("Fail to uninstall certificate from slb servers. " + message);
            } else {
                rCertificateSlbServerDao.deleteById(abandoned.values().toArray(new RelCertSlbServerDo[abandoned.size()]));
            }
        } finally {
            executor.shutdown();
        }
    }

    protected interface CertClientOperation {
        Response call(CertSyncClient c, Long[] args);
    }

    protected class CertManageTask extends FutureTask<CertTaskResponse> {

        public CertManageTask(final String ip, final Long[] args, final CertClientOperation op) {
            this(new Callable<CertTaskResponse>() {
                @Override
                public CertTaskResponse call() throws Exception {
                    CertSyncClient c = new CertSyncClient("http://" + ip + ":8099");
                    Response res;
                    try {
                        res = op.call(c, args);
                    } catch (Exception ex) {
                        logger.error("Fail to send out install certificate request to " + ip + ".", ex);
                        return new CertTaskResponse(false, ip);
                    }

                    if (res.getStatus() / 100 == 2) {
                        return new CertTaskResponse(true, ip);
                    } else {
                        try {
                            String responseEntity = IOUtils.inputStreamStringify((InputStream) res.getEntity());
                            logger.error("Fail to install certificate on " + ip + ". " + responseEntity);
                        } catch (IOException ex) {
                            logger.error("Fail to install certificate on " + ip + ". An unexpected error occurred when stringifying response.", ex);
                        }
                        return new CertTaskResponse(false, ip);
                    }
                }
            });
        }

        CertManageTask(Callable<CertTaskResponse> callable) {
            super(callable);
        }
    }

    protected class CertTaskResponse {
        boolean success;
        String ip;

        CertTaskResponse(boolean success, String ip) {
            this.success = success;
            this.ip = ip;
        }

        public boolean getSuccess() {
            return success;
        }
    }

    protected static class CertSyncClient extends AbstractRestClient {
        protected CertSyncClient(String url) {
            super(url);
        }

        Response requestInstallDefault(Long certId, boolean force) {
            return getTarget().path("/api/cert/default/localInstall").queryParam("certId", certId).queryParam("force", force).request().headers(getDefaultHeaders()).get();
        }

        Response requestInstall(Long vsId, Long certId) {
            return getTarget().path("/api/cert/localInstall").queryParam("vsId", vsId).queryParam("certId", certId).request().headers(getDefaultHeaders()).get();
        }

        Response requestUninstall(Long vsId) {
            return getTarget().path("/api/cert/localUninstall").queryParam("vsId", vsId).request().headers(getDefaultHeaders()).get();
        }

        Response requestBatchInstall(Long slbId, boolean force) {
            return getTarget().path("/api/cert/localBatchInstall").queryParam("slbId", slbId).queryParam("force", force).request().headers(getDefaultHeaders()).get();
        }
    }
}
