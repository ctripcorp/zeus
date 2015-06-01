package com.ctrip.zeus.integration;

import com.ctrip.zeus.ao.ReqClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.service.ModelServiceTest;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.util.ModelAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/4/10.
 */
public class IntegrationTest {

    private static final String host = "http://10.2.27.21:8099";

//    private static final String host = "http://127.0.0.1:8099";
    private static final String hostip = "10.2.27.21";
    private static final String slb1_server_0 = "10.2.25.83";
    private static final String slb1_server_1 = "10.2.27.21";
    private static final String slb1_server_2 = "10.2.25.96";
    private static final String slb1_name = "__Test_slb1";
    private static final String slb2_name = "__Test_slb2";
    private static final int STATUS_OK = 200;
    private static ReqClient reqClient = new ReqClient(host);


    @Before
    public void before() throws IOException {
        Group groupres = null;
        String groupstr = null;

        for (int i = 1; i < 11; i++) {
            groupstr = reqClient.getstr("/api/group/get/__Test_app"+i);
            groupres = DefaultJsonParser.parse(Group.class, groupstr);
            if (groupres!=null)
            {
                reqClient.getstr("/api/group/delete?groupId=" + groupres.getId());
            }
        }


        String slb_res = reqClient.getstr("/api/slb/get/" + slb1_name);
        Slb slb_res_obj = DefaultJsonParser.parse(Slb.class, slb_res);
        if (slb_res_obj!=null)
        {
            reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
        }

        slb_res = reqClient.getstr("/api/slb/get/" + slb2_name);
        slb_res_obj = DefaultJsonParser.parse(Slb.class, slb_res);
        if (slb_res_obj!=null)
        {
            reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
        }
    }

    @After
    public void after() throws IOException {

        Group groupres = null;
        String groupstr = null;

        for (int i = 1; i < 11; i++) {
            groupstr = reqClient.getstr("/api/group/get/__Test_app"+i);
            groupres = DefaultJsonParser.parse(Group.class, groupstr);
            if (groupres!=null)
            {
                reqClient.getstr("/api/group/delete?groupId=" + groupres.getId());
            }
        }


        String slb_res = reqClient.getstr("/api/slb/get/" + slb1_name);
        if (slb_res!=null)
        {
            Slb slb_res_obj = DefaultJsonParser.parse(Slb.class, slb_res);
            reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
        }

        slb_res = reqClient.getstr("/api/slb/get/" + slb2_name);
        if (slb_res!=null)
        {
            Slb slb_res_obj = DefaultJsonParser.parse(Slb.class, slb_res);
            reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
        }


        String res = reqClient.getstr("/api/group/get/__Test_app2");
        Assert.assertEquals(true,res.isEmpty());
        reqClient.markPass("/api/group/delete");

        res = reqClient.getstr("/api/slb/get/" + slb2_name);
        Assert.assertEquals(true,res.isEmpty());
        reqClient.markPass("/api/slb/delete");

        ReqClient.buildReport();

    }

