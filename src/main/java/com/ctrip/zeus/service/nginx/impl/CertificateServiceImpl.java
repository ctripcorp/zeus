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
import java.util.concurrent.atomic.AtomicBoolean;

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
    public Long getCertificateOnBoard(String[] domains) throws Exception {
        CertificateDo value;
        String[] searchRange = getDomainSearchRange(domains);
        String domainValue;
        boolean state = CertificateConfig.ONBOARD;
        if (searchRange.length == 0)
            throw new ValidationException("Domain info is not found when searching certificate.");
        if (searchRange.length == 1) {
            domainValue = searchRange[0];
            value = certificateDao.findMaxByDomainAndState(domainValue, state, CertificateEntity.READSET_FULL);
        } else {
            List<CertificateDo> check = certificateDao.grossByDomainAndState(searchRange, state, CertificateEntity.READSET_FULL);
            if (check.isEmpty())
                throw new ValidationException("Cannot find corresponding certificate.");
            if (check.size() > 1) {
                throw new ValidationException("Multiple certificates found referring the domain list.");
            }
            domainValue = check.get(0).getDomain();
            value = certificateDao.findMaxByDomainAndState(domainValue, state, CertificateEntity.READSET_FULL);
        }
        if (value == null)
            throw new ValidationException("Cannot find corresponding certificate referring domain " + domainValue + ".");
        return value.getId();
    }

    @Override
    public Long upload(InputStream cert, InputStream key, String domain, boolean state) throws Exception {
        if (cert == null || key == null)
            throw new ValidationException("Cert or key file is null.");
        CertificateDo max = certificateDao.findMaxByDomainAndState(domain, state, CertificateEntity.READSET_FULL);
        if (max != null)
            throw new ValidationException("Certificate exists.");
        CertificateDo d = new CertificateDo()
                .setCert(IOUtils.getBytes(cert)).setKey(IOUtils.getBytes(key)).setDomain(domain).setState(state).setVersion(1);
        certificateDao.insert(d);
        return d.getId();
    }

    @Override
    public Long upgrade(InputStream cert, InputStream key, String domain, boolean state) throws Exception {
        if (cert == null || key == null)
            throw new ValidationException("Cert or key file is null.");
        CertificateDo max = certificateDao.findMaxByDomainAndState(domain, state, CertificateEntity.READSET_FULL);
        if (max == null)
            throw new ValidationException("No history has found. No need to upgrade.");
        CertificateDo d = new CertificateDo()
                .setCert(IOUtils.getBytes(cert)).setKey(IOUtils.getBytes(key)).setDomain(domain).setState(state).setVersion(max.getVersion() + 1);
        certificateDao.insert(d);
        return d.getId();
    }

    @Override
    public void install(final Long vsId, List<String> ips, final Long certId) throws Exception {
        List<RelCertSlbServerDo> dos = rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL);
        Set<String> check = new HashSet<>();
        for (RelCertSlbServerDo d : dos) {
            check.add(d.getIp() + "#" + vsId + "#" + d.getCertId());
        }

        final AtomicBoolean success = new AtomicBoolean(true);
        List<FutureTask<String>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final String ip : ips) {
                if (check.contains(ip + "#" + vsId + "#" + certId)) continue;

                reqQueue.add(new FutureTask<>(new Callable<String>() {
                    @Override
                    public String call() {
                        CertSyncClient c = new CertSyncClient("http://" + ip + ":8099");
                        Response res;
                        try {
                            res = c.requestInstall(vsId, certId);
                        } catch (Exception ex) {
                            success.set(false);
                            logger.error(ip + ":" + "Fail to get response. ", ex);
                            return ip + ":" + "Fail to get response.\n";
                        }
                        if (res.getStatus() / 100 > 2)
                            res = c.requestInstall(vsId, certId);
                        if (res.getStatus() / 100 > 2) {
                            success.set(false);
                            try {
                                String error = ip + ":" + IOUtils.inputStreamStringify((InputStream) res.getEntity());
                                logger.error(error);
                                return error + "\n";
                            } catch (IOException e) {
                                logger.error(ip + ":" + "Unable to parse the response entity.", e);
                                return ip + ":" + "Unable to parse the response entity.\n";
                            }
                        }
                        return ip + ":" + "success.";
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            String message = "";
            for (FutureTask futureTask : reqQueue) {
                message += futureTask.get(3000, TimeUnit.MILLISECONDS);
            }

            if (!success.get()) {
                throw new Exception(message);
            }
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void install(final Long slbId, List<String> ips) throws Exception {
        final AtomicBoolean success = new AtomicBoolean(true);
        List<FutureTask<String>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final String ip : ips) {
                reqQueue.add(new FutureTask<>(new Callable<String>() {
                    @Override
                    public String call() {
                        CertSyncClient c = new CertSyncClient("http://" + ip + ":8099");
                        Response res;
                        try {
                            res = c.requestBatchInstall(slbId);
                        } catch (Exception ex) {
                            success.set(false);
                            logger.error(ip + ":" + "Fail to get response. ", ex);
                            return ip + ":" + "Fail to get response.\n";
                        }
                        if (res.getStatus() / 100 > 2)
                            res = c.requestBatchInstall(slbId);
                        if (res.getStatus() / 100 > 2) {
                            success.set(false);
                            try {
                                String error = ip + ":" + IOUtils.inputStreamStringify((InputStream) res.getEntity());
                                logger.error(error);
                                return error + "\n";
                            } catch (IOException e) {
                                logger.error(ip + ":" + "Unable to parse the response entity.", e);
                                return ip + ":" + "Unable to parse the response entity.\n";
                            }
                        }
                        return ip + ":" + "success.";
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            String message = "";
            for (FutureTask futureTask : reqQueue) {
                message += futureTask.get(3000, TimeUnit.MILLISECONDS);
            }

            if (!success.get()) {
                throw new Exception(message);
            }
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void uninstallIfRecalled(final Long vsId, List<String> ips) throws Exception {
        Map<String, RelCertSlbServerDo> abandoned = new HashMap<>();
        for (RelCertSlbServerDo d : rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL)) {
            if (ips.contains(d.getIp()))
                abandoned.put(d.getIp(), d);
        }

        final AtomicBoolean success = new AtomicBoolean(true);
        List<FutureTask<String>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final Map.Entry<String, RelCertSlbServerDo> entry : abandoned.entrySet()) {
                reqQueue.add(new FutureTask<>(new Callable<String>() {
                    @Override
                    public String call() {
                        CertSyncClient c = new CertSyncClient("http://" + entry.getKey() + ":8099");
                        Response res;
                        try {
                            res = c.requestUninstall(vsId);
                        } catch (Exception ex) {
                            success.set(false);
                            logger.error(entry.getKey() + ":" + "Fail to get response. ", ex);
                            return entry.getKey() + ":" + "Fail to get response.\n";
                        }
                        if (res.getStatus() / 100 > 2)
                            res = c.requestUninstall(vsId);
                        if (res.getStatus() / 100 > 2) {
                            success.set(false);
                            try {
                                String error = entry.getKey() + ":" + IOUtils.inputStreamStringify((InputStream) res.getEntity());
                                logger.error(error);
                                return error + "\n";
                            } catch (IOException e) {
                                logger.error(entry.getKey() + ":" + "Unable to parse the response entity.", e);
                                return entry.getKey() + ":" + "Unable to parse the response entity.\n";
                            }
                        }
                        return entry.getKey() + ":" + "success";
                    }
                }));
                for (FutureTask futureTask : reqQueue) {
                    executor.execute(futureTask);
                }

                String message = "";
                for (FutureTask futureTask : reqQueue) {
                    message += futureTask.get(30000, TimeUnit.MILLISECONDS);
                }

                if (!success.get()) {
                    throw new Exception(message);
                } else {
                    rCertificateSlbServerDao.deleteAllById(entry.getValue());
                }
            }
        } finally {
            executor.shutdown();
        }
    }

    private String[] getDomainSearchRange(String[] domains) {
        if (domains.length <= 1)
            return domains;
        else {
            Arrays.sort(domains);
//            String[] values = new String[domains.length + 1];
            String[] values = new String[1];
            values[0] = Joiner.on("|").join(domains);
//            for (int i = 1; i < values.length; i++) {
//                values[i] = domains[i - 1];
//            }
            return values;
        }
    }

    private static class CertSyncClient extends AbstractRestClient {
        protected CertSyncClient(String url) {
            super(url);
        }

        public Response requestInstall(Long vsId, Long certId) {
            return getTarget().path("/api/op/installcerts").queryParam("vsId", vsId).queryParam("certId", certId).request().get();
        }

        public Response requestUninstall(Long vsId) {
            return getTarget().path("/api/op/uninstallcerts").queryParam("vsId", vsId).request().get();
        }

        public Response requestBatchInstall(Long slbId) {
            return getTarget().path("/api/op/cert/batchInstall").queryParam("slbId", slbId).request().headers(getDefaultHeaders()).get();
        }
    }
}
