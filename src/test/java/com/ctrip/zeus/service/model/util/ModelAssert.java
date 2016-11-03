package com.ctrip.zeus.service.model.util;

import com.ctrip.zeus.model.entity.*;
import org.junit.Assert;

/**
 * Created by zhoumy on 2015/5/26.
 */
public class ModelAssert {
    public static void assertGroupEquals(Group expected, Group actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getSsl() == null ? false : expected.getSsl().booleanValue(), actual.getSsl().booleanValue());
        Assert.assertEquals(expected.getGroupServers().size(), actual.getGroupServers().size());
        for (int i = 0; i < expected.getGroupServers().size(); i++) {
            assertGroupServerEquals(expected.getGroupServers().get(i), actual.getGroupServers().get(i));
        }
        Assert.assertEquals(expected.getGroupVirtualServers().size(), actual.getGroupVirtualServers().size());
        for (int i = 0; i < expected.getGroupVirtualServers().size(); i++) {
            assertGroupVsEquals(expected.getGroupVirtualServers().get(i), actual.getGroupVirtualServers().get(i));
        }
        Assert.assertEquals(expected.getHealthCheck().getUri(), actual.getHealthCheck().getUri());
        Assert.assertEquals(expected.getLoadBalancingMethod().getType(), actual.getLoadBalancingMethod().getType());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }

    public static void assertGroupVsEquals(GroupVirtualServer expected, GroupVirtualServer actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getPath(), actual.getPath());
        if (expected.getRewrite() != null) {
            Assert.assertEquals(expected.getRewrite(), actual.getRewrite());
        }
        Assert.assertEquals(expected.getPriority() == null ? 1000 : expected.getPriority().intValue(), actual.getPriority().intValue());
        Assert.assertEquals(expected.getVirtualServer().getId(), actual.getVirtualServer().getId());
    }

    public static void assertVirtualServerEquals(VirtualServer expected, VirtualServer actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getSsl(), actual.getSsl());
        Assert.assertEquals(expected.getDomains().size(), actual.getDomains().size());
        for (int i = 0; i < expected.getDomains().size(); i++) {
            Assert.assertEquals(expected.getDomains().get(i), actual.getDomains().get(i));
        }
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }

    public static void assertGroupServerEquals(GroupServer expected, GroupServer actual) {
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
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
    }
}
