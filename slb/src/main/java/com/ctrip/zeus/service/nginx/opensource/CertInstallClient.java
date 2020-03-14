package com.ctrip.zeus.service.nginx.opensource;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.dao.entity.CertCertificateSlbServerR;
import com.ctrip.zeus.dao.entity.CertCertificateSlbServerRExample;
import com.ctrip.zeus.dao.entity.CertCertificateWithBLOBs;
import com.ctrip.zeus.dao.mapper.CertCertificateSlbServerRMapper;
import com.ctrip.zeus.model.cert.VSCertficate;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

import static com.ctrip.zeus.auth.util.AuthTokenUtil.getDefaultHeaders;

/**
 * @Discription
 **/
@Component
public class CertInstallClient {

    @Resource
    protected CertCertificateSlbServerRMapper certificateSlbServerRMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void installDefault(CertCertificateWithBLOBs certificate, List<String> ips, boolean overwriteIfExist) throws Exception {
        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (String ip : ips) {
                reqQueue.add(new CertManageTask(ip, null, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Object[] args) {
                        return c.requestInstallDefault(certificate, overwriteIfExist);
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            boolean succ = true;
            StringBuilder message = new StringBuilder(128);
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message.append(String.format("%s(%s)", tr.ip, tr.success));
            }
            if (!succ) {
                throw new Exception("Fail to install default certificate on slb servers. " + message.toString());
            }
        } finally {
            executor.shutdown();
        }
    }

    /***
     * In case of install cert for vs.
     * called by api resource and add/update vs.
     * @param vsId
     * @param ips
     * @param certificate
     * @param overwriteIfExist
     * @throws Exception
     */
    public void install(Long vsId, List<String> ips, CertCertificateWithBLOBs certificate, boolean overwriteIfExist) throws Exception {
        Set<String> check = new HashSet<>();
        if (!overwriteIfExist) {
            CertCertificateSlbServerRExample example = new CertCertificateSlbServerRExample();
            example.or().andVsIdEqualTo(vsId);
            example.setOrderByClause("id DESC");
            for (CertCertificateSlbServerR r : certificateSlbServerRMapper.selectByExample(example)) {
                check.add(r.getIp() + "#" + vsId + "#" + r.getCertId());
            }
        }

        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final String ip : ips) {
                if (check.contains(ip + "#" + vsId + "#" + certificate.getId())) {
                    continue;
                }
                reqQueue.add(new CertManageTask(ip, new Object[]{vsId, certificate}, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Object[] args) {
                        Response response = c.requestInstall((Long) args[0], (CertCertificateWithBLOBs) args[1]);
                        if (response != null && response.getStatus() / 100 == 2) {
                            return c.setCertificateServerStatus(vsId, certificate.getId());
                        }
                        return null;
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            boolean succ = true;
            StringBuilder message = new StringBuilder(128);
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message.append(String.format("%s(%s)", tr.ip, tr.success));
            }
            if (!succ) {
                throw new Exception("Fail to install certificate on slb servers. " + message.toString());
            }
        } finally {
            executor.shutdown();
        }
    }

    public void install(Long slbId, List<CertCertificateWithBLOBs> certificates, Map<Long, String> vsIdDomainMap, List<String> ips, boolean overwrite) throws Exception {
        if (slbId == null || slbId.equals(0L) || ips.size() == 0) {
            return;
        }
        if (vsIdDomainMap == null || vsIdDomainMap.size() == 0) {
            return;
        }
        final boolean overwriteIfExist = overwrite;

        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final String ip : ips) {
                reqQueue.add(new CertManageTask(ip, new Object[]{new HashSet<>(vsIdDomainMap.keySet()), vsIdDomainMap, certificates}, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Object[] args) {
                        return c.requestBatchInstall((Set<Long>) args[0], (Map<Long, String>) args[1], (List<CertCertificateWithBLOBs>) args[2], overwriteIfExist);
                    }
                }));
            }
            for (FutureTask futureTask : reqQueue) {
                executor.execute(futureTask);
            }

            boolean succ = true;
            StringBuilder message = new StringBuilder(128);
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message.append(String.format("%s(%s)", tr.ip, tr.success));
            }
            if (!succ) {
                throw new Exception("Fail to install certificate on slb servers. " + message.toString());
            }
        } finally {
            executor.shutdown();
        }
    }

    public void uninstall(Long vsId, List<String> ips) throws Exception {
        Map<String, CertCertificateSlbServerR> abandoned = new HashMap<>();

        List<CertCertificateSlbServerR> records = certificateSlbServerRMapper.selectByExample(
                new CertCertificateSlbServerRExample().createCriteria().andVsIdEqualTo(vsId).example());
        for (CertCertificateSlbServerR r : records) {
            if (ips.contains(r.getIp()))
                abandoned.put(r.getIp(), r);
        }

        List<FutureTask<CertTaskResponse>> reqQueue = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(ips.size() < 6 ? ips.size() : 6);
        try {
            for (final Map.Entry<String, CertCertificateSlbServerR> entry : abandoned.entrySet()) {
                reqQueue.add(new CertManageTask(entry.getKey(), new Long[]{vsId}, new CertClientOperation() {
                    @Override
                    public Response call(CertSyncClient c, Object[] args) {
                        return c.requestUninstall((Long) args[0]);
                    }
                }));
                for (FutureTask futureTask : reqQueue) {
                    executor.execute(futureTask);
                }
            }
            boolean succ = true;
            StringBuilder message = new StringBuilder(128);
            for (FutureTask<CertTaskResponse> futureTask : reqQueue) {
                CertTaskResponse tr = futureTask.get(3000, TimeUnit.MILLISECONDS);
                succ &= tr.success;
                message.append(String.format("%s(%s)", tr.ip, tr.success));
            }

            if (!succ) {
                throw new Exception("Fail to uninstall certificate from slb servers. " + message.toString());
            } else {
                for (CertCertificateSlbServerR record : records) {
                    certificateSlbServerRMapper.deleteByPrimaryKey(record.getId());
                }
            }
        } finally {
            executor.shutdown();
        }
    }
    protected interface CertClientOperation {
        Response call(CertSyncClient c, Object[] args);
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

    protected class CertManageTask extends FutureTask<CertTaskResponse> {

        public CertManageTask(final String ip, final Object[] args, final CertClientOperation op) {
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

    public static class CertSyncClient extends AbstractRestClient {
        public CertSyncClient(String url) {
            super(url);
        }

        Response requestInstallDefault(Long certId, boolean force) {
            return getTarget().path("/api/cert/default/localInstall").queryParam("certId", certId).queryParam("force", force).request().headers(getDefaultHeaders()).get();
        }

        Response requestInstallDefault(CertCertificateWithBLOBs certificate, boolean force) {
            return getTarget().
                    queryParam("force", force).
                    path("/api/cert/default/localInstall").
                    request(MediaType.APPLICATION_JSON).
                    headers(getDefaultHeaders()).
                    post(Entity.entity(certificate, MediaType.APPLICATION_JSON), Response.class);
        }

        Response requestInstall(Long vsId, CertCertificateWithBLOBs certificate) {
            return getTarget().
                    queryParam("vsId", vsId).
                    path("/api/cert/localInstall").
                    request(MediaType.APPLICATION_JSON).
                    headers(getDefaultHeaders()).
                    post(Entity.entity(certificate, MediaType.APPLICATION_JSON), Response.class);
        }

        public HashMap<Long, CertCertificateWithBLOBs> requestCerts(Set<Long> vsIds) {
            WebTarget target = getTarget().path("/api/cert/find");
            if (vsIds == null) return null;
            ArrayList<Long> vsIdsArray = new ArrayList<>(vsIds);
            for (int i = 0; i < vsIdsArray.size(); i++) {
                target = target.queryParam("vsIds", vsIdsArray.get(i));
            }
            String responseStr = target.request().headers(getDefaultHeaders()).get(String.class);

            HashMap<Long, CertCertificateWithBLOBs> result = ObjectJsonParser.parse(responseStr,
                    new com.fasterxml.jackson.core.type.TypeReference<HashMap<Long, CertCertificateWithBLOBs>>() {
                    });

            return result;
        }


        Response setCertificateServerStatus(Long vsId, Long certId) {
            return getTarget().queryParam("vsId", vsId).queryParam("certId", certId).path("/api/cert/set/status").request().headers(getDefaultHeaders()).get();
        }

        Response requestUninstall(Long vsId) {
            return getTarget().path("/api/cert/localUninstall").queryParam("vsId", vsId).request().headers(getDefaultHeaders()).get();
        }

        Response requestBatchInstall(Set<Long> vsIds, Map<Long, String> rVsDomainMap, List<CertCertificateWithBLOBs> certificates, boolean force) {
            VSCertficate vsCertficate = new VSCertficate();
            vsCertficate.setCertificates(certificates);
            vsCertficate.setVsDomain(rVsDomainMap);
            vsCertficate.setVsIds(new ArrayList<>(vsIds));

            return getTarget().
                    queryParam("force", force).
                    path("/api/cert/localBatchInstall").
                    request(MediaType.APPLICATION_JSON).
                    headers(getDefaultHeaders()).
                    post(Entity.json(vsCertficate), Response.class);
        }
    }
}
