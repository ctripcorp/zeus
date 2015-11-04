package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.util.IOUtils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("certificateService")
public class CertificateServiceImpl implements CertificateService {
    @Resource
    private CertificateDao certificateDao;
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;

    @Override
    public Long pickCertificate(String[] domains) throws Exception {
        CertificateDo value;
        String[] searchRange = getDomainSearchRange(domains);
        if (searchRange.length == 1) {
            List<CertificateDo> result = certificateDao.findByDomainAndState(searchRange, CertificateConfig.ONBOARD, CertificateEntity.READSET_FULL);
            if (result.size() == 0)
                throw new ValidationException("Cannot find corresponding certificate.");
            value = result.get(0);
        } else {
            Map<String, CertificateDo> check = Maps.uniqueIndex(certificateDao.findByDomainAndState(searchRange, CertificateConfig.ONBOARD, CertificateEntity.READSET_FULL),
                    new Function<CertificateDo, String>() {
                        @Nullable
                        @Override
                        public String apply(CertificateDo certificateDo) {
                            return certificateDo.getDomain();
                        }
                    });
            if (check.isEmpty())
                throw new ValidationException("Cannot find corresponding certificate.");
            if ((value = check.get(searchRange[0])) == null) {
                if (check.values().size() > 1)
                    throw new ValidationException("Multiple certificates found referring the domain list.");
                value = check.values().iterator().next();
            }
        }
        if (value == null)
            throw new ValidationException("Some error occurred when matching certificate.");
        return value.getId();
    }

    @Override
    public Long upload(InputStream cert, InputStream key, String domain, boolean state) throws Exception {
        List<CertificateDo> abandoned = certificateDao.findByDomainAndState(new String[]{domain}, state, CertificateEntity.READSET_FULL);
        certificateDao.deleteById(abandoned.toArray(new CertificateDo[abandoned.size()]));
        CertificateDo d = new CertificateDo()
                .setCert(IOUtils.getBytes(cert)).setKey(IOUtils.getBytes(key)).setDomain(domain).setState(state);
        certificateDao.insert(d);
        return d.getId();
    }

    @Override
    public void command(Long vsId, List<String> ips, Long certId) throws Exception {
        CertificateDo cert = certificateDao.findByPK(certId, CertificateEntity.READSET_FULL);
        if (cert == null)
            throw new ValidationException("Certificate cannot be found.");
        for (String ip : ips) {
            rCertificateSlbServerDao.insertOrUpdateCommand(
                    new RelCertSlbServerDo().setIp(ip).setCommand(cert.getId()).setVsId(vsId));
        }
    }

    @Override
    public void recall(Long vsId, List<String> ips) throws Exception {
        for (String ip : ips) {
            rCertificateSlbServerDao.insertOrUpdateCommand(
                    new RelCertSlbServerDo().setIp(ip).setCommand(0L).setVsId(vsId));
        }
    }

    @Override
    public void install(Long vsId) throws Exception {
        List<RelCertSlbServerDo> dos = rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL);
        boolean success = true;
        String errMsg = "";
        for (RelCertSlbServerDo d : dos) {
            if (d.getCertId() == d.getCommand())
                continue;
            CertSyncClient c = new CertSyncClient("http://" + d.getIp() + ":8099");
            Response res = c.requestInstall(vsId, d.getCommand());
            // retry
            if (res.getStatus() / 100 > 2)
                res = c.requestInstall(vsId, d.getCommand());
            // still failed after retry
            if (res.getStatus() / 100 > 2) {
                success &= false;
                try {
                    errMsg += d.getIp() + ":" + IOUtils.inputStreamStringify((InputStream) res.getEntity()) + "\n";
                } catch (IOException e) {
                    errMsg += d.getIp() + ":" + "Unable to parse the response entity.\n";
                }
            }
            if (!success)
                throw new Exception(errMsg);
        }
    }

    @Override
    public void uninstallIfRecalled(Long vsId) throws Exception {
        List<RelCertSlbServerDo> dos = rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL);
        Map<String, RelCertSlbServerDo> abandoned = new HashMap<>();
        for (RelCertSlbServerDo d : dos) {
            if (d.getCommand() == 0L) {
                abandoned.put(d.getIp(), d);
            }
        }
        boolean success = true;
        String errMsg = "";
        for (Map.Entry<String, RelCertSlbServerDo> entry : abandoned.entrySet()) {
            boolean result = true;
            CertSyncClient c = new CertSyncClient("http://" + entry.getKey() + ":8099");
            Response res = c.requestUninstall(vsId);
            // retry
            if (res.getStatus() / 100 > 2)
                res = c.requestUninstall(vsId);
            // still failed after retry
            if (res.getStatus() / 100 > 2) {
                result &= false;
                try {
                    errMsg += entry.getKey() + ":" + IOUtils.inputStreamStringify((InputStream) res.getEntity()) + "\n";
                } catch (IOException e) {
                    errMsg += entry.getKey() + ":" + "Unable to parse the response entity.\n";
                }
            }
            if (result)
                rCertificateSlbServerDao.deleteAllById(entry.getValue());
            success &= result;
        }
        if (!success)
            throw new Exception(errMsg);
    }

    private String[] getDomainSearchRange(String[] domains) {
        if (domains.length <= 1)
            return domains;
        else {
            Arrays.sort(domains);
            String[] values = new String[domains.length + 1];
            values[0] = Joiner.on("|").join(domains);
            for (int i = 1; i < values.length; i++) {
                values[i] = domains[i - 1];
            }
            return values;
        }
    }

    private static class CertSyncClient extends AbstractRestClient {
        protected CertSyncClient(String url) {
            super(url);
        }

        public Response requestInstall(Long vsId, Long certId) throws ValidationException {
            return getTarget().path("/api/op/installcerts").queryParam("vsId", vsId).queryParam("certId", certId).request().get();
        }

        public Response requestUninstall(Long vsId) throws ValidationException {
            return getTarget().path("/api/op/uninstallcerts").queryParam("vsId", vsId).request().get();
        }
    }
}
