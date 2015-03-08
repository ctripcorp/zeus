package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.*;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfWriterTest {

    @Test
    public void testConf() throws IOException {
        String slbName = "default";
        Slb slb = new Slb();
        VirtualServer vs002 = new VirtualServer().setName("vs002").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("hotel.ctrip.com"));
        slb.setName(slbName).setNginxBin("/usr/local/nginx/bin").setNginxConf("d:/nginx/conf").setNginxWorkerProcesses(1)
                .addVip(new Vip().setIp("192.168.1.3"))
                .addVip(new Vip().setIp("192.168.1.6"))
                .addSlbServer(new SlbServer().setHostName("slb001a").setIp("192.168.10.1").setEnable(true))
                .addSlbServer(new SlbServer().setHostName("slb003").setIp("192.168.10.3").setEnable(true))
                .addVirtualServer(vs002)
                .addVirtualServer(new VirtualServer().setName("vs003").setPort("80").setSsl(false)
                        .addDomain(new Domain().setName("m.ctrip.com"))
                        .addDomain(new Domain().setName("m2.ctrip.com")))
                .setStatus("TEST");

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
                .addAppSlb(new AppSlb().setSlbName("default").setVirtualServer(vs002).setPath("/hotel"));

        List<App> list = new ArrayList<>();
        list.add(app);

        ConfWriter.writeNginxConf(slb, NginxConf.generate(slb));
        ConfWriter.writeServerConf(slb, vs002, ServerConf.generate(slb, vs002, list));

    }

}