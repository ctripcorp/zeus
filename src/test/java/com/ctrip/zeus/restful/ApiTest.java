package com.ctrip.zeus.restful;

import com.ctrip.zeus.client.GroupClient;
import com.ctrip.zeus.client.SlbClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.server.SlbAdminServer;
import com.ctrip.zeus.util.IOUtils;
import com.ctrip.zeus.util.ModelAssert;
import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.*;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.MysqlDbServer;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    private final String host = "http://127.0.0.1:8099";
    private final SlbClient sc = new SlbClient(host);
    private final GroupClient gc = new GroupClient(host);

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
        final String slbName = "default";
        final int total = 6;
        Slb orig = generateSlb(slbName);
        sc.add(orig);

        ExecutorService es = Executors.newFixedThreadPool(total);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            final int num = i;
            futures.add(es.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Slb slb = sc.get(slbName);
                        slb.addSlbServer(new SlbServer().setHostName("slbupd" + num).setIp("192.168.11." + num));
                        Response updResponse = sc.update(slb);
                        if (updResponse.getStatus() == 200)
                            break;
                    }
                }
            }));
        }
        for (Future f : futures)
            f.get();
        es.shutdown();
        int base = orig.getSlbServers().size();
        Slb upd = sc.get(slbName);
        Assert.assertEquals(base + total, upd.getSlbServers().size());
    }

    @Test
    public void testSlb() {
        String slbName = "default";
        Slb s = generateSlb(slbName);
        sc.add(s);
        Slb sc2 = sc.get(slbName);
        Assert.assertEquals(s.getVersion().intValue() + 1, sc2.getVersion().intValue());
        ModelAssert.assertSlbEquals(s, sc2);
    }

    @Test
    public void testGroup() {
        String slbName = "default";
        Slb s = generateSlb(slbName);
        sc.add(s);
        s = sc.get(slbName);
        String appName = "testGroup";
        Group app = new Group();
        app.setName(appName)
                .setAppId("999999").setVersion(0)
                .setHealthCheck(new HealthCheck().setFails(5).setIntervals(50).setPasses(2).setUri("/hotel"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupServer(new GroupServer().setIp("192.168.20.1").setHostName("slb001a")
                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
                .addGroupServer(new GroupServer().setIp("192.168.20.2").setHostName("slb001a")
                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setPath("/hotel").setVirtualServer(new VirtualServer().setId(s.getVirtualServers().get(0).getId())));
        gc.add(app);
        Group app2 = gc.get(appName);
        // set virtual server full information
        app.getGroupVirtualServers().get(0).setVirtualServer(s.getVirtualServers().get(0));
        app.getGroupVirtualServers().get(0).setPriority(1000);
        Assert.assertEquals(1, app2.getVersion().intValue());
        ModelAssert.assertGroupEquals(app, app2);
    }

    @After
    public void clearDb() throws Exception {
        deleteGroups();
        deleteSlb();
    }

    private void deleteGroups() {
        List<Group> l = gc.getAll();
        for (Group group : l) {
            gc.delete(group.getId());
        }
        Assert.assertEquals(0, gc.getAll().size());
    }

    private void deleteSlb() {
        List<Slb> l = sc.getAll();
        for (Slb slb : l) {
            Response r = sc.delete(slb.getId());
            try {
                System.out.println(IOUtils.inputStreamStringify((InputStream)r.getEntity()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Assert.assertEquals(0, sc.getAll().size());
    }

    private Slb generateSlb(String slbName) {
        return new Slb().setName(slbName).setNginxBin("/usr/local/nginx/bin").setNginxConf("/usr/local/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addVip(new Vip().setIp("192.168.1.3"))
                .addVip(new Vip().setIp("192.168.1.6"))
                .addSlbServer(new SlbServer().setHostName("slb001a").setIp("1110.1"))
                .addSlbServer(new SlbServer().setHostName("slb003").setIp("192.168.10.3"))
                .addVirtualServer(new VirtualServer().setName("vs002").setPort("80").setSsl(false)
                        .addDomain(new Domain().setName("hotel.ctrip.com")))
                .addVirtualServer(new VirtualServer().setName("vs003").setPort("80").setSsl(false)
                        .addDomain(new Domain().setName("m.ctrip.com"))
                        .addDomain(new Domain().setName("m2.ctrip.com")))
                .setStatus("TEST");
    }
}
