package com.ctrip.zeus.service;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.auth.util.AuthUserUtil;
import com.ctrip.zeus.client.GroupClient;
import com.ctrip.zeus.client.SlbClient;
import com.ctrip.zeus.client.VirtualServerClient;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.entity.ConfFile;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.build.conf.LocationConf;
import com.ctrip.zeus.service.build.conf.NginxConf;
import com.ctrip.zeus.service.build.conf.ServerConf;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lu.wang on 2016/4/20.
 */
public class ConfigHandlerTest extends AbstractServerTest {

    @Resource
    ConfigHandler configHandler;
    @Resource
    NginxConf nginxConf;
    @Resource
    ServerConf serverConf;
    @Resource
    UpstreamsConf upstreamsConf;
    @Resource
    LocationConf locationConf;

    @BeforeClass
    public static void beforeClass() {
        String appName = ConfigurationManager.getDeploymentContext().getApplicationId();
        try {
            ConfigurationManager.loadCascadedPropertiesFromResources(appName);
        } catch (IOException e) {
            Assert.fail("Fail to load properties.");
        }
    }

    @Test
    public void testLoad() {
        DynamicStringProperty stringProperty =
                DynamicPropertyFactory.getInstance().getStringProperty("nginx.location.bastion.white.list.default", "aa");
        Assert.assertEquals("10.32.*", stringProperty.get());
    }

    @Test
    public void testGetKeyType() {
        String type = configHandler.getKeyType("ip.authentication", AuthUserUtil.getAuthUsers(), "10.32.20.131");
        Assert.assertEquals("sdong", type);

        type = configHandler.getKeyType("ip.authentication", AuthUserUtil.getAuthUsers(), "10.32.64.37");
        Assert.assertEquals("lsqiu", type);

        type = configHandler.getKeyType("ip.authentication", AuthUserUtil.getAuthUsers(), "192.168.18.215");
        Assert.assertEquals("opSys", type);

        type = configHandler.getKeyType("ip.authentication", AuthUserUtil.getAuthUsers(), "172.16.226.137");
        Assert.assertEquals("releaseSys", type);

        type = configHandler.getKeyType("ip.authentication", AuthUserUtil.getAuthUsers(), "1.2.3.4");
        Assert.assertNull(type);
    }

    @Test
    public void testGetStringValue() {
        try {
            String value;
            //testKey1
            value = configHandler.getStringValue("upstream.testKey1", null, 1L, 1L, "");
            Assert.assertEquals("testKey1_group1_value", value);
            value = configHandler.getStringValue("upstream.testKey1", null, 1L, 2L, "");
            Assert.assertEquals("testKey1_vs1_value", value);
            value = configHandler.getStringValue("upstream.testKey1", 1L, 2L, 2L, "");
            Assert.assertEquals("testKey1_slb1_value", value);
            value = configHandler.getStringValue("upstream.testKey1", 2L, 2L, 2L, "code_value");
            Assert.assertEquals("code_value", value);

            value = configHandler.getStringValue("upstream.testKey2", 1L, 1L, 1L, "code_value");
            Assert.assertEquals("testKey2_default_value", value);

        } catch (Exception e) {
            Assert.fail("Catch exception when testGetStringValue method." + e.getMessage());
        }
    }

    @Test
    public void testGetIntValue() {
        try {
            int value;
            value = configHandler.getIntValue("location.testKey1", 1L, 1L, 1L, 10);
            Assert.assertEquals(4, value);
            value = configHandler.getIntValue("location.testKey1", 1L, 1L, 2L, 10);
            Assert.assertEquals(3, value);
            value = configHandler.getIntValue("location.testKey1", 1L, 2L, 2L, 10);
            Assert.assertEquals(2, value);
            value = configHandler.getIntValue("location.testKey1", 2L, 2L, 2L, 10);
            Assert.assertEquals(1, value);
            value = configHandler.getIntValue("location.testKey1", 3L, 3L, 3L, 10);
            Assert.assertEquals(1, value);

        } catch (Exception e) {
            Assert.fail("Catch exception when testGetIntValue method." + e.getMessage());
        }
    }

