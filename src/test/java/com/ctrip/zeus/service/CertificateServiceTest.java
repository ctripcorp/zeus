package com.ctrip.zeus.service;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.service.nginx.CertificateTestService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by zhoumy on 2015/11/5.
 */
public class CertificateServiceTest extends AbstractServerTest {
    @Resource
    private CertificateService certificateService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;

    @Test
    public void testUploadCertificate() throws Exception {
        InputStream cert = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/CertTest.crt");
        InputStream key = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/CertTest.key");

        Long onBoardId = certificateService.upload(cert, key, "localhost", CertificateConfig.ONBOARD);
        Long grayscaleId = certificateService.upload(cert, key, "localhost", CertificateConfig.GRAYSCALE);
        Long noiseId = certificateService.upload(cert, key, "testUploadCertificate.noise.com", CertificateConfig.ONBOARD);

        Long pickedId = certificateService.getCertificateOnBoard(new String[]{"localhost"});

        Assert.assertFalse(onBoardId.longValue() == grayscaleId.longValue()
                || grayscaleId.longValue() == noiseId.longValue()
                || onBoardId.longValue() == noiseId.longValue());
        Assert.assertEquals(onBoardId, pickedId);
    }

    @Test
    public void testInstallCertificate() throws Exception {
        Slb slb = new Slb().setName("testInstallCertificate").setVersion(1).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("127.0.0.1").setHostName("LOCALHOST"));
        slb = slbRepository.add(slb);

        VirtualServer vs = new VirtualServer().setName("www.testInstallCertificate.com_443").setSsl(true).setPort("443")
                .addDomain(new Domain().setName("www.testInstallCertificate.com"));
        try {
            virtualServerRepository.add(vs);
            Assert.assertFalse(true);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof ValidationException);
            System.out.println(ex.getMessage());
        }

        InputStream cert = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/CertTest.crt");
        InputStream key = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/CertTest.key");
        Long certId = certificateService.upload(cert, key, "www.testInstallCertificate.com", CertificateConfig.ONBOARD);

        virtualServerRepository.add(vs);
        Assert.assertNotNull(virtualServerRepository.getById(vs.getId()));

        Assert.assertTrue(certificateService instanceof CertificateTestService);
        Set<String> ips = ((CertificateTestService) certificateService).getInstalledSlbServers(certId);
        for (SlbServer slbServer : slb.getSlbServers()) {
            ips.contains(slbServer.getIp());
        }
    }
}