package com.ctrip.zeus.integration.cases;

import com.ctrip.zeus.integration.AbstractCase;
import com.ctrip.zeus.integration.IntegrationData;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.transform.DefaultJsonParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fanqq on 2015/8/20.
 */
public class ActivateGroupTest extends AbstractCase {
    @Test
    public void ActivateGroup()throws Exception{
        String status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        GroupStatus gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(false,gs.getActivated());
        IntegrationData.getReqClient().get("/api/activate/group?groupName=__Test_app1&groupName=__Test_app2");
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(true,gs.getActivated());
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app2");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(true,gs.getActivated());
        Long vsid = IntegrationData.groups.get(0).getGroupVirtualServers().get(0).getVirtualServer().getId();
        String conf = IntegrationData.getReqClient().getstr("/api/nginx/conf?vs="+vsid);
        Assert.assertEquals(true,conf.contains("backend_"+IntegrationData.groups.get(0).getId()));
        vsid = IntegrationData.groups.get(1).getGroupVirtualServers().get(1).getVirtualServer().getId();
        conf = IntegrationData.getReqClient().getstr("/api/nginx/conf?vs="+vsid);
        Assert.assertEquals(true,conf.contains("backend_"+IntegrationData.groups.get(1).getId()));
    }
}
