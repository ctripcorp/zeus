//package com.ctrip.zeus.nginx;
//
//import com.ctrip.zeus.model.entity.*;
//import com.ctrip.zeus.service.build.conf.UpstreamsConf;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//
//public class UpstreamsConfTest {
//
//    @Test
//    public void testGenerate() throws Exception {
//        VirtualServer vs = new VirtualServer().setName("vs002").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("hotel.ctrip.com"))
//                .addDomain(new Domain().setName("flight.ctrip.com")).setId(2L);
//
//        String groupName = "testApp";
//        Group group = new Group();
//        group.setName(groupName)
//                .setAppId("999999")
//                .setHealthCheck(new HealthCheck().setFails(5).setIntervals(50).setPasses(2).setUri("/hotel"))
//                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
//                .addGroupServer(new GroupServer().setIp("192.168.20.1")
//                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
//                .addGroupServer(new GroupServer().setIp("192.168.20.2")
//                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
//                .addGroupSlb(new GroupSlb().setSlbName("default").setSlbId(1L).setVirtualServer(new VirtualServer().setName("vs002").setPort("80")
//                        .setSsl(false).addDomain(new Domain().setName("hotel.ctrip.com"))).setPath("/hotel")).setSsl(true);
//
//        Slb slb = new Slb().setName("default").setNginxBin("/usr/local/nginx/bin").setNginxConf("d:/nginx/conf").setNginxWorkerProcesses(1)
//                .addVip(new Vip().setIp("192.168.1.3"))
//                .addSlbServer(new SlbServer().setHostName("slb001a").setIp("192.168.10.1"))
//                .addSlbServer(new SlbServer().setHostName("slb003").setIp("192.168.10.3"))
//                .addVirtualServer(vs)
//                .addVirtualServer(new VirtualServer().setName("vs003").setPort("80").setSsl(false)
//                        .addDomain(new Domain().setName("m.ctrip.com"))
//                        .addDomain(new Domain().setName("m2.ctrip.com")))
//                .setStatus("TEST").setId(1L);
//
//
//        ArrayList<Group> groups = new ArrayList<>();
//        groups.add(group);
//        System.out.println(UpstreamsConf.generate(slb, vs, groups, new HashSet<String>(), new HashSet<String>()));
//
//    }
//}