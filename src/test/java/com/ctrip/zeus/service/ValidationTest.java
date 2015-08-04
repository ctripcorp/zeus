package com.ctrip.zeus.service;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import javax.annotation.Resource;
import java.io.File;

/**
 * Created by zhoumy on 2015/7/7.
 */
public class ValidationTest extends AbstractSpringTest {
    private static MysqlDbServer mysqlDbServer;

    @Resource
    SlbRepository slbRepository;
    @Resource
    GroupRepository groupRepository;
    @Resource
    VirtualServerRepository virtualServerRepository;

    private static Slb slb4Group;

    @BeforeClass
    public static void setUpDb() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @Test
    public void testValidateSlb_emptyField() throws Exception {
        Slb slb = new Slb().setStatus("Test")
                .setNginxConf("/conf").setNginxBin("/bin").setNginxWorkerProcesses(3)
                .addSlbServer(new SlbServer());
        try {
            slbRepository.add(slb);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: slb with null name; real cause: " + e.getMessage());
        }

        slb = new Slb().setName("testEmptyField").setStatus("Test")
                .setNginxConf("/conf").setNginxBin("/bin").setNginxWorkerProcesses(3);
        try {
            slbRepository.add(slb);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: slb without slb servers; real cause: " + e.getMessage());
        }

        slb.addSlbServer(new SlbServer().setHostName("localhost").setIp("127.0.0.1"));
        slbRepository.add(slb);
    }

    @Test
    public void testValidateSlb_duplicateDomainPort() throws Exception {
        Slb slb = new Slb().setName("testDuplicateDomainPort").setStatus("Test")
                .setNginxConf("/conf").setNginxBin("/bin").setNginxWorkerProcesses(3)
                .addSlbServer(new SlbServer())
                .addVirtualServer(new VirtualServer().setPort("80").setName("VS_01").addDomain(new Domain().setName("localhost.com")))
                .addVirtualServer(new VirtualServer().setPort("80").setName("VS_02").addDomain(new Domain().setName("localhost.com")));
        try {
            slbRepository.add(slb);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: duplicate domain & port in the model; real cause: " + e.getMessage());
        }

        slb = new Slb().setName("testDuplicateDomainPort").setStatus("Test")
                .setNginxConf("/conf").setNginxBin("/bin").setNginxWorkerProcesses(3)
                .addSlbServer(new SlbServer().setHostName("localhost.com").setIp("127.0.0.1"))
                .addVirtualServer(new VirtualServer().setPort("80").setName("VS_01").addDomain(new Domain().setName("localhost.com")))
                .addVirtualServer(new VirtualServer().setPort("8080").setName("VS_02").addDomain(new Domain().setName("localhost.com")))
                .addVirtualServer(new VirtualServer().setPort("80").setName("VS_03").addDomain(new Domain().setName("localhost1.com")));
        slbRepository.add(slb);
    }

