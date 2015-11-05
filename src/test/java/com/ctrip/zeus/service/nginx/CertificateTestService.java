package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.dal.core.RCertificateSlbServerDao;
import com.ctrip.zeus.dal.core.RCertificateSlbServerEntity;
import com.ctrip.zeus.dal.core.RelCertSlbServerDo;
import com.ctrip.zeus.service.nginx.impl.CertificateServiceImpl;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2015/11/5.
 */
public class CertificateTestService extends CertificateServiceImpl {
    @Resource
    private RCertificateSlbServerDao rCertificateSlbServerDao;
    @Resource
    private CertificateInstaller certificateInstaller;

    @Override
    public void install(Long vsId) throws Exception {
        List<RelCertSlbServerDo> dos = rCertificateSlbServerDao.findByVs(vsId, RCertificateSlbServerEntity.READSET_FULL);
        for (RelCertSlbServerDo d : dos) {
            if (d.getCertId() == d.getCommand())
                continue;
            certificateInstaller.localInstall(vsId, d.getCommand());
        }
    }
}