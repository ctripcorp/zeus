package com.ctrip.zeus.nginx;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatusList;
import com.ctrip.zeus.service.nginx.NginxService;
import org.junit.Assert;
import org.junit.Test;
import support.AbstractSpringTest;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/4/21.
 */
public class NginxStatusTest extends AbstractSpringTest {

    @Test
    public void testTrafficStatusApi() throws Exception {
        NginxClient client = new NginxClient("http://127.0.0.1:8099");
        TrafficStatusList status = client.getTrafficStatus();
        Assert.assertNotNull(status);
        Assert.assertTrue(status.getStatuses().get(0).getReqStatuses().size() > 0);
    }
}