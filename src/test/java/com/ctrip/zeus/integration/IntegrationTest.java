//package com.ctrip.zeus.integration;
//
//import com.ctrip.zeus.ao.ReqClient;
//import com.ctrip.zeus.model.entity.*;
//import com.ctrip.zeus.model.transform.DefaultJsonParser;
//import com.ctrip.zeus.service.ModelServiceTest;
//import com.ctrip.zeus.support.GenericSerializer;
//import com.ctrip.zeus.util.ModelAssert;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.ws.rs.core.Response;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
//* Created by fanqq on 2015/4/10.
//*/
//public class IntegrationTest {
//
//    private static final String host = "http://10.2.25.83:8099";
//
////    private static final String host = "http://127.0.0.1:8099";
//    private static final String hostip = "10.2.25.83";
//    private static final String slb1_server_0 = "10.2.25.83";
//    private static final String slb1_server_1 = "10.2.25.95";
//    private static final String slb1_name = "__Test_slb1";
//    private static final String slb2_name = "__Test_slb2";
//    private static final int STATUS_OK = 200;
//    private static ReqClient reqClient = new ReqClient(host);
//
//
//    @Before
//    public void before() throws IOException {
//        clean();
//    }
//    private void clean()throws IOException{
//        Group groupres = null;
//        String groupstr = null;
//        StringBuilder sb = new StringBuilder(128);
//        for (int i = 0 ; i < 3000 ; i ++){
//            sb.append("groupName=__Test_app").append(i).append("&");
//        }
//        try{
//            reqClient.getstr("/api/deactivate/group?"+sb.toString());
//            reqClient.markPass("/api/deactivate/group");
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
//
//        for (int i = 0; i < 3000; i++) {
//            try {
//                groupstr = reqClient.getstr("/api/group?groupName=__Test_app"+i);
//            }catch (Exception e)
//            {
//                System.out.println(e);
//                continue;
//            }
//
//            groupres = DefaultJsonParser.parse(Group.class, groupstr);
//            if (groupres!=null)
//            {
//                reqClient.getstr("/api/group/delete?groupId=" + groupres.getId());
//                reqClient.markPass("/api/group/delete");
//            }
//        }
//        String slb_res = null;
//        try{
//            slb_res = reqClient.getstr("/api/slb?slbName=" + slb1_name);
//        }catch (Exception e){
//            System.out.println(e);
//        }
//        Slb slb_res_obj = null;
//        try {
//            slb_res_obj  = DefaultJsonParser.parse(Slb.class, slb_res);
//        }catch (Exception e){
//            System.out.println(e);
//        }
//
//        if (slb_res_obj!=null)
//        {
//            reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
//            reqClient.markPass("/api/slb/delete");
//        }
//
//        try{
//            slb_res = reqClient.getstr("/api/slb?slbName=" + slb2_name);
//        }catch (Exception e){
//            System.out.println(e);
//        }
//        try {
//            slb_res_obj  = DefaultJsonParser.parse(Slb.class, slb_res);
//        }catch (Exception e){
//            System.out.println(e);
//        }
//        if (slb_res_obj!=null)
//        {
//            reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
//        }
//    }
//    @After
//    public void after() throws IOException {
//        clean();
//        reqClient.buildReport();
//    }
//
//    @Test
//    public void integrationTest() throws IOException, InterruptedException {
//
//        VirtualServer v1 = new VirtualServer().setName("__Test_vs1").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("vs1.ctrip.com"));
//        VirtualServer v2 = new VirtualServer().setName("__Test_vs2").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("vs2.ctrip.com"))
//                .addDomain(new Domain().setName("vs6.ctrip.com"));
//        VirtualServer v3 = new VirtualServer().setName("__Test_vs3").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("vs3.ctrip.com"));
//        VirtualServer v4 = new VirtualServer().setName("__Test_vs4").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("vs4.ctrip.com"));
//        VirtualServer v5 = new VirtualServer().setName("__Test_vs5").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("vs5.ctrip.com"));
//
//
//        GroupServer groupServer1 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver1").setIp(slb1_server_0);
//        GroupServer groupServer2 = new GroupServer().setPort(1000).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver2").setIp(slb1_server_1);
//
//
//        //add slb and vs
//        Slb slb1 = new Slb().setName(slb1_name).addVip(new Vip().setIp(slb1_server_0)).setNginxBin("/opt/app/nginx/sbin")
//                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
//                .addSlbServer(new SlbServer().setHostName("slb1_server_0").setIp(slb1_server_0))
//                .addVirtualServer(v1)
//                .addVirtualServer(v2)
//                .addVirtualServer(v3)
//                .addVirtualServer(v4)
//                .addVirtualServer(v5)
//                .setStatus("Test");
//
//        Slb slb2 = new Slb().setName(slb2_name).addVip(new Vip().setIp(slb1_server_1)).setNginxBin("/opt/app/nginx/sbin")
//                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
//                .addSlbServer(new SlbServer().setHostName("slb1_server_1").setIp(slb1_server_1))
//                .addVirtualServer(v1)
//                .addVirtualServer(v2)
//                .addVirtualServer(v3)
//                .addVirtualServer(v4)
//                .addVirtualServer(v5)
//                .setStatus("Test");
//        reqClient.post("/api/slb/new", String.format(Slb.JSON, slb1));
//        reqClient.post("/api/slb/new", String.format(Slb.JSON, slb2));
//
//        String slb1_res = reqClient.getstr("/api/slb?slbName=" + slb1_name);
//        Slb slb1_res_obj = DefaultJsonParser.parse(Slb.class, slb1_res);
//        ModelAssert.assertSlbEquals(slb1, slb1_res_obj);
//        String slb2_res = reqClient.getstr("/api/slb?slbName=" + slb2_name);
//        Slb slb2_res_obj = DefaultJsonParser.parse(Slb.class, slb2_res);
//        ModelAssert.assertSlbEquals(slb2, slb2_res_obj);
//        reqClient.markPass("/api/slb/new");
//        reqClient.markPass("/api/slb");
//        //activate test slbs
//        reqClient.getstr("/api/activate/slb?slbName=__Test_slb1&slbName=__Test_slb2");
//
//        List<Group> groups = new ArrayList<>();
//        for (int i = 0 ; i < 3000 ; i++ )
//        {
//            Group group = new Group().setName("__Test_app" + i).setAppId("1000" + i).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
//                    .setIntervals(30000).setPasses(1).setUri("/status.json")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
//                    .setValue("test"))
//                .addGroupSlb(new GroupSlb().addVip(new Vip().setIp(hostip)).setSlbId(i % 3 == 0 ? slb2_res_obj.getId() : slb1_res_obj.getId())
//                        .setSlbName(i % 3 == 0 ? slb2_res_obj.getName() : slb1_res_obj.getName())
//                        .setPath("~* ^/app" + i).setVirtualServer(i % 3 == 0 ? slb2_res_obj.getVirtualServers().get(i%4) : slb1_res_obj.getVirtualServers().get(i%4)).setRewrite(i % 2 == 0 ? null : "\"/app\" /app0?sleep=1&size=1" + i)
//                        .setPriority(i)).addGroupServer(i % 2 == 0 ?groupServer1:groupServer2);
//            reqClient.post("/api/group/new", String.format(Group.JSON, group));
//            groups.add(group);
//        }
//        String apps = reqClient.getstr("/api/groups");
//        boolean appsuc = apps.contains("\"__Test_app1\"") && apps.contains("\"__Test_app2\"") && apps.contains("\"__Test_app3\"")
//                && apps.contains("\"__Test_app4\"") && apps.contains("\"__Test_app5\"") && apps.contains("\"__Test_app6\"")
//                && apps.contains("\"__Test_app7\"") && apps.contains("\"__Test_app8\"") && apps.contains("\"__Test_app9\"")
//                && apps.contains("\"__Test_app0\"");
//
//        Assert.assertEquals(true,appsuc);
//        reqClient.markPass("/api/group/new");
//        reqClient.markPass("/api/groups");
//
//
//        Group groupres = null;
//        String groupstr = null;
//
//        groupstr = reqClient.getstr("/api/group?groupName=__Test_app1");
//        groupres = DefaultJsonParser.parse(Group.class, groupstr);
//        ModelAssert.assertGroupEquals(groups.get(1), groupres);
//
//        groupstr = reqClient.getstr("/api/group?groupName=__Test_app2");
//        groupres = DefaultJsonParser.parse(Group.class, groupstr);
//        ModelAssert.assertGroupEquals(groups.get(2), groupres);
//
//        groupstr = reqClient.getstr("/api/group?groupName=__Test_app3");
//        groupres = DefaultJsonParser.parse(Group.class, groupstr);
//        ModelAssert.assertGroupEquals(groups.get(3), groupres);
//        reqClient.markPass("/api/group");
//
//        integrationTest_update();
//
//        StringBuilder sb = new StringBuilder(128);
//        for (int i = 0 ; i < 3000 ; i ++){
//            sb.append("groupName=__Test_app").append(i).append("&");
//            if (i%50==0)
//            {
//                reqClient.getstr("/api/activate/group?"+sb.toString());
//                sb.setLength(0);
//            }
//        }
//
//
//        for (int i = 0 ; i < 3000 ; i ++)
//        {
//            reqClient.get("/api/op/upMember?batch=true&groupName=__Test_app"+i);
//        }
//        reqClient.getstr("/api/op/upServer?ip=" + slb1_server_0);
//        reqClient.getstr("/api/op/upServer?ip=" + slb1_server_1);
//
//        for (int i = 0; i < 10; i++) {
//            String groupstatus = reqClient.getstr("/api/status/group?groupName=__Test_app" + i);
//            GroupStatus gs = DefaultJsonParser.parse(GroupStatus.class, groupstatus);
//
//            Assert.assertEquals("__Test_app" + i, gs.getGroupName());
//            Assert.assertEquals(true, gs.getSlbName().equals(slb1_name) || gs.getSlbName().equals(slb2_name));
//
//            for (GroupServerStatus ass : gs.getGroupServerStatuses()) {
//                Assert.assertEquals(true, ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1));
//                Assert.assertEquals(true, ass.getServer());
//                Assert.assertEquals(true, ass.getMember());
//                Assert.assertEquals(i%2==0, ass.getUp());
//            }
//            reqClient.markPass("/api/status/group");
//        }
//
//        reqClient.markPass("/api/activate/group");
//        reqClient.markPass("/api/activate/slb");
//        reqClient.getstr("/api/op/downServer?ip=" + slb1_server_1);
//        reqClient.getstr("/api/op/downServer?ip=" + slb1_server_0);
//        Thread.sleep(1000);
//        String slbstatus = reqClient.getstr("/api/status/groups?slbName=" + slb1_name);
//
//        GroupStatusList groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);
//
//        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
//            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
//                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
//                    Assert.assertEquals(false, ass.getServer());
//                    Assert.assertEquals(true, ass.getMember());
//                    Assert.assertEquals(false, ass.getUp());
//                }
//            }
//        }
//
//        slbstatus = reqClient.getstr("/api/status/groups?slbName=" + slb2_name);
//
//        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);
//
//        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
//            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
//                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
//                    Assert.assertEquals(false, ass.getServer());
//                    Assert.assertEquals(true, ass.getMember());
//                    Assert.assertEquals(false, ass.getUp());
//                }
//            }
//        }
//
//        reqClient.markPass("/api/status/groups");
//        reqClient.markPass("/api/op/downServer");
//        reqClient.getstr("/api/op/upServer?ip=" + slb1_server_0);
//        reqClient.getstr("/api/op/upServer?ip=" + slb1_server_1);
//        Thread.sleep(1000);
//
//        slbstatus = reqClient.getstr("/api/status/groups?slbName=" + slb2_name);
//
//        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);
//
//        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
//            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
//                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
//                    Assert.assertEquals(true, ass.getServer());
//                    Assert.assertEquals(true, ass.getMember());
//                    Assert.assertEquals(ass.getIp().equals(slb1_server_0), ass.getUp());
//                }
//            }
//        }
//
//        slbstatus = reqClient.getstr("/api/status/groups?slbName=" + slb1_name);
//        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);
//        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
//            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
//                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
//                    Assert.assertEquals(true, ass.getServer());
//                    Assert.assertEquals(true, ass.getMember());
//                    Assert.assertEquals(ass.getIp().equals(slb1_server_0), ass.getUp());
//                }
//            }
//        }
//
//        reqClient.markPass("/api/op/upServer");
//        reqClient.getstr("/api/op/downMember?ip=" + slb1_server_1 + "&groupName=__Test_app3");
//        Thread.sleep(1000);
//        String groupstatus = reqClient.getstr("/api/status/group?groupName=__Test_app3");
//        GroupStatus groupStatus = null;
//        groupStatus = DefaultJsonParser.parse(GroupStatus.class, groupstatus);
//
//        for (GroupServerStatus ass : groupStatus.getGroupServerStatuses()) {
//            if (ass.getIp().equals(slb1_server_1)) {
//                Assert.assertEquals(false, ass.getMember());
//                Assert.assertEquals(true, ass.getServer());
//                Assert.assertEquals(false, ass.getUp());
//
//            }
//        }
//
//        reqClient.markPass("/api/op/downMember");
//        reqClient.markPass("/api/status/group");
//
//        reqClient.getstr("/api/op/upMember?ip=" + slb1_server_1 + "&groupName=__Test_app3");
//        Thread.sleep(1000);
//        groupstatus = reqClient.getstr("/api/status/group?groupName=__Test_app3");
//        groupStatus = DefaultJsonParser.parse(GroupStatus.class, groupstatus);
//
//        for (GroupServerStatus ass : groupStatus.getGroupServerStatuses()) {
//            if (ass.getIp().equals(slb1_server_1)) {
//                Assert.assertEquals(true, ass.getMember());
//                Assert.assertEquals(true, ass.getServer());
//                Assert.assertEquals(false, ass.getUp());
//            }
//        }
//
//        reqClient.getstr("/api/op/downMember?ip=" + slb1_server_0 + "&groupName=__Test_app2");
//        Thread.sleep(1000);
//        groupstatus = reqClient.getstr("/api/status/group?groupName=__Test_app2");
//        groupStatus = DefaultJsonParser.parse(GroupStatus.class, groupstatus);
//
//        for (GroupServerStatus ass : groupStatus.getGroupServerStatuses()) {
//            if (ass.getIp().equals(slb1_server_0)) {
//                Assert.assertEquals(false, ass.getMember());
//                Assert.assertEquals(true, ass.getServer());
//                Assert.assertEquals(false, ass.getUp());
//
//            }
//        }
//        reqClient.getstr("/api/op/upMember?ip=" + slb1_server_0 + "&groupName=__Test_app2");
//        Thread.sleep(1000);
//        groupstatus = reqClient.getstr("/api/status/group?groupName=__Test_app2");
//        groupStatus = DefaultJsonParser.parse(GroupStatus.class, groupstatus);
//
//        for (GroupServerStatus ass : groupStatus.getGroupServerStatuses()) {
//            if (ass.getIp().equals(slb1_server_0)) {
//                Assert.assertEquals(true, ass.getMember());
//                Assert.assertEquals(true, ass.getServer());
//                Assert.assertEquals(true, ass.getUp());
//            }
//        }
//        reqClient.markPass("/api/op/upMember");
//    }
//
//    private void integrationTest_update() throws IOException {
//        final String app1_name = "__Test_app1";
//        final ReqClient c = new ReqClient(host);
//        String orig, upd;
//        Response res;
//
//        // test update slb1(__Test_slb1)
//        orig = c.getstr("/api/slb?slbName=" + slb1_name);
//        Slb origSlb = DefaultJsonParser.parse(Slb.class, orig);
//        origSlb.setNginxWorkerProcesses(origSlb.getNginxWorkerProcesses() + 127);
//        res = c.post("/api/slb/update", GenericSerializer.writeJson(origSlb));
//        Assert.assertEquals(STATUS_OK, res.getStatus());
//        upd = c.getstr("/api/slb?slbName=" + slb1_name);
//        Slb updSlb = DefaultJsonParser.parse(Slb.class, upd);
//        ModelAssert.assertSlbEquals(origSlb, updSlb);
//
//        c.markPass("/api/slb");
//        c.markPass("/api/slb/update");
//
//        // test update app1(__Test_app1)
//        orig = c.getstr("/api/group?groupName=" + app1_name);
//        Group origApp = DefaultJsonParser.parse(Group.class, orig);
//        Group changedApp = new Group().setId(origApp.getId()).setName(origApp.getName()).setAppId(origApp.getAppId())
//                .setHealthCheck(origApp.getHealthCheck())
//                .setLoadBalancingMethod(origApp.getLoadBalancingMethod())
//                .setVersion(origApp.getVersion())
//                .addGroupServer(origApp.getGroupServers().get(0))
//                .addGroupSlb(origApp.getGroupSlbs().get(0));
//        res = c.post("/api/group/update", GenericSerializer.writeJson(changedApp));
//        Assert.assertEquals(true, res.getStatus()==STATUS_OK||res.getStatus()==202);
//        upd = c.getstr("/api/group?groupName=" + app1_name);
//        Group updApp = DefaultJsonParser.parse(Group.class, upd);
//        ModelAssert.assertGroupEquals(changedApp, updApp);
//
//        c.markPass("/api/group");
//        c.markPass("/api/group/update");
//    }
//}
