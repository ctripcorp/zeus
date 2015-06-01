package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

public class UpstreamsConfTest {

    @Test
    public void testGenerate() throws Exception {
        VirtualServer vs = new VirtualServer().setName("vs002").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("hotel.ctrip.com"))
                .addDomain(new Domain().setName("flight.ctrip.com"));

        String groupName = "testApp";
        Group group = new Group();
        group.setName(groupName)
                .setAppId("999999")
                .setHealthCheck(new HealthCheck().setFails(5).setIntervals(50).setPasses(2).setUri("/hotel"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupServer(new GroupServer().setIp("192.168.20.1")
                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
                .addGroupServer(new GroupServer().setIp("192.168.20.2")
                        .setFailTimeout(30).setMaxFails(2).setPort(80).setWeight(2))
                .addGroupSlb(new GroupSlb().setSlbName("default").setSlbId(1L).setVirtualServer(new VirtualServer().setName("vs002").setPort("80")
                        .setSsl(false).addDomain(new Domain().setName("hotel.ctrip.com"))).setPath("/hotel"))
        ;


        ArrayList<Group> groups = new ArrayList<>();
        groups.add(group);
        System.out.println(UpstreamsConf.generate(new Slb().setName("default"), vs, groups, new HashSet<String>(), new HashSet<String>()));

    }
}