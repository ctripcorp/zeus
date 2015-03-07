package com.ctrip.zeus.restful;

import com.ctrip.zeus.client.AppClient;
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

        String slbName = "default";

        Slb sc = new Slb();
        sc.setName(slbName).setNginxBin("/usr/local/nginx/bin").setNginxConf("/usr/local/nginx/conf").setNginxWorkerProcesses(1)
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

        Slb sc2 = c.get(slbName);

        Assert.assertEquals(sc, sc2);

    }

    @Test
    public void testApp() {
        AppClient c = new AppClient("http://127.0.0.1:8099");
        c.getAll();

        String appName = "testApp";

        App app = new App();
        app.setName(appName)
                .setAppId("999999")
                .setHealthCheck(new HealthCheck().setFails(5).setInterval(50).setPasses(2).setUri("/hotel"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addAppServer(new AppServer().setServer(new Server().setIp("192.168.20.1").setHostName("app001").setUp(true))
                        .setEnable(true).setFailTimeout(30).setHealthy(true).setMaxFails(2).setPort(80).setWeight(2))
                .addAppServer(new AppServer().setServer(new Server().setIp("192.168.20.2").setHostName("app002").setUp(true))
                        .setEnable(true).setFailTimeout(30).setHealthy(true).setMaxFails(2).setPort(80).setWeight(2))
                .addAppSlb(new AppSlb().setSlbName("default").setVirtualServer(new VirtualServer().setName("vs002")).setPath("/hotel"))
        ;
        c.add(app);

//        App app2 = c.get(appName);
//        Assert.assertEquals(app, app2);

    }
}
