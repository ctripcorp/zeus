package com.ctrip.zeus.restful;

import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.message.impl.DefaultResponseHandler;
import com.ctrip.zeus.restful.message.impl.ErrorResponseHandler;
import com.ctrip.zeus.support.GenericSerializer;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

/**
 * Created by zhoumy on 2016/2/22.
 */
public class ResponseWritingTest {

    @Test
    public void testSpecialCases() throws Exception {
        System.out.println("************************* Test Special Cases Serialization *************************");

        DefaultResponseHandler rh = new DefaultResponseHandler();
        System.out.printf(String.valueOf(
                rh.generateMessage("%s", MediaType.APPLICATION_JSON_TYPE).getResponse()));

        ErrorResponseHandler erh = new ErrorResponseHandler();
        System.out.printf(String.valueOf(
                erh.generateMessage(new Exception("%s"), MediaType.APPLICATION_JSON_TYPE, false).getResponse()));
    }

    @Test
    public void testFormat() throws Exception {
        System.out.println("*************************    Test Serialization Format    *************************");

        DefaultResponseHandler rh = new DefaultResponseHandler();
        System.out.printf(String.valueOf(
                rh.generateMessage("json", MediaType.APPLICATION_JSON_TYPE).getResponse()));

        System.out.printf(String.valueOf(
                rh.generateMessage("xml", MediaType.APPLICATION_XML_TYPE).getResponse()));
    }

    @Test
    public void testPerformance() throws Exception {
        System.out.println("************************* Test Serialization Performance *************************");

        Group group = new Group().setId(9837401263971292L).setName("performace").setAppId("000000").setSsl(false)
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"));
        for (int i = 0; i < 10; i++) {
            group.addGroupVirtualServer(new GroupVirtualServer().setPath("/performace").setVirtualServer(new VirtualServer().setId(12345678891028L)));
        }
        for (int i = 0; i < 10; i++) {
            group.addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.201"));
        }
        GroupList groupList = new GroupList();
        for (int i = 0; i < 100; i++) {
            groupList.addGroup(group);
        }

        final String JSON = "%#.3s";
        for (int i = 0; i < 150; i++) {
            GenericSerializer.writeJson(groupList);
        }
        for (int i = 0; i < 150; i++) {
            String.format(JSON, groupList);
        }

        final String XML = "%.3s";
        for (int i = 0; i < 150; i++) {
            GenericSerializer.writeXml(groupList);
        }
        for (int i = 0; i < 150; i++) {
            String.format(XML, groupList);
        }

        Long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            GenericSerializer.writeJson(groupList);
        }
        System.out.println("Using GenericSerializer to write json data takes " + (System.nanoTime() - start) / (1000 * 1000) + " ms.");

        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            String.format(JSON, groupList);
        }
        System.out.println("Using StringFormat to write json data takes " + (System.nanoTime() - start) / (1000 * 1000) + " ms.");

        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            GenericSerializer.writeXml(groupList);
        }
        System.out.println("Using GenericSerializer to write xml data takes " + (System.nanoTime() - start) / (1000 * 1000) + " ms.");

        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            String.format(XML, groupList);
        }
        System.out.println("Using StringFormat to write xml data takes " + (System.nanoTime() - start) / (1000 * 1000) + " ms.");
    }
}
