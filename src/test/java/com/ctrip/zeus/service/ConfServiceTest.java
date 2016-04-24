package com.ctrip.zeus.service;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import javax.annotation.Resource;
import java.io.File;

/**
 * Created by lu.wang on 2016/4/20.
 */
public class ConfServiceTest extends AbstractServerTest {

    @Resource
    ConfService confService;

    @Test
    public void test1() {
        DynamicStringProperty stringProperty =
                DynamicPropertyFactory.getInstance().getStringProperty("nginx.server.proxy.buffer.size.default", "aa");
        System.out.println(stringProperty.get());
    }

    @Test
    @Ignore
    public void testGetStringValue() {
        try {
            String value;
            //nginxConf
            value = confService.getStringValue("logLevel", null, null, null, "");
            Assert.assertEquals("", value);

            //serverConf
            value = confService.getStringValue("server.proxy.buffer.size", 2L, null, null, "8k");
            Assert.assertEquals("15k", value);

            value = confService.getStringValue("server.proxy.buffers", 3L, null, null, "8 8k");
            Assert.assertEquals("8 8k", value);

            value = confService.getStringValue("server.proxy.busy.buffers.size", null, null, null, "8k");
            Assert.assertEquals("8k", value);

            value = confService.getStringValue("server.errorPage.accept", 3L, null, null, "text/html");
            Assert.assertEquals("text/html;application/xml", value);

            value = confService.getStringValue("server.vs.health.check.gif.base64", null, null, null, null);
            Assert.assertNull(value);

            value = confService.getStringValue("server.errorPage.host.url", null, null, null, null);
            Assert.assertEquals("http://test.test", value);

        } catch (Exception e) {
            Assert.fail("Catch exception when testGetStringValue method." + e.getMessage());
        }
    }

    @Test
    @Ignore
    public void testGetIntValue() {
        try {

            int value;
            //nginxConf
            value = confService.getIntValue("statusPort", null, null, null, 10001);
            Assert.assertEquals(10001, value);

            value = confService.getIntValue("serverNames.maxSize", null, null, null, 10000);
            Assert.assertEquals(10000, value);

            value = confService.getIntValue("serverNames.bucketSize", null, null, null, 128);
            Assert.assertEquals(128, value);

            value = confService.getIntValue("checkShmSize", null, null, null, 32);
            Assert.assertEquals(32, value);

            value = confService.getIntValue("dyups.port", null, null, null, 8081);
            Assert.assertEquals(8081, value);

            //upstreamConf
            value = confService.getIntValue("upstream.keepAlive", 3L, 123L, 999999L, 100);
            Assert.assertEquals(300, value);

            value = confService.getIntValue("upstream.keepAlive.timeout", 3L, 123L, 999999L, 110);
            Assert.assertEquals(100, value);

        } catch (Exception e) {
            Assert.fail("Catch exception when testGetIntValue method." + e.getMessage());
        }
    }

    @Test
    @Ignore
    public void testGetEnable() {
        try {
            boolean value;
            //serverConf
            value = confService.getEnable("server.errorPage", 3L, null, null, true);
            Assert.assertFalse(value);

            value = confService.getEnable("server.errorPage.use.new", 3L, null, null, true);
            Assert.assertFalse(value);

            value = confService.getEnable("server.proxy.buffer.size", 3L, null, null, false);
            Assert.assertTrue(value);

            value = confService.getEnable("server.vs.health.check", 2L, null, null, false);
            Assert.assertFalse(value);

            //upstreamConf
            value = confService.getEnable("upstream.keepAlive", 3L, 123L, 999999L, false);
            Assert.assertTrue(value);

            value = confService.getEnable("upstream.keepAlive.timeout", 3L, 123L, 999999L, false);
            Assert.assertTrue(value);

        } catch (Exception e) {
            Assert.fail("Catch exception when testGetEnable method." + e.getMessage());
        }


    }

    @Test
    public void test() {
        
    }

}
