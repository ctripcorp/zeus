package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dal.core.RCertificateSlbServerDao;
import com.ctrip.zeus.dal.core.RCertificateSlbServerEntity;
import com.ctrip.zeus.dal.core.RelCertSlbServerDo;
import com.ctrip.zeus.service.nginx.impl.CertificateServiceImpl;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhoumy on 2015/11/5.
 */
public class CertificateTestService extends CertificateServiceImpl {
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;
    @Resource
    private CertificateInstaller certificateInstaller;

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
                        try {
                            certificateInstaller.localInstall(vsId, certId);
                        } catch (Exception e) {
                            return "failure";
                        }
                        return "success";
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

    public Set<String> getInstalledSlbServers(Long certId) throws Exception {
        Set<String> result = new HashSet<>();
        for (RelCertSlbServerDo d : rCertificateSlbServerDao.findByCert(certId, RCertificateSlbServerEntity.READSET_FULL)) {
            result.add(d.getIp());
        }
        return result;
    }
}