package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

import java.io.InputStream;
import java.util.Set;

/**
 * Created by zhoumy on 2016/11/10.
 */
public class CertificateServiceTest extends AbstractServerTest {
    @Resource
    private CertificateService certificateService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    @Test
    public void update() throws Exception {
        Long onBoardId = certificateService.upload(getCert(), getKey(), "testUpdate.test.com", CertificateConfig.GRAYSCALE);
        try {
            certificateService.getCertificateOnBoard("testUpdate.test.com");
            Assert.assertTrue(false);
        } catch (ValidationException ex) {
        }

        certificateService.update(onBoardId, CertificateConfig.ONBOARD);
        Long pickedId = certificateService.getCertificateOnBoard("testUpdate.test.com");
        Assert.assertEquals(onBoardId, pickedId);

        certificateService.update(onBoardId, CertificateConfig.GRAYSCALE);
        try {
            certificateService.getCertificateOnBoard("testUpdate.test.com");
            Assert.assertTrue(false);
        } catch (ValidationException ex) {
        }
    }

    @Test
    public void upload() throws Exception {
        Long onBoardId = certificateService.upload(getCert(), getKey(), "testUpload.test.com", CertificateConfig.ONBOARD);
        Long noiseId = certificateService.upload(getCert(), getKey(), "testUpload.test2.noise.com", CertificateConfig.ONBOARD);

        Long pickedId = certificateService.getCertificateOnBoard("testUpload.test.com");
        Assert.assertEquals(onBoardId, pickedId);
        Assert.assertNotEquals(onBoardId, noiseId);
    }

    @Test
    public void upgrade() throws Exception {
        Long onBoardId = certificateService.upload(getCert(), getKey(), "testUpgrade.test.com", CertificateConfig.ONBOARD);
        Long newOnBoardId = certificateService.upgrade(getCert(), getKey(), "testUpgrade.test.com", CertificateConfig.ONBOARD);
        Assert.assertNotEquals(onBoardId, newOnBoardId);

        Long pickedId = certificateService.getCertificateOnBoard("testUpgrade.test.com");
        Assert.assertEquals(newOnBoardId, pickedId);
    }

    @Test
    public void install() throws Exception {
        Slb slb = new Slb().setName("testInstallCertificate").setVersion(1).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("127.0.0.1").setHostName("LOCALHOST"));
        slb = slbRepository.add(slb);

        VirtualServer vs = new VirtualServer().setName("testInstallCertificate.com_443").setSsl(true).setPort("443")
                .addDomain(new Domain().setName("testInstallCertificate.com"));
        vs.getSlbIds().add(slb.getId());

        try {
            virtualServerRepository.add(vs);
            Assert.assertFalse(true);
        } catch (ValidationException ex) {
        }


        Long certId = certificateService.upload(getCert(), getKey(), "testInstallCertificate.com", CertificateConfig.ONBOARD);

        virtualServerRepository.add(vs);
        Assert.assertNotNull(virtualServerRepository.getById(vs.getId()));

        Assert.assertTrue(certificateService instanceof CertificateTestService);
        Set<String> ips = ((CertificateTestService) certificateService).getInstalledSlbServers(certId);
        for (SlbServer slbServer : slb.getSlbServers()) {
            ips.contains(slbServer.getIp());
        }

        virtualServerRepository.delete(vs.getId());
        slbRepository.delete(slb.getId());
    }

    @Test
    public void replaceAndInstall() throws Exception {
        Long onBoardId = certificateService.upload(getCert(), getKey(), "testReplaceAndInstall.test.com", CertificateConfig.ONBOARD);
        Slb slb = new Slb().setName("testReplaceAndInstall").setVersion(1).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("127.0.0.1").setHostName("LOCALHOST"));
        slb = slbRepository.add(slb);
        VirtualServer vs = new VirtualServer().setName("testReplaceAndInstall.com_443").setSsl(true).setPort("443")
                .addDomain(new Domain().setName("testReplaceAndInstall.test.com"));
        vs.getSlbIds().add(slb.getId());
        virtualServerRepository.add(vs);


        Long newDomainOnBoardId = certificateService.upload(getCert(), getKey(), "testReplaceAndInstall.test.com|testReplaceAndInstall.2.test.com", CertificateConfig.ONBOARD);
        vs.getDomains().add(new Domain().setName("testReplaceAndInstall.2.test.com"));
        virtualServerRepository.update(vs);

        Assert.assertTrue(certificateService instanceof CertificateTestService);
        Assert.assertEquals(0, ((CertificateTestService) certificateService).getInstalledSlbServers(onBoardId).size());
        Set<String> ips = ((CertificateTestService) certificateService).getInstalledSlbServers(newDomainOnBoardId);
        for (SlbServer slbServer : slb.getSlbServers()) {
            ips.contains(slbServer.getIp());
        }


        Long upgradedDomainOnBoardId = certificateService.upgrade(getCert(), getKey(), "testReplaceAndInstall.2.test.com|testReplaceAndInstall.test.com", CertificateConfig.ONBOARD);
        certificateService.replaceAndInstall(newDomainOnBoardId, upgradedDomainOnBoardId);

        Assert.assertEquals(0, ((CertificateTestService) certificateService).getInstalledSlbServers(onBoardId).size());
        Assert.assertEquals(0, ((CertificateTestService) certificateService).getInstalledSlbServers(newDomainOnBoardId).size());
        ips = ((CertificateTestService) certificateService).getInstalledSlbServers(upgradedDomainOnBoardId);
        for (SlbServer slbServer : slb.getSlbServers()) {
            ips.contains(slbServer.getIp());
        }

        virtualServerRepository.delete(vs.getId());
        slbRepository.delete(slb.getId());
    }

    private InputStream getCert() {
        return CertificateServiceTest.class.getClassLoader().getResourceAsStream("com.ctrip.zeus.service/nginx/CertTest.crt");
    }

    private InputStream getKey() {
        return CertificateServiceTest.class.getClassLoader().getResourceAsStream("com.ctrip.zeus.service/nginx/CertTest.key");
    }
}