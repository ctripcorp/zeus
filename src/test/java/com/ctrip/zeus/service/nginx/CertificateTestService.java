package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dal.core.RCertificateSlbServerDao;
import com.ctrip.zeus.dal.core.RCertificateSlbServerEntity;
import com.ctrip.zeus.dal.core.RelCertSlbServerDo;
import com.ctrip.zeus.service.nginx.impl.CertificateServiceImpl;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by zhoumy on 2015/11/5.
 */
public class CertificateTestService extends CertificateServiceImpl {
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;
    @Resource
    private CertificateInstaller certificateInstaller;

    @Override
    public void install(final Long vsId, List<String> ips, final Long certId, boolean overwriteIfExist) throws Exception {
        Set<String> check = new HashSet<>();
        if (!overwriteIfExist) {
            List<RelCertSlbServerDo> dos = rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL);
            for (RelCertSlbServerDo d : dos) {
                check.add(d.getIp() + "#" + vsId + "#" + d.getCertId());
            }
        }

        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final String ip : ips) {
                if (check.contains(ip + "#" + vsId + "#" + certId)) continue;
                reqQueue.add(new CertManageTask(ip, new Long[]{vsId}, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Long[] args) {
                        try {
                            certificateInstaller.localInstall(vsId, certId);
                            return Response.status(200).build();
                        } catch (Exception e) {
                            return Response.status(500).build();
                        }
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            boolean succ = true;
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.getSuccess();
            }

            if (!succ) {
                throw new Exception("Certificate installation failed.");
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