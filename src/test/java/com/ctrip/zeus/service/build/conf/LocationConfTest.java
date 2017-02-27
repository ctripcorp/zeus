package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.*;
import org.junit.Test;

/**
 * Created by zhoumy on 2017/1/18.
 */
public class LocationConfTest {

    @Test
    public void generateTrafficControlScriptWithDiffWeight() throws Exception {
        VirtualServer v = new VirtualServer().setId(1L);
        TrafficPolicy policy = new TrafficPolicy().setId(100L).setName("test-policy").setVersion(1)
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(v).setPath("~* ^/test($|/|\\?)").setPriority(1100))
                .addTrafficControl(new TrafficControl().setWeight(50)
                        .setGroup(new Group().setId(1L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group1"))))
                .addTrafficControl(new TrafficControl().setWeight(30)
                        .setGroup(new Group().setId(2L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group2"))))
                .addTrafficControl(new TrafficControl().setWeight(70)
                        .setGroup(new Group().setId(3L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group3"))));
        LocationConf lc = new LocationConf();
        System.out.println("content_by_lua " + lc.generateTrafficControlScript(policy.getControls()));
    }

    @Test
    public void generateTrafficControlScriptWithSameWeight() throws Exception {
        VirtualServer v = new VirtualServer().setId(1L);
        TrafficPolicy policy = new TrafficPolicy().setId(100L).setName("test-policy").setVersion(1)
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(v).setPath("~* ^/test($|/|\\?)").setPriority(1100))
                .addTrafficControl(new TrafficControl().setWeight(50)
                        .setGroup(new Group().setId(1L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group1"))))
                .addTrafficControl(new TrafficControl().setWeight(50)
                        .setGroup(new Group().setId(2L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group2"))))
                .addTrafficControl(new TrafficControl().setWeight(50)
                        .setGroup(new Group().setId(3L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group3"))));
        LocationConf lc = new LocationConf();
        System.out.println("content_by_lua " + lc.generateTrafficControlScript(policy.getControls()));
    }

    @Test
    public void generateTrafficControlScriptWithZeroWeight_case1() {
        VirtualServer v = new VirtualServer().setId(1L);
        TrafficPolicy policy = new TrafficPolicy().setId(100L).setName("test-policy").setVersion(1)
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(v).setPath("~* ^/test($|/|\\?)").setPriority(1100))
                .addTrafficControl(new TrafficControl().setWeight(0)
                        .setGroup(new Group().setId(1L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group1"))))
                .addTrafficControl(new TrafficControl().setWeight(50)
                        .setGroup(new Group().setId(2L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group2"))))
                .addTrafficControl(new TrafficControl().setWeight(0)
                        .setGroup(new Group().setId(3L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group3"))));
        LocationConf lc = new LocationConf();
        System.out.println("content_by_lua " + lc.generateTrafficControlScript(policy.getControls()));
    }

    @Test
    public void generateTrafficControlScriptWithZeroWeight_case2() {
        VirtualServer v = new VirtualServer().setId(1L);
        TrafficPolicy policy = new TrafficPolicy().setId(100L).setName("test-policy").setVersion(1)
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(v).setPath("~* ^/test($|/|\\?)").setPriority(1100))
                .addTrafficControl(new TrafficControl().setWeight(30)
                        .setGroup(new Group().setId(1L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group1"))))
                .addTrafficControl(new TrafficControl().setWeight(50)
                        .setGroup(new Group().setId(2L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group2"))))
                .addTrafficControl(new TrafficControl().setWeight(0)
                        .setGroup(new Group().setId(3L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group3"))));
        LocationConf lc = new LocationConf();
        System.out.println("content_by_lua " + lc.generateTrafficControlScript(policy.getControls()));
    }

    @Test
    public void generateTrafficControlScriptWithSingleControl() throws Exception {
        VirtualServer v = new VirtualServer().setId(1L);
        TrafficPolicy policy = new TrafficPolicy().setId(100L).setName("test-policy").setVersion(1)
                .addPolicyVirtualServer(new PolicyVirtualServer().setVirtualServer(v).setPath("~* ^/test($|/|\\?)").setPriority(1100))
                .addTrafficControl(new TrafficControl().setWeight(50)
                        .setGroup(new Group().setId(1L)
                                .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(v).setPriority(1000).setPath("@group1"))));
        LocationConf lc = new LocationConf();
        System.out.println("content_by_lua " + lc.generateTrafficControlScript(policy.getControls()));
    }
}