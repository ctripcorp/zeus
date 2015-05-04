package com.ctrip.zeus.nginx;

import com.ctrip.zeus.nginx.entity.TrafficStatus;
import org.junit.Assert;
import org.junit.Test;
import support.AbstractSpringTest;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/4/21.
 */
public class NginxStatusTest extends AbstractSpringTest {

    @Resource
    private NginxStatusService nginxStatusService;

    @Test
    public void testTrafficStatus() {
        TrafficStatus status = nginxStatusService.getTrafficStatus();
        Assert.assertNotNull(status);
        Assert.assertTrue(status.getReqStatuses().size() > 0);
    }
}