    @Test
    public void testGetEnable() {
        try {
            boolean value;
            //testKey1
            value = configHandler.getEnable("server.testKey1", null, null, 1L, false);
            Assert.assertTrue(value);
            value = configHandler.getEnable("server.testKey1", null, null, 2L, false);
            Assert.assertFalse(value);
            value = configHandler.getEnable("server.testKey1", null, null, null, false);
            Assert.assertFalse(value);
            value = configHandler.getEnable("server.testKey1", null, null, null, true);
            Assert.assertTrue(value);

            //testKey2
            value = configHandler.getEnable("server.testKey2", null, 1L, null, false);
            Assert.assertTrue(value);
            value = configHandler.getEnable("server.testKey2", null, 1L, 1L, false);
            Assert.assertTrue(value);
            value = configHandler.getEnable("server.testKey2", null, 1L, 10L, false);
            Assert.assertTrue(value);
            value = configHandler.getEnable("server.testKey2", null, 2L, null, false);
            Assert.assertFalse(value);
            value = configHandler.getEnable("server.testKey2", 1L, 2L, 1L, false);
            Assert.assertFalse(value);

            //testKey3
            value = configHandler.getEnable("server.testKey3", 1L, null, null, false);
            Assert.assertTrue(value);
            value = configHandler.getEnable("server.testKey3", 1L, 1L, 1L, false);
            Assert.assertTrue(value);

            //testKey4
            value = configHandler.getEnable("server.testKey4", 1L, 1L, 1L, false);
            Assert.assertTrue(value);

        } catch (Exception e) {
            Assert.fail("Catch exception when testGetEnable method." + e.getMessage());
        }
    }

    @Test
    public void testConf() {
        try {
            String slbUrl = "http://10.2.25.93:8099/";

            SlbClient slbClient = new SlbClient(slbUrl);
            Slb slb = slbClient.get("VS_Slb.uat_80"); //slbId=73

            /*nginxConf.generate*/
            String result = nginxConf.generate(slb);
            String actualContext = deleteCRLFOnce(result);

            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/conf/nginx.conf");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int i = inputStream.read();
            while(i != -1){
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            String exceptContext = byteArrayOutputStream.toString();
            exceptContext = deleteCRLFOnce(exceptContext);
            Assert.assertEquals(exceptContext, actualContext);

            /*serverConf.generate*/
            VirtualServerClient vsClient = new VirtualServerClient(slbUrl);
            VirtualServer vs = vsClient.get("632");

            GroupClient groupClient = new GroupClient(slbUrl);
            List<Group> groupList = groupClient.getGroupsByVsId("632");

            result = serverConf.generate(slb, vs, groupList); //slbId=3; virtualServerId=632;
            actualContext = deleteCRLFOnce(result);

            inputStream = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/conf/vhosts_632.conf");
            byteArrayOutputStream = new ByteArrayOutputStream();
            i = inputStream.read();
            while(i != -1){
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            exceptContext = deleteCRLFOnce(byteArrayOutputStream.toString());

            Assert.assertEquals(exceptContext, actualContext);


            /*upstreamConf.generate*/
            Set<String> allDownServers = new HashSet<>();
            Set<String> allUpServers = new HashSet<>();

            for (Group group : groupList) {
                List<GroupServer> groupServerList = group.getGroupServers();
                for (GroupServer groupServer : groupServerList) {
                    allUpServers.add(vs.getId() + "_" + group.getId() + "_" + groupServer.getIp());
                }
            }

            Set<String> visited = new HashSet<>();
            List<ConfFile> res = upstreamsConf.generate(null, vs, groupList, allDownServers, allUpServers, visited);
            actualContext = deleteCRLFOnce(res.get(0).getContent());

            inputStream = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/conf/upstreams_632.conf");
            byteArrayOutputStream = new ByteArrayOutputStream();
            i = inputStream.read();
            while(i != -1){
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            exceptContext = deleteCRLFOnce(byteArrayOutputStream.toString());
            Assert.assertEquals(exceptContext, actualContext);

        } catch (Exception e) {
            Assert.fail("Catch exception when testConf method.");
        }
    }

    private String deleteCRLFOnce(String input) {
        return input.replaceAll("\\r\\n", "\n");
    }


}
