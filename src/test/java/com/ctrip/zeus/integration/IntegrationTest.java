package com.ctrip.zeus.integration;

import com.ctrip.zeus.ao.ReqClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.service.ModelServiceTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by fanqq on 2015/4/10.
 */
public class IntegrationTest {

    private static final String host = "http://10.2.25.83:8099";

    //    private static final String host = "http://127.0.0.1:8099";
    private static final String hostip = "10.2.25.83";
    private static final String slb1_server_0 = "10.2.25.83";
    private static final String slb1_server_1 = "10.2.27.21";
    private static final String slb1_server_2 = "10.2.25.96";
    private static final String slb1_name = "__Test_slb1";
    private static final String slb2_name = "__Test_slb2";


    @Before
    public void before() {
        for (int i = 1; i < 11; i++) {
            new ReqClient(host + "/api/app/delete?appName=__Test_app" + i).getstr();
        }

        new ReqClient(host + "/api/slb/delete?slbName=" + slb1_name).getstr();
        new ReqClient(host + "/api/slb/delete?slbName=" + slb2_name).getstr();
    }

    @After
    public void after() {

        for (int i = 1; i < 11; i++) {
            new ReqClient(host + "/api/app/delete?appName=__Test_app" + i).getstr();
        }

        new ReqClient(host + "/api/slb/delete?slbName=" + slb1_name).getstr();
        new ReqClient(host + "/api/slb/delete?slbName=" + slb2_name).getstr();
    }