    @Test
    public void testValidateSlb_removeVirtualServer() throws Exception {
        Slb slb = new Slb().setName("testRemoveVirtualServer").setStatus("Test")
                .setNginxConf("/conf").setNginxBin("/bin").setNginxWorkerProcesses(3)
                .addSlbServer(new SlbServer().setHostName("localhost.com").setIp("127.0.0.1"))
                .addVirtualServer(new VirtualServer().setPort("80").setName("VS_01").addDomain(new Domain().setName("localhost.com")));
        slbRepository.add(slb);
        slb = slbRepository.getById(slb.getId());
        Group group = new Group().setName("test").setAppId("000000")
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(slb.getVirtualServers().get(0)))
                .addGroupServer(new GroupServer().setPort(80).setWeight(1).setMaxFails(1).setFailTimeout(30).setHostName("0").setIp("10.2.6.202"));
        groupRepository.add(group);
        group = groupRepository.getById(group.getId());
        Assert.assertEquals(slb.getVirtualServers().get(0).getId(), group.getGroupVirtualServers().get(0).getVirtualServer().getId());
        try {
            virtualServerRepository.deleteVirtualServer(slb.getVirtualServers().get(0).getId());
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: virtual server has dependencies; real cause: " + e.getMessage());
        }
        groupRepository.delete(group.getId());
    }

    @Test
    public void testValidateGroup_emptyField() throws Exception {
        Slb slb = prepareSlb();
        Group group = new Group().setAppId("000000")
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(slb.getVirtualServers().get(0)));
        try {
            groupRepository.add(group);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: group without name; real cause: " + e.getMessage());
        }
        group = new Group().setName("testEmptyField")
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test2").setVirtualServer(slb.getVirtualServers().get(0)));
        try {
            groupRepository.add(group);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: group without appId; real cause: " + e.getMessage());
        }

        group = new Group().setName("testEmptyField").setAppId("000000")
                .setHealthCheck(new HealthCheck().setIntervals(2000).setFails(1).setPasses(1).setUri("/"))
                .setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin").setValue("test"));
        try {
            groupRepository.add(group);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: group without group-vs; real cause: " + e.getMessage());
        }

        group = new Group().setName("testEmptyField").setAppId("000000")
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test3").setVirtualServer(slb.getVirtualServers().get(0)));
        groupRepository.add(group);
    }

    @Test
    public void testValidateGroup_virtualServerNotExist() throws Exception {
        Slb slb = prepareSlb();
        Group group = new Group().setName("testVirtualServerNotExist").setAppId("000000")
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(new VirtualServer().setId(1024L).setSlbId(10L)));
        try {
            groupRepository.add(group);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
            System.out.println("Expected cause: virtual server does not exist; real cause: " + e.getMessage());
        }

        group = new Group().setName("testVirtualServerNotExist").setAppId("000000")
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(new VirtualServer().setName("error")));
        try {
            groupRepository.add(group);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
            System.out.println("Expected cause: virtual server does not exist; real cause: " + e.getMessage());
        }

        group = new Group().setName("testVirtualServerNotExist").setAppId("000000")
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test2").setVirtualServer(slb.getVirtualServers().get(0)));
        groupRepository.add(group);
    }

    @Test
    public void testValidateGroup_duplicatePath() throws Exception {
        Slb slb = prepareSlb();
        Group group = new Group().setName("testDuplicatePath").setAppId("000000")
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(slb.getVirtualServers().get(0)))
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(slb.getVirtualServers().get(0)));
        try {
            groupRepository.add(group);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: duplicate path was found; real cause: " + e.getMessage());
        }

        group = new Group().setName("testDuplicatePath").setAppId("000000")
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(slb.getVirtualServers().get(0)));
        groupRepository.add(group);

        group = new Group().setName("testDuplicatePath1").setAppId("000000")
                .addGroupVirtualServer(new GroupVirtualServer().setPath("/test").setVirtualServer(slb.getVirtualServers().get(0)));
        try {
            groupRepository.add(group);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
            System.out.println("Expected cause: duplicate path was found; real cause: " + e.getMessage());
        }

        group = groupRepository.get("testDuplicatePath");
        groupRepository.update(group);
    }

    private Slb prepareSlb() throws Exception {
        if (slb4Group == null) {
            Slb slb = new Slb().setName("slb4Group").setStatus("Test")
                    .setNginxConf("/conf").setNginxBin("/bin").setNginxWorkerProcesses(3)
                    .addSlbServer(new SlbServer().setHostName("localslb.com").setIp("127.0.0.1"))
                    .addVirtualServer(new VirtualServer().setPort("8099").setName("VS_01").addDomain(new Domain().setName("localslb.com")));
            slbRepository.add(slb);
            slb4Group = slbRepository.get(slb.getName());
        }
        return slb4Group;
    }

    @AfterClass
    public static void tearDownDb() throws InterruptedException, ComponentLookupException, ComponentLifecycleException {
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }
}
