package com.ctrip.zeus.service;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * Created by zhoumy on 2015/11/5.
 */
public class CertificateServiceTest extends AbstractServerTest {
    @Resource
    private CertificateService certificateService;
    @Resource
    private SlbRepository slbRepository;

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
        Slb tmpSlb = new Slb().setName("testInstallCertificate").setVersion(1).setStatus("TEST")
                .addVip(new Vip().setIp("127.0.0.1"))
                .addSlbServer(new SlbServer().setIp("127.0.0.1").setHostName("LOCALHOST"))
                .addVirtualServer(new VirtualServer().setName("www.testInstallCertificate.com_443").setSsl(true).setPort("443")
                        .addDomain(new Domain().setName("www.testInstallCertificate.com")));
        try {
            tmpSlb = slbRepository.add(tmpSlb);
            Assert.assertFalse(true);
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof ValidationException);
            System.out.println(ex.getMessage());
        }

        InputStream cert = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/CertTest.crt");
        InputStream key = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/CertTest.key");
        certificateService.upload(cert, key, "www.testInstallCertificate.com", CertificateConfig.ONBOARD);
        tmpSlb = slbRepository.add(tmpSlb);
        Assert.assertNotNull(slbRepository.getById(tmpSlb.getId()));
    }
}
