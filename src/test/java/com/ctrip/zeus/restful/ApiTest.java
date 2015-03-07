package com.ctrip.zeus.restful;

import com.ctrip.zeus.client.SlbClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.server.SlbAdminServer;
import com.ctrip.zeus.util.S;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class ApiTest {

    private static SlbAdminServer server;

    @BeforeClass
    public static void setup() throws Exception {
        S.setPropertyDefaultValue("archaius.deployment.applicationId", "slb-admin");
        S.setPropertyDefaultValue("archaius.deployment.environment", "local");
        S.setPropertyDefaultValue("server.www.base-dir", new File("").getAbsolutePath() + "/src/main/www");
        S.setPropertyDefaultValue("server.temp-dir", new File("").getAbsolutePath() + "/target/temp");
        S.setPropertyDefaultValue("APP_HOME", new File("").getAbsolutePath());

        server = new SlbAdminServer();
        server.start();
    }

    @AfterClass
    public static void teardown() {
        server.close();
    }

    @Test
    public void testSlb() {
        SlbClient c = new SlbClient("http://127.0.0.1:8099");
        c.getAll();

        Slb sc = new Slb();
        sc.setName("default").setNginxBin("/usr/local/nginx/bin").setNginxConf("/usr/local/nginx/conf").setNginxWorkerProcesses(1)
                .addVip(new Vip().setIp("192.168.1.3"))
                .addVip(new Vip().setIp("192.168.1.6"))
                .addSlbServer(new SlbServer().setHostName("slb001a").setIp("192.168.10.1").setEnable(true))
                .addSlbServer(new SlbServer().setHostName("slb003").setIp("192.168.10.3").setEnable(true))
                .addVirtualServer(new VirtualServer().setName("vs002").setSsl(false)
                        .addDomain(new Domain().setName("hotel.ctrip.com").setPort(80)))
                .addVirtualServer(new VirtualServer().setName("vs003").setSsl(false)
                        .addDomain(new Domain().setName("m.ctrip.com").setPort(80))
                        .addDomain(new Domain().setName("m2.ctrip.com").setPort(80)))
        .setStatus("TEST");
        c.add(sc);

        Slb  sc2 = c.get("default");

        Assert.assertEquals(sc, sc2);

    }
}
