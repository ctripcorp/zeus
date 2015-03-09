package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.conf.ServerConf;
import org.junit.Test;

import java.util.ArrayList;

public class ServerConfTest {

    @Test
    public void testGenerate() throws Exception {
        VirtualServer vs = new VirtualServer().setName("vs002").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("hotel.ctrip.com"))
                .addDomain(new Domain().setName("flight.ctrip.com"));

        String appName = "testApp";
        App app = new App();
        app.setName(appName)
                .setAppId("999999")
                .setHealthCheck(new HealthCheck().setFails(5).setIntervals(50).setPasses(2).setUri("/hotel"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addAppServer(new AppServer().setServer(new Server().setIp("192.168.20.1").setHostName("app001").setUp(true))
                        .setEnable(true).setFailTimeout(30).setHealthy(true).setMaxFails(2).setPort(80).setWeight(2))
                .addAppServer(new AppServer().setServer(new Server().setIp("192.168.20.2").setHostName("app002").setUp(true))
                        .setEnable(true).setFailTimeout(30).setHealthy(true).setMaxFails(2).setPort(80).setWeight(2))
                .addAppSlb(new AppSlb().setSlbName("default").setVirtualServer(new VirtualServer().setName("vs002").setPort("80")
                        .setSsl(false).addDomain(new Domain().setName("hotel.ctrip.com"))).setPath("/hotel"))
        ;


        ArrayList<App> apps = new ArrayList<>();
        apps.add(app);
        System.out.println(ServerConf.generate(new Slb().setName("default"), vs, apps));

    }
}