//package com.ctrip.zeus.nginx;
//
//import com.ctrip.zeus.model.entity.*;
//import com.ctrip.zeus.service.build.conf.NginxConf;
//import com.ctrip.zeus.service.build.conf.ServerConf;
//import com.ctrip.zeus.service.build.conf.UpstreamsConf;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//
//public class ConfWriterTest {
//
//    @Test
//    public void testConf() throws Exception {
//        String slbName = "default";
//        Slb slb = new Slb();
//        VirtualServer vs002 = new VirtualServer().setName("vs002").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("hotel.ctrip.com")).setId(2L);
//        slb.setName(slbName).setNginxBin("/usr/local/nginx/bin").setNginxConf("d:/nginx/conf").setNginxWorkerProcesses(1)
//                .addVip(new Vip().setIp("192.168.1.3"))
//                .addSlbServer(new SlbServer().setHostName("slb001a").setIp("192.168.10.1"))
//                .addSlbServer(new SlbServer().setHostName("slb003").setIp("192.168.10.3"))
//                .addVirtualServer(vs002)
//                .addVirtualServer(new VirtualServer().setName("vs003").setPort("80").setSsl(false)
//                        .addDomain(new Domain().setName("m.ctrip.com"))
//                        .addDomain(new Domain().setName("m2.ctrip.com")))
//                .setStatus("TEST").setId(1L);
//
//        String groupName = "testApp";
//        Group group = new Group();
//        group.setName(groupName)
//                .setAppId("999999").setSsl(true)
//                .setHealthCheck(new HealthCheck().setFails(5).setIntervals(50).setPasses(2).setUri("/hotel"))
//                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
//                .addGroupServer(new GroupServer().setIp("192.168.20.1")
//                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
//                .addGroupServer(new GroupServer().setIp("192.168.20.2")
//                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
//                .addGroupSlb(new GroupSlb().setSlbName("default").setSlbId(1L).setVirtualServer(vs002).setPath("/hotel"));
//
//        List<Group> list = new ArrayList<>();
//        list.add(group);
//        NginxConf.generate(slb);
//        ServerConf.generate(slb, vs002, list);
//        NginxOperator op = new NginxOperator(slb.getNginxConf(),slb.getNginxBin());
//        op.writeNginxConf(NginxConf.generate(slb));
//        op.writeServerConf(2L, ServerConf.generate(slb, vs002, list));
//        op.writeUpstreamsConf(2L, UpstreamsConf.generate(slb, vs002, list,new HashSet<String>(),new HashSet<String>()));
//
//    }
//
//}