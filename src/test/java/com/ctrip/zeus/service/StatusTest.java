//package com.ctrip.zeus.service;
//
//import com.ctrip.zeus.ao.AbstractAPITest;
//import com.ctrip.zeus.ao.AopSpring;
//import com.ctrip.zeus.ao.Checker;
//import com.ctrip.zeus.ao.ReqClient;
//import com.ctrip.zeus.client.GroupClient;
//import com.ctrip.zeus.client.SlbClient;
//import com.ctrip.zeus.model.entity.*;
//import com.ctrip.zeus.service.status.StatusService;
//import com.ctrip.zeus.support.GenericSerializer;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.annotation.Resource;
//
//
///**
// * Created by fanqq on 2015/4/2.
// */
//public class StatusTest extends AbstractAPITest {
//
//    private SlbClient sc = new SlbClient("http://127.0.0.1:8099");
//    private GroupClient gc = new GroupClient("http://127.0.0.1:8099");
//
//    @Resource
//    StatusService statusService;
//
//    @Before
//    public void before() throws Exception {
//        new ReqClient("http://127.0.0.1:8099").post("/api/slb/new", GenericSerializer.writeJson(generateSlb("default")));
//        new ReqClient("http://127.0.0.1:8099").post("/api/group/new", GenericSerializer.writeJson(generateGroup("Test")));
//    }
//
//    @Test
//    public void statusTest() {
//        Slb slb = sc.get("default");
//        Group group = gc.get("Test");
//        AopSpring.addChecker("StatusServiceImpl.upServer", new Checker() {
//
//            @Override
//            public void check() {
//                try {
//                    Assert.assertEquals(true, statusService.getServerStatus("101.2.6.201"));
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        AopSpring.addChecker("StatusServiceImpl.downServer", new Checker() {
//
//            @Override
//            public void check() {
//                try {
//                    Assert.assertEquals(false, statusService.getServerStatus("101.2.6.201"));
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
////        new ReqClient("http://127.0.0.1:8099").get("/api/conf/activateByName?slbName=default&groupName=Test");
////
////        String responseup = new ReqClient("http://127.0.0.1:8099/api/op/upServer?ip=101.2.6.201").getstr();
////        String responsedown = new ReqClient("http://127.0.0.1:8099/api/op/downServer?ip=101.2.6.201").getstr();
////        System.out.println(responseup);
////        System.out.println(responsedown);
////
////        String responseupM = new ReqClient("http://127.0.0.1:8099/api/op/upMemberByName?groupName=Test&ip=101.2.6.201").getstr();
////        String responsedownM = new ReqClient("http://127.0.0.1:8099/api/op/downMemberByName?groupName=Test&ip=101.2.6.201").getstr();
////
////        System.out.println(responseupM);
////        System.out.println(responsedownM);
//    }
//
//    private Group generateGroup(String groupName) {
//        Slb slb = sc.get("default");
//        return new Group().setName(groupName).setAppId("921812").setVersion(1)
//                .setHealthCheck(new HealthCheck().setIntervals(5000).setFails(1).setPasses(2).setUri("/domaininfo/OnService.html"))
//                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
//                .addGroupSlb(new GroupSlb().setSlbId(slb.getId()).setSlbName(slb.getName()).setPath("/").setVirtualServer(slb.getVirtualServers().get(0)))
//                .addGroupServer(new GroupServer().setPort(8080).setWeight(1).setMaxFails(2).setFailTimeout(30).setHostName("0").setIp("10.2.6.201"))
//                .addGroupServer(new GroupServer().setPort(80).setWeight(2).setMaxFails(2).setFailTimeout(30).setHostName("0").setIp("10.2.6.202"));
//    }
//
//    private Slb generateSlb(String slbName) {
//        return new Slb().setName(slbName).setVersion(7)
//                .setNginxBin("/opt/group/nginx/sbin").setNginxConf("/opt/group/nginx/conf").setNginxWorkerProcesses(2).setVersion(0)
//                .addVip(new Vip().setIp("101.2.25.93"))
//                .addSlbServer(new SlbServer().setHostName("uat0358").setIp("101.2.25.93"))
//                .addSlbServer(new SlbServer().setHostName("uat0359").setIp("101.2.25.94"))
//                .addSlbServer(new SlbServer().setHostName("uat0360").setIp("101.2.25.95"))
//                .addVirtualServer(new VirtualServer().setName("site1").setPort("80").setSsl(false)
//                        .addDomain(new Domain().setName("s1.ctrip.com")))
//                .addVirtualServer(new VirtualServer().setName("site2").setPort("80").setSsl(false)
//                        .addDomain(new Domain().setName("s2a.ctrip.com"))
//                        .addDomain(new Domain().setName("s2b.ctrip.com")))
//                .setStatus("TEST");
//    }
//}