    @Test
    public void integrationTest() throws IOException {

        VirtualServer v1 = new VirtualServer().setName("__Test_vs1").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs1.ctrip.com"));
        VirtualServer v2 = new VirtualServer().setName("__Test_vs2").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs2.ctrip.com"))
                .addDomain(new Domain().setName("vs6.ctrip.com"));
        VirtualServer v3 = new VirtualServer().setName("__Test_vs3").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs3.ctrip.com"));
        VirtualServer v4 = new VirtualServer().setName("__Test_vs4").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs4.ctrip.com"));
        VirtualServer v5 = new VirtualServer().setName("__Test_vs5").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs5.ctrip.com"));


        AppServer appServer1 = new AppServer().setPort(80).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver1").setIp(slb1_server_0);
        AppServer appServer2 = new AppServer().setPort(80).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver2").setIp(slb1_server_1);
        AppServer appServer3 = new AppServer().setPort(80).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver3").setIp(slb1_server_2);


        //add slb and vs
        Slb slb1 = new Slb().setName(slb1_name).addVip(new Vip().setIp(slb1_server_0)).setNginxBin("/usr/local/nginx/bin")
                .setNginxConf("/usr/local/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addSlbServer(new SlbServer().setHostName("slb1_server_0").setIp(slb1_server_0).setEnable(true))
                .addSlbServer(new SlbServer().setHostName("slb1_server_1").setIp(slb1_server_1).setEnable(true))
                .addVirtualServer(v1)
                .addVirtualServer(v2)
                .addVirtualServer(v3)
                .addVirtualServer(v4)
                .addVirtualServer(v5)
                .setStatus("Test");

        Slb slb2 = new Slb().setName(slb2_name).addVip(new Vip().setIp(slb1_server_2)).setNginxBin("/usr/local/nginx/bin")
                .setNginxConf("/usr/local/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addSlbServer(new SlbServer().setHostName("slb1_server_2").setIp(slb1_server_2).setEnable(true))
                .addVirtualServer(v1)
                .addVirtualServer(v2)
                .addVirtualServer(v3)
                .addVirtualServer(v4)
                .addVirtualServer(v5)
                .setStatus("Test");


        new ReqClient(host).post("/api/slb/add", String.format(Slb.JSON, slb1));
        new ReqClient(host).post("/api/slb/add", String.format(Slb.JSON, slb2));


        //assert slb1 slb2
        boolean suc1 = new ReqClient(host + "/api/slb").getstr().contains(slb1_name);
        boolean suc2 = new ReqClient(host + "/api/slb").getstr().contains(slb2_name);

        Assert.assertEquals(true, suc1 && suc2);


        String slb1_res = new ReqClient(host + "/api/slb/get/" + slb1_name).getstr();
        Slb slb1_res_obj = DefaultJsonParser.parse(Slb.class, slb1_res);

        ModelServiceTest.assertSlbEquals(slb1, slb1_res_obj);

        String slb2_res = new ReqClient(host + "/api/slb/get/" + slb2_name).getstr();
        Slb slb2_res_obj = DefaultJsonParser.parse(Slb.class, slb2_res);

        ModelServiceTest.assertSlbEquals(slb2, slb2_res_obj);


        //activate test slbs
        new ReqClient(host + "/api/conf/activate?slbName=__Test_slb1&slbName=__Test_slb2").get();


        //add apps

        //app1 v1 appserver1 appserver2
        String appname = "__Test_app1";
        String slbname = "__Test_slb1";
        String appid = "000001";
        String appslbpath = "/app1";

        App app1 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v1))
                .addAppServer(appServer1)
                .addAppServer(appServer2);


        //app2 v2 appserver1
        appname = "__Test_app2";
        slbname = "__Test_slb1";
        appid = "000002";
        appslbpath = "/app2";


        App app2 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v2))
                .addAppServer(appServer1);


        //app3 v3 appserver3
        appname = "__Test_app3";
        slbname = "__Test_slb1";
        appid = "000003";
        appslbpath = "/app3";


        App app3 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v3))
                .addAppServer(appServer3);


        //app4 v4 appserver2
        appname = "__Test_app4";
        slbname = "__Test_slb1";
        appid = "000004";
        appslbpath = "/app4";


        App app4 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v4))
                .addAppServer(appServer2);


        //app5 v5 appserver1
        appname = "__Test_app5";
        slbname = "__Test_slb1";
        appid = "000005";
        appslbpath = "/app5";


        App app5 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v5))
                .addAppServer(appServer1);


        //app6 v3 appserver2
        appname = "__Test_app6";
        slbname = "__Test_slb2";
        appid = "000006";
        appslbpath = "/app6";


        App app6 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v3))
                .addAppServer(appServer2);

        //app7 v2 appserver1
        appname = "__Test_app7";
        slbname = "__Test_slb2";
        appid = "000007";
        appslbpath = "/app7";


        App app7 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v2))
                .addAppServer(appServer1);


        //app8 v2 v5 appserver3
        appname = "__Test_app8";
        slbname = "__Test_slb2";
        String slbname1 = "__Test_slb1";
        appid = "000008";
        appslbpath = "/app8";


        App app8 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v2))
                .addAppSlb(new AppSlb().setSlbName(slbname1).setPath(appslbpath)
                        .setVirtualServer(v5))
                .addAppServer(appServer3);


        //app9 v1 v3 appserver1 appserver2
        appname = "__Test_app9";
        slbname = "__Test_slb2";
        slbname1 = "__Test_slb1";
        appid = "000008";
        appslbpath = "/app8";


        App app9 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v1))
                .addAppSlb(new AppSlb().setSlbName(slbname1).setPath(appslbpath)
                        .setVirtualServer(v3))
                .addAppServer(appServer1)
                .addAppServer(appServer2);

        appname = "__Test_app10";
        slbname = "__Test_slb2";
        slbname1 = "__Test_slb1";
        appid = "000010";
        appslbpath = "/app10";


        //app10 v2 v5 appserver3
        App app10 = new App().setName(appname).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addAppSlb(new AppSlb().setSlbName(slbname).setPath(appslbpath)
                        .setVirtualServer(v2))
                .addAppSlb(new AppSlb().setSlbName(slbname1).setPath(appslbpath)
                        .setVirtualServer(v5))
                .addAppServer(appServer3);


        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app1));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app2));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app3));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app4));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app5));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app6));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app7));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app8));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app9));
        new ReqClient(host).post("/api/app/add", String.format(App.JSON, app10));

        String apps = new ReqClient(host + "/api/app").getstr();

        boolean appsuc = apps.contains("\"__Test_app1\"") && apps.contains("\"__Test_app2\"") && apps.contains("\"__Test_app3\"")
                && apps.contains("\"__Test_app4\"") && apps.contains("\"__Test_app5\"") && apps.contains("\"__Test_app6\"")
                && apps.contains("\"__Test_app7\"") && apps.contains("\"__Test_app8\"") && apps.contains("\"__Test_app9\"")
                && apps.contains("\"__Test_app10\"");

        App appres = null;
        String appstr = null;

        appstr = new ReqClient(host + "/api/app/get/__Test_app1").getstr();
        appres = DefaultJsonParser.parse(App.class, appstr);
        ModelServiceTest.assertAppEquals(app1, appres);

        appstr = new ReqClient(host + "/api/app/get/__Test_app2").getstr();
        appres = DefaultJsonParser.parse(App.class, appstr);
        ModelServiceTest.assertAppEquals(app2, appres);

        appstr = new ReqClient(host + "/api/app/get/__Test_app9").getstr();
        appres = DefaultJsonParser.parse(App.class, appstr);
        ModelServiceTest.assertAppEquals(app9, appres);

        appstr = new ReqClient(host + "/api/app/get/__Test_app10").getstr();
        appres = DefaultJsonParser.parse(App.class, appstr);
        ModelServiceTest.assertAppEquals(app10, appres);


        new ReqClient(host + "/api/conf/activate?appName=__Test_app1").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app2").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app3").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app4").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app5").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app6").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app7").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app8").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app9").get();
        new ReqClient(host + "/api/conf/activate?appName=__Test_app10").get();

        for (int i = 1; i < 11; i++) {
            String appstatus = new ReqClient(host + "/api/status/app/__Test_app" + i).getstr();
            AppStatusList appStatusList = DefaultJsonParser.parse(AppStatusList.class, appstatus);

            for (AppStatus as : appStatusList.getAppStatuses()) {
                Assert.assertEquals("__Test_app" + i, as.getAppName());
                Assert.assertEquals(true, as.getSlbName().equals(slb1_name) || as.getSlbName().equals(slb2_name));

                for (AppServerStatus ass : as.getAppServerStatuses()) {
                    Assert.assertEquals(true, ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1) || ass.getIp().equals(slb1_server_2));
                    Assert.assertEquals(true, ass.getServer());
                    Assert.assertEquals(true, ass.getMember());
                }
            }
        }


        new ReqClient(host + "/api/op/downServer?ip=" + slb1_server_1).get();
        new ReqClient(host + "/api/op/downServer?ip=" + slb1_server_0).get();

        String slbstatus = new ReqClient(host + "/api/status/slb/" + slb1_name).getstr();

        AppStatusList appStatusList = DefaultJsonParser.parse(AppStatusList.class, slbstatus);

        for (AppStatus as : appStatusList.getAppStatuses()) {
            for (AppServerStatus ass : as.getAppServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(false, ass.getServer());
                }
            }
        }

        slbstatus = new ReqClient(host + "/api/status/slb/" + slb2_name).getstr();

        appStatusList = DefaultJsonParser.parse(AppStatusList.class, slbstatus);

        for (AppStatus as : appStatusList.getAppStatuses()) {
            for (AppServerStatus ass : as.getAppServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(false, ass.getServer());
                }
            }
        }


        new ReqClient(host + "/api/op/upServer?ip=" + slb1_server_0).get();
        new ReqClient(host + "/api/op/upServer?ip=" + slb1_server_1).get();


        slbstatus = new ReqClient(host + "/api/status/slb/" + slb2_name).getstr();

        appStatusList = DefaultJsonParser.parse(AppStatusList.class, slbstatus);

        for (AppStatus as : appStatusList.getAppStatuses()) {
            for (AppServerStatus ass : as.getAppServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(true, ass.getServer());
                }
            }
        }

        slbstatus = new ReqClient(host + "/api/status/slb/" + slb1_name).getstr();

        appStatusList = DefaultJsonParser.parse(AppStatusList.class, slbstatus);

        for (AppStatus as : appStatusList.getAppStatuses()) {
            for (AppServerStatus ass : as.getAppServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(true, ass.getServer());
                }
            }
        }


        new ReqClient(host + "/api/op/downMember?ip=" + slb1_server_2 + "&appName=__Test_app3").get();

        String appstatus = new ReqClient(host + "/api/status/app/__Test_app3").getstr();

        appStatusList = DefaultJsonParser.parse(AppStatusList.class, appstatus);

        for (AppStatus as : appStatusList.getAppStatuses()) {
            for (AppServerStatus ass : as.getAppServerStatuses()) {
                if (ass.getIp().equals(slb1_server_2)) {
                    Assert.assertEquals(false, ass.getMember());
                }
            }
        }


        new ReqClient(host + "/api/op/upMember?ip=" + slb1_server_2 + "&appName=__Test_app3").get();


        appstatus = new ReqClient(host + "/api/status/app/__Test_app3").getstr();

        appStatusList = DefaultJsonParser.parse(AppStatusList.class, appstatus);

        for (AppStatus as : appStatusList.getAppStatuses()) {
            for (AppServerStatus ass : as.getAppServerStatuses()) {
                if (ass.getIp().equals(slb1_server_2)) {
                    Assert.assertEquals(true, ass.getMember());
                }
            }
        }
    }

}
