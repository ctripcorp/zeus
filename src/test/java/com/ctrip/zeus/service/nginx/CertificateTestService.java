package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dal.core.RCertificateSlbServerDao;
import com.ctrip.zeus.dal.core.RCertificateSlbServerEntity;
import com.ctrip.zeus.dal.core.RelCertSlbServerDo;
import com.ctrip.zeus.service.nginx.impl.CertificateServiceImpl;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/11/5.
 */
public class CertificateTestService extends CertificateServiceImpl {
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;
    @Resource
    private CertificateInstaller certificateInstaller;

    @Override
    public void install(Long vsId, List<String> ips, Long certId) throws Exception {
        List<RelCertSlbServerDo> dos = rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL);
        Set<String> check = new HashSet<>();
        for (RelCertSlbServerDo d : dos) {
            check.add(d.getIp() + "#" + vsId + "#" + d.getCertId());
        }
        for (String ip : ips) {
            if (check.contains(ip + "#" + vsId + "#" + certId))
                continue;
            certificateInstaller.localInstall(vsId, certId);
        }
    }
}