package com.ctrip.zeus.util;

import com.ctrip.zeus.model.entity.*;
import org.junit.Assert;

/**
 * Created by zhoumy on 2015/5/26.
 */
public class ModelAssert {
    public static void assertAppEquals(App expected, App actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getSsl() == null ? false : expected.getSsl().booleanValue(), actual.getSsl().booleanValue());
        Assert.assertEquals(expected.getAppServers().size(), actual.getAppServers().size());
        for (int i = 0; i < expected.getAppServers().size(); i++) {
            assertAppServerEquals(expected.getAppServers().get(i), actual.getAppServers().get(i));
        }
        Assert.assertEquals(expected.getAppSlbs().size(), actual.getAppSlbs().size());
        for (int i = 0; i < expected.getAppSlbs().size(); i++) {
            assertAppSlbEquals(expected.getAppSlbs().get(i), actual.getAppSlbs().get(i));
        }
        Assert.assertEquals(expected.getHealthCheck().getUri(), actual.getHealthCheck().getUri());
        Assert.assertEquals(expected.getLoadBalancingMethod().getType(), actual.getLoadBalancingMethod().getType());
    }

    public static void assertAppSlbEquals(AppSlb expected, AppSlb actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getSlbName(), actual.getSlbName());
        Assert.assertEquals(expected.getPath(), actual.getPath());
        if (expected.getRewrite() != null) {
            Assert.assertEquals(expected.getRewrite(), actual.getRewrite());
        }
        Assert.assertEquals(expected.getPriority() == null ? 0 : expected.getPriority().intValue(), actual.getPriority().intValue());
        assertVirtualServerEquals(expected.getVirtualServer(), actual.getVirtualServer());
    }

    public static void assertVirtualServerEquals(VirtualServer expected, VirtualServer actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getSsl(), actual.getSsl());
        Assert.assertEquals(expected.getDomains().size(), actual.getDomains().size());
        for (int i = 0; i < expected.getDomains().size(); i++) {
            Assert.assertEquals(expected.getDomains().get(i), actual.getDomains().get(i));
        }
    }

    public static void assertAppServerEquals(AppServer expected, AppServer actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getHostName(), actual.getHostName());
        Assert.assertEquals(expected.getIp(), actual.getIp());
    }

    public static void assertSlbEquals(Slb expected, Slb actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getNginxBin(), actual.getNginxBin());
        Assert.assertEquals(expected.getNginxConf(), actual.getNginxConf());
        Assert.assertEquals(expected.getNginxWorkerProcesses(), actual.getNginxWorkerProcesses());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());
        Assert.assertEquals(expected.getSlbServers().size(), actual.getSlbServers().size());
        Assert.assertEquals(expected.getVips().size(), actual.getVips().size());
        Assert.assertEquals(expected.getVirtualServers().size(), actual.getVirtualServers().size());
    }
}
