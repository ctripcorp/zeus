package com.ctrip.zeus.restful;

import com.ctrip.zeus.client.SlbClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.server.SlbAdminServer;
import com.ctrip.zeus.util.S;
import org.junit.AfterClass;
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
        sc.setName("default").setNginxBin("nginx").setNginxConf("/usr/local/nginx/conf").setNginxWorkerProcesses(1)
                .addVip(new Vip().setIp("192.168.1.1"))
                .addVip(new Vip().setIp("192.168.1.2"))
                .addSlbServer(new SlbServer().setHostName("slb001").setIp("192.168.10.1"))
                .addSlbServer(new SlbServer().setHostName("slb002").setIp("192.168.10.2"))
                .addVirtualServer(new VirtualServer().setName("vs001").setSsl(false)
                        .addDomain(new Domain().setName("m.ctrip.com").setPort(80))
                        .addDomain(new Domain().setName("m2.ctrip.com").setPort(80)))
                .addVirtualServer(new VirtualServer().setName("vs002").setSsl(false).addDomain(new Domain().setName("hotel.ctrip.com").setPort(80)));

        c.add(sc);

    }
}
