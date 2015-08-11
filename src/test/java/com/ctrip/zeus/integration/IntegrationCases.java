//package com.ctrip.zeus.integration;
//
//import com.ctrip.zeus.ao.ReqClient;
//import com.ctrip.zeus.model.entity.*;
//import com.ctrip.zeus.model.transform.DefaultJsonParser;
//import com.ctrip.zeus.util.ModelAssert;
//import org.junit.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by fanqq on 2015/7/3.
// */
//public class IntegrationCases {
//    private static final String host = "http://10.2.27.21:8099";
//    private static final String hostip = "10.2.27.21";
//    private static final String slb1_server_0 = "10.2.25.83";
//    private static final String slb1_server_1 = "10.2.27.21";
//    private static final String slb1_server_2 = "10.2.25.96";
//    private static final String slb1_name = "__Test_slb1";
//    private static final String slb2_name = "__Test_slb2";
//    private static final int STATUS_OK = 200;
//    private static final int GROUP_NUM = 10;
//    private static ReqClient reqClient = new ReqClient(host);
//    private static final StringBuilder groupNameQuery = new StringBuilder(128);
//    private static final List<Group> orggroups = new ArrayList<>();
//    private static final List<Group> groups = new ArrayList<>();
//    private static Slb orgslb1 = null;
//    private static Slb orgslb2 = null;
//    private static Slb slb1 = null;
//    private static Slb slb2 = null;
//
//    @BeforeClass
//    public void before() throws IOException {
//        clean();
//        init();
//    }
//    @AfterClass
//    public void after() throws IOException {
//        clean();
//        ReqClient.buildReport();
//    }
//    private void clean()throws IOException{
//        Group groupres = null;
//        String groupstr = null;
//        for (int i = 0 ; i < GROUP_NUM ; i ++){
//            groupNameQuery.append("groupName=__Test_app").append(i).append("&");
//        }
//        for (int i = 0; i < GROUP_NUM; i++) {
//            try{
//                reqClient.getstr("/api/deactivate/group?"+groupNameQuery.toString());
//            }catch (Exception e)
//            {
//                System.out.println(e);
//            }
//            reqClient.markPass("/api/deactivate/group");
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
//    private void init() throws IOException {
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
//        GroupServer groupServer1 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver1").setIp(slb1_server_0);
//        GroupServer groupServer2 = new GroupServer().setPort(1000).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver2").setIp(slb1_server_1);
//
//        //add slb and vs
//        orgslb1 = new Slb().setName(slb1_name).addVip(new Vip().setIp(slb1_server_0)).setNginxBin("/opt/app/nginx/sbin")
//                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
//                .addSlbServer(new SlbServer().setHostName("slb1_server_0").setIp(slb1_server_0))
//                .addSlbServer(new SlbServer().setHostName("slb1_server_1").setIp(slb1_server_1))
//                .addVirtualServer(v1)
//                .addVirtualServer(v2)
//                .addVirtualServer(v3)
//                .addVirtualServer(v4)
//                .addVirtualServer(v5)
//                .setStatus("Test");
//
//        orgslb2 = new Slb().setName(slb2_name).addVip(new Vip().setIp(slb1_server_2)).setNginxBin("/opt/app/nginx/sbin")
//                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
//                .addSlbServer(new SlbServer().setHostName("slb1_server_2").setIp(slb1_server_2))
//                .addVirtualServer(v1)
//                .addVirtualServer(v2)
//                .addVirtualServer(v3)
//                .addVirtualServer(v4)
//                .addVirtualServer(v5)
//                .setStatus("Test");
//        reqClient.post("/api/slb/new", String.format(Slb.JSON, orgslb1));
//        reqClient.post("/api/slb/new", String.format(Slb.JSON, orgslb2));
//        String slb1_res = reqClient.getstr("/api/slb?slbName=" + slb1_name);
//        Slb slb1_res_obj = DefaultJsonParser.parse(Slb.class, slb1_res);
//        ModelAssert.assertSlbEquals(slb1, slb1_res_obj);
//        String slb2_res = reqClient.getstr("/api/slb?slbName=" + slb2_name);
//        Slb slb2_res_obj = DefaultJsonParser.parse(Slb.class, slb2_res);
//        ModelAssert.assertSlbEquals(slb2, slb2_res_obj);
//        slb1=slb1_res_obj;
//        slb2=slb2_res_obj;
//        reqClient.markPass("/api/slb/new");
//        reqClient.markPass("/api/slb");
//        //activate test slbs
//        reqClient.getstr("/api/activate/slb?slbName=__Test_slb1&slbName=__Test_slb2");
//        for (int i = 0 ; i < GROUP_NUM ; i++ )
//        {
//            Group group = new Group().setName("__Test_app" + i).setAppId("1000" + i).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
//                    .setIntervals(5000).setPasses(1).setUri("/status.json").setFails(1)).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
//                    .setValue("test"))
//                    .addGroupSlb(new GroupSlb().addVip(new Vip().setIp(hostip)).setSlbId(i % 3 == 0 ? slb2_res_obj.getId() : slb1_res_obj.getId())
//                            .setSlbName(i % 3 == 0 ? slb2_res_obj.getName() : slb1_res_obj.getName())
//                            .setPath("~* ^/app" + i).setVirtualServer(i % 3 == 0 ? slb2_res_obj.getVirtualServers().get(i%4) : slb1_res_obj.getVirtualServers().get(i%4)).setRewrite(i % 2 == 0 ? null : "\"/app\" /app0?sleep=1&size=1" + i)
//                            .setPriority(i)).addGroupServer(i % 2 == 0 ?groupServer1:groupServer2);
//            reqClient.post("/api/group/new", String.format(Group.JSON, group));
//            orggroups.add(group);
//        }
//        String apps = reqClient.getstr("/api/groups");
//        boolean appsuc = apps.contains("\"__Test_app1\"") && apps.contains("\"__Test_app2\"") && apps.contains("\"__Test_app3\"")
//                && apps.contains("\"__Test_app4\"") && apps.contains("\"__Test_app5\"") && apps.contains("\"__Test_app6\"")
//                && apps.contains("\"__Test_app7\"") && apps.contains("\"__Test_app8\"") && apps.contains("\"__Test_app9\"")
//                && apps.contains("\"__Test_app0\"");
//
//        Assert.assertEquals(true, appsuc);
//        reqClient.markPass("/api/group/new");
//        reqClient.markPass("/api/groups");
//
//        Group groupres = null;
//        String groupstr = null;
//
//        groupstr = reqClient.getstr("/api/group?groupName=__Test_app1");
//        groupres = DefaultJsonParser.parse(Group.class, groupstr);
//        ModelAssert.assertGroupEquals(orggroups.get(1), groupres);
//
//        groupstr = reqClient.getstr("/api/group?groupName=__Test_app2");
//        groupres = DefaultJsonParser.parse(Group.class, groupstr);
//        ModelAssert.assertGroupEquals(orggroups.get(2), groupres);
//
//        groupstr = reqClient.getstr("/api/group?groupName=__Test_app3");
//        groupres = DefaultJsonParser.parse(Group.class, groupstr);
//        ModelAssert.assertGroupEquals(orggroups.get(3), groupres);
//        reqClient.markPass("/api/group");
//        for (int i = 0 ; i < GROUP_NUM ; i ++)
//        {
//            groupstr = reqClient.getstr("/api/group?groupName=__Test_app"+i);
//            groupres = DefaultJsonParser.parse(Group.class, groupstr);
//            groups.add(groupres);
//        }
//    }
//
//    @Before
//    public void beforecase() throws IOException {
//        String slb1_res = reqClient.getstr("/api/slb?slbName=" + slb1_name);
//        Slb slb1_tmp = DefaultJsonParser.parse(Slb.class, slb1_res);
//
//    }
//}