    @Test
    public void integrationTest() throws IOException {

        VirtualServer v1 = new VirtualServer().setName("__Test_vs1").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs1.ctrip.com"));
        VirtualServer v2 = new VirtualServer().setName("__Test_vs2").setPort("80").setSsl(true)
                .addDomain(new Domain().setName("vs2.ctrip.com"))
                .addDomain(new Domain().setName("vs6.ctrip.com"));
        VirtualServer v3 = new VirtualServer().setName("__Test_vs3").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs3.ctrip.com"));
        VirtualServer v4 = new VirtualServer().setName("__Test_vs4").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs4.ctrip.com"));
        VirtualServer v5 = new VirtualServer().setName("__Test_vs5").setPort("80").setSsl(true)
                .addDomain(new Domain().setName("vs5.ctrip.com"));


        GroupServer groupServer1 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver1").setIp(slb1_server_0);
        GroupServer groupServer2 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver2").setIp(slb1_server_1);
        GroupServer groupServer3 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver3").setIp(slb1_server_2);


        //add slb and vs
        Slb slb1 = new Slb().setName(slb1_name).addVip(new Vip().setIp(slb1_server_0)).setNginxBin("/opt/app/nginx/sbin")
                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addSlbServer(new SlbServer().setHostName("slb1_server_0").setIp(slb1_server_0).setEnable(true))
                .addSlbServer(new SlbServer().setHostName("slb1_server_1").setIp(slb1_server_1).setEnable(true))
                .addVirtualServer(v1)
                .addVirtualServer(v2)
                .addVirtualServer(v3)
                .addVirtualServer(v4)
                .addVirtualServer(v5)
                .setStatus("Test");

        Slb slb2 = new Slb().setName(slb2_name).addVip(new Vip().setIp(slb1_server_2)).setNginxBin("/opt/app/nginx/sbin")
                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addSlbServer(new SlbServer().setHostName("slb1_server_2").setIp(slb1_server_2).setEnable(true))
                .addVirtualServer(v1)
                .addVirtualServer(v2)
                .addVirtualServer(v3)
                .addVirtualServer(v4)
                .addVirtualServer(v5)
                .setStatus("Test");


        reqClient.post("/api/slb/add", String.format(Slb.JSON, slb1));
        reqClient.post("/api/slb/add", String.format(Slb.JSON, slb2));


        //assert slb1 slb2
        boolean suc1 = reqClient.getstr("/api/slb").contains(slb1_name);
        boolean suc2 = reqClient.getstr("/api/slb").contains(slb2_name);

        Assert.assertEquals(true, suc1 && suc2);

        reqClient.markPass("/api/slb/add");
        reqClient.markPass("/api/slb");


        String slb1_res = reqClient.getstr("/api/slb/get/" + slb1_name);
        Slb slb1_res_obj = DefaultJsonParser.parse(Slb.class, slb1_res);

        ModelAssert.assertSlbEquals(slb1, slb1_res_obj);

        String slb2_res = reqClient.getstr("/api/slb/get/" + slb2_name);
        Slb slb2_res_obj = DefaultJsonParser.parse(Slb.class, slb2_res);

        ModelAssert.assertSlbEquals(slb2, slb2_res_obj);

        reqClient.markPass("/api/slb/get/"+ slb1_name);
        reqClient.markPass("/api/slb/get/"+ slb2_name);
        //activate test slbs
        reqClient.getstr("/api/conf/activateByName?slbName=__Test_slb1&slbName=__Test_slb2");

        List<Group> groups = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++ )
        {
            Group group = new Group().setName("__Test_app"+i).setAppId("1000"+i).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                .setIntervals(2000).setPasses(1).setUri("/status.json")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                .setValue("test"))
                .addGroupSlb(new GroupSlb().setSlbId(i%3==0?slb2_res_obj.getId():slb1_res_obj.getId()).setPath("/app"+i).setVirtualServer(i%2==0?v1:v2).setRewrite(i%2==0?null:"/app /app0?sleep=1&size=1"+i)
                .setPriority(i)).addGroupServer(groupServer1)
                    .addGroupServer(groupServer2)
                    .addGroupServer(groupServer3);
            reqClient.post("/api/group/add", String.format(Group.JSON, group));
            groups.add(group);
        }
        String apps = reqClient.getstr("/api/group");
        boolean appsuc = apps.contains("\"__Test_app1\"") && apps.contains("\"__Test_app2\"") && apps.contains("\"__Test_app3\"")
                && apps.contains("\"__Test_app4\"") && apps.contains("\"__Test_app5\"") && apps.contains("\"__Test_app6\"")
                && apps.contains("\"__Test_app7\"") && apps.contains("\"__Test_app8\"") && apps.contains("\"__Test_app9\"")
                && apps.contains("\"__Test_app10\"");

        Assert.assertEquals(true,appsuc);
        reqClient.markPass("/api/group/add");
        reqClient.markPass("/api/group");


        Group groupres = null;
        String groupstr = null;

        groupstr = reqClient.getstr("/api/group/get/__Test_app1");
        groupres = DefaultJsonParser.parse(Group.class, groupstr);
        ModelAssert.assertGroupEquals(groups.get(1), groupres);

        groupstr = reqClient.getstr("/api/group/get/__Test_app2");
        groupres = DefaultJsonParser.parse(Group.class, groupstr);
        ModelAssert.assertGroupEquals(groups.get(2), groupres);

        groupstr = reqClient.getstr("/api/group/get/__Test_app3");
        groupres = DefaultJsonParser.parse(Group.class, groupstr);
        ModelAssert.assertGroupEquals(groups.get(3), groupres);
        reqClient.markPass("/api/app/get/__Test_app1");
        reqClient.markPass("/api/app/get/__Test_app2");
        reqClient.markPass("/api/app/get/__Test_app3");


        integrationTest_update();

        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app1");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app2");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app3");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app4");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app5");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app6");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app7");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app8");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app9");
        reqClient.getstr("/api/conf/activateByName?groupName=__Test_app10");


        for (int i = 1; i < 11; i++) {
            String groupstatus = reqClient.getstr("/api/status/groupName/__Test_app" + i);
            GroupStatusList groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, groupstatus);

            for (GroupStatus as : groupStatusList.getGroupStatuses()) {
                Assert.assertEquals("__Test_app" + i, as.getGroupName());
                Assert.assertEquals(true, as.getSlbName().equals(slb1_name) || as.getSlbName().equals(slb2_name));

                for (GroupServerStatus ass : as.getGroupServerStatuses()) {
                    Assert.assertEquals(true, ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1) || ass.getIp().equals(slb1_server_2));
                    Assert.assertEquals(true, ass.getServer());
                    Assert.assertEquals(true, ass.getMember());
                }
            }

            reqClient.markPass("/api/status/groupName/__Test_app"+i);
        }

        reqClient.markPass("/api/conf/activate");

        reqClient.getstr("/api/op/downServer?ip=" + slb1_server_1);
        reqClient.getstr("/api/op/downServer?ip=" + slb1_server_0);

        String slbstatus = reqClient.getstr("/api/status/slbName/" + slb1_name);

        GroupStatusList groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);

        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(false, ass.getServer());
                }
            }
        }

        slbstatus = reqClient.getstr("/api/status/slbName/" + slb2_name);

        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);

        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(false, ass.getServer());
                }
            }
        }

        reqClient.markPass("/api/status/slbName/"+slb1_name);
        reqClient.markPass("/api/status/slbName/"+slb2_name);
        reqClient.markPass("/api/op/downServer");
        reqClient.getstr("/api/op/upServer?ip=" + slb1_server_0);
        reqClient.getstr("/api/op/upServer?ip=" + slb1_server_1);


        slbstatus = reqClient.getstr("/api/status/slbName/" + slb2_name);

        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);

        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(true, ass.getServer());
                }
            }
        }

        slbstatus = reqClient.getstr("/api/status/slbName/" + slb1_name);

        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, slbstatus);

        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
                if (ass.getIp().equals(slb1_server_0) || ass.getIp().equals(slb1_server_1)) {
                    Assert.assertEquals(true, ass.getServer());
                }
            }
        }

        reqClient.markPass("/api/op/upServer");


        reqClient.getstr("/api/op/downMemberByName?ip=" + slb1_server_2 + "&groupName=__Test_app3");

        String groupstatus = reqClient.getstr("/api/status/groupName/__Test_app3");

        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, groupstatus);

        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
                if (ass.getIp().equals(slb1_server_2)) {
                    Assert.assertEquals(false, ass.getServer());
                }
            }
        }


        reqClient.markPass("/api/op/downMemberByName");
        reqClient.markPass("/api/status/groupName/__Test_app3");


        reqClient.getstr("/api/op/upMember?ip=" + slb1_server_2 + "&appName=__Test_app3");


        groupstatus = reqClient.getstr("/api/status/groupName/__Test_app3");

        groupStatusList = DefaultJsonParser.parse(GroupStatusList.class, groupstatus);

        for (GroupStatus as : groupStatusList.getGroupStatuses()) {
            for (GroupServerStatus ass : as.getGroupServerStatuses()) {
                if (ass.getIp().equals(slb1_server_2)) {
                    Assert.assertEquals(true, ass.getServer());
                }
            }
        }

        reqClient.markPass("/api/op/upMember");
    }

    private void integrationTest_update() throws IOException {
        final String app1_name = "__Test_app1";
        final ReqClient c = new ReqClient(host);
        String orig, upd;
        Response res;

        // test update slb1(__Test_slb1)
        orig = c.getstr("/api/slb/get/" + slb1_name);
        Slb origSlb = DefaultJsonParser.parse(Slb.class, orig);
        origSlb.setNginxWorkerProcesses(origSlb.getNginxWorkerProcesses() + 127);
        res = c.post("/api/slb/update", GenericSerializer.writeJson(origSlb));
        Assert.assertEquals(STATUS_OK, res.getStatus());
        upd = c.getstr("/api/slb/get/" + slb1_name);
        Slb updSlb = DefaultJsonParser.parse(Slb.class, upd);
        ModelAssert.assertSlbEquals(origSlb, updSlb);

        c.markPass("/api/slb/get/" + slb1_name);
        c.markPass("/api/slb/update");

        // test update app1(__Test_app1)
        orig = c.getstr("/api/group/get/" + app1_name);
        Group origApp = DefaultJsonParser.parse(Group.class, orig);
        Group changedApp = new Group().setName(origApp.getName()).setAppId(origApp.getAppId())
                .setHealthCheck(origApp.getHealthCheck())
                .setLoadBalancingMethod(origApp.getLoadBalancingMethod())
                .setVersion(origApp.getVersion())
                .addGroupServer(origApp.getGroupServers().get(0))
                .addGroupSlb(origApp.getGroupSlbs().get(0));
        res = c.post("/api/group/update", GenericSerializer.writeJson(changedApp));
        Assert.assertEquals(STATUS_OK, res.getStatus());
        upd = c.getstr("/api/group/get/" + app1_name);
        Group updApp = DefaultJsonParser.parse(Group.class, upd);
        ModelAssert.assertGroupEquals(changedApp, updApp);

        c.markPass("/api/group/get/" + app1_name);
        c.markPass("/api/group/update");
    }
}
