package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.util.IOUtils;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("certificateService")
public class CertificateServiceImpl implements CertificateService {
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private CertificateDao certificateDao;
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;

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
    public void command(Long vsId, List<String> ips, boolean state) throws Exception {
        CertificateDo cert = pickCert(vsId, state);
        if (cert == null)
            throw new ValidationException("Some error occurred when searching the certificate.");
        for (String ip : ips) {
            rCertificateSlbServerDao.insert(new RelCertSlbServerDo().setIp(ip).setCommand(cert.getId()).setVsId(vsId));
        }
    }

    @Override
    public void command(Long vsId, List<String> ips, Long certId) throws Exception {
        CertificateDo cert = certificateDao.findByPK(certId, CertificateEntity.READSET_FULL);
        if (cert == null)
            throw new ValidationException("Certificate cannot be found.");
        for (String ip : ips) {
            rCertificateSlbServerDao.insert(new RelCertSlbServerDo().setIp(ip).setCommand(cert.getId()).setVsId(vsId));
        }
    }

    @Override
    public void install(Long vsId) throws Exception {
        List<RelCertSlbServerDo> dos = rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL);
        boolean success = true;
        String errMsg = "";
        for (RelCertSlbServerDo d : dos) {
            CertSyncClient c = new CertSyncClient("http://" + d.getIp() + ":8099/api/op/installcerts");
            Response res = c.requestInstall(vsId, d.getCommand());
            // retry
            if (res.getStatus() / 100 > 2)
                res = c.requestInstall(vsId, d.getCommand());
            // still failed after retry
            if (res.getStatus() / 100 > 2) {
                success &= false;
                try {
                    errMsg += d.getIp() + ":" + IOUtils.inputStreamStringify((InputStream) res.getEntity()) + ";";
                } catch (IOException e) {
                    errMsg += d.getIp() + ":" + "Unable to parse the response entity.";
                }
            }
            if (!success)
                throw new Exception(errMsg);
        }
    }

    private CertificateDo pickCert(Long vsId, boolean state) throws Exception {
        VirtualServer vs = virtualServerRepository.getById(vsId);
        String[] searchRange = getDomainSearchRange(vs.getDomains());
        CertificateDo value;
        if (searchRange.length == 1) {
            List<CertificateDo> result = certificateDao.findByDomainAndState(searchRange, state, CertificateEntity.READSET_FULL);
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
            if ((value = check.get(searchRange[searchRange.length - 1])) == null) {
                if (check.values().size() > 1)
                    throw new ValidationException("Multiple certificates found referring the domain list.");
                value = check.values().iterator().next();
            }
        }
        return value;
    }

    private String[] getDomainSearchRange(List<Domain> range) {
        String groupKey = "";
        List<String> values = new ArrayList<>();
        for (Domain domain : range) {
            groupKey += (domain.getName() + ",");
            values.add(domain.getName());
        }
        if (values.size() == 0)
            return new String[0];
        if (values.size() == 1)
            return new String[]{values.get(0)};
        values.add(groupKey);
        return values.toArray(new String[values.size()]);
    }

    private static class CertSyncClient extends AbstractRestClient {
        protected CertSyncClient(String url) {
            super(url);
        }

        public Response requestInstall(Long vsId, Long certId) throws ValidationException {
            return getTarget().queryParam("vsId", vsId).queryParam("certId", certId).request().get();
        }
    }
}
