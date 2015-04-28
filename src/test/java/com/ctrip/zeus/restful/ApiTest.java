package com.ctrip.zeus.restful;

import com.ctrip.zeus.client.AppClient;
import com.ctrip.zeus.client.SlbClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.server.SlbAdminServer;
import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.MysqlDbServer;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class ApiTest {

    static SlbAdminServer server;
    static MysqlDbServer mysqlDbServer;

    @BeforeClass
    public static void setup() throws Exception {
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();

        S.setPropertyDefaultValue("archaius.deployment.applicationId", "slb-admin");
        S.setPropertyDefaultValue("archaius.deployment.environment", "local");
        S.setPropertyDefaultValue("server.www.base-dir", new File("").getAbsolutePath() + "/src/main/www");
        S.setPropertyDefaultValue("server.temp-dir", new File("").getAbsolutePath() + "/target/temp");
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        server = new SlbAdminServer();
        server.start();

    }

    @AfterClass
    public static void teardown() throws InterruptedException, ComponentLifecycleException, ComponentLookupException {
        server.close();
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }

    @Test
    public void testConcurrentUpdate() throws ExecutionException, InterruptedException {
        final SlbClient c = new SlbClient("http://127.0.0.1:8099");
        final String slbName = "default";
        final int total = 6;
        Slb orig = generateSlb(slbName);
        c.add(orig);

        ExecutorService es = Executors.newFixedThreadPool(total);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            final int num = i;
            futures.add(es.submit(new Runnable() {
                @Override
                public void run() {
                    while(!tryUpdate())
                        continue;
                }

                private boolean tryUpdate() {
                    Slb slb = c.get(slbName);
                    slb.addSlbServer(new SlbServer().setHostName("slbupd" + num).setIp("192.168.11." + num).setEnable(false));
                    Response updResponse = c.update(slb);
                    return updResponse.getStatus() == 200;
                }
            }));
        }
        for (Future f : futures)
            f.get();
        es.shutdown();
        int base = orig.getSlbServers().size();
        Slb upd = c.get(slbName);
        Assert.assertEquals(base + total, upd.getSlbServers().size());
    }

    @Test
    public void testSlb() {
        System.out.println("###########################test1");

        SlbClient c = new SlbClient("http://127.0.0.1:8099");
        c.getAll();

        String slbName = "default";

        Slb sc = generateSlb(slbName);
        c.add(sc);

        Slb sc2 = c.get(slbName);

        Assert.assertEquals(sc.setVersion(sc2.getVersion()), sc2);

    }

    @Test
    public void testApp() {
        System.out.println("###########################test2");

        SlbClient s = new SlbClient("http://127.0.0.1:8099");
        String slbName = "default";
        Slb sc = generateSlb(slbName);
        s.add(sc);

        AppClient c = new AppClient("http://127.0.0.1:8099");
        c.getAll();

        String appName = "testApp";

        App app = new App();
        app.setName(appName)
                .setAppId("999999").setVersion(0)
                .setHealthCheck(new HealthCheck().setFails(5).setIntervals(50).setPasses(2).setUri("/hotel"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addAppServer(new AppServer().setIp("192.168.20.1").setHostName("slb001a")
                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
                .addAppServer(new AppServer().setIp("192.168.20.2").setHostName("slb001a")
                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
                .addAppSlb(new AppSlb().setSlbName("default").setVirtualServer(new VirtualServer().setName("vs002").setPort("80")
                        .setSsl(false).addDomain(new Domain().setName("hotel.ctrip.com"))).setPath("/hotel"))
        ;
        c.add(app);

        App app2 = c.get(appName);
        Assert.assertEquals(app.setVersion(1), app2);
    }

    private Slb generateSlb(String slbName) {
        return new Slb().setName(slbName).setNginxBin("/usr/local/nginx/bin").setNginxConf("/usr/local/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addVip(new Vip().setIp("192.168.1.3"))
                .addVip(new Vip().setIp("192.168.1.6"))
                .addSlbServer(new SlbServer().setHostName("slb001a").setIp("1110.1").setEnable(true))
                .addSlbServer(new SlbServer().setHostName("slb003").setIp("192.168.10.3").setEnable(true))
                .addVirtualServer(new VirtualServer().setName("vs002").setPort("80").setSsl(false)
                        .addDomain(new Domain().setName("hotel.ctrip.com")))
                .addVirtualServer(new VirtualServer().setName("vs003").setPort("80").setSsl(false)
                        .addDomain(new Domain().setName("m.ctrip.com"))
                        .addDomain(new Domain().setName("m2.ctrip.com")))
                .setStatus("TEST");
    }
}
