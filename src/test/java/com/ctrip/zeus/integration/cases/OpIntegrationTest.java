package com.ctrip.zeus.integration.cases;

import com.ctrip.zeus.integration.AbstractCase;
import com.ctrip.zeus.integration.IntegrationData;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.status.transform.DefaultJsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by fanqq on 2015/8/20.
 */
public class OpIntegrationTest extends AbstractCase {
    /*
    *
    * Activate Group : 1,2,3,4
    */
    @Test
    public void ActivateGroup()throws Exception{
        String status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        GroupStatus gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(false, gs.getActivated());
        IntegrationData.getReqClient().get("/api/activate/group?groupName=__Test_app1&groupName=__Test_app2&groupName=__Test_app3&groupName=__Test_app4");
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(true,gs.getActivated());
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app2");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(true,gs.getActivated());
        Long vsid = IntegrationData.groups.get(1).getGroupVirtualServers().get(0).getVirtualServer().getId();
        String conf = IntegrationData.getReqClient().getstr("/api/nginx/conf?vs="+vsid);
        Assert.assertEquals(true,conf.contains("backend_"+IntegrationData.groups.get(1).getId()));
        vsid = IntegrationData.groups.get(2).getGroupVirtualServers().get(0).getVirtualServer().getId();
        conf = IntegrationData.getReqClient().getstr("/api/nginx/conf?vs="+vsid);
        Assert.assertEquals(true,conf.contains("backend_"+IntegrationData.groups.get(2).getId()));
    }
    /*
    *
    *deactivate Group:3,4
    */
    @Test
    public void DeactivateGroup()throws Exception{
        String status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app3");
        GroupStatus gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(true, gs.getActivated());
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app4");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(true, gs.getActivated());
        IntegrationData.getReqClient().get("/api/deactivate/group?groupName=__Test_app3&groupName=__Test_app4");
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app3");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(false,gs.getActivated());
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app4");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        Assert.assertEquals(false,gs.getActivated());
        Long vsid = IntegrationData.groups.get(3).getGroupVirtualServers().get(0).getVirtualServer().getId();
        String conf = IntegrationData.getReqClient().getstr("/api/nginx/conf?vs="+vsid);
        Assert.assertEquals(false,conf.contains("backend_"+IntegrationData.groups.get(3).getId()));
        vsid = IntegrationData.groups.get(4).getGroupVirtualServers().get(0).getVirtualServer().getId();
        conf = IntegrationData.getReqClient().getstr("/api/nginx/conf?vs="+vsid);
        Assert.assertEquals(false,conf.contains("backend_"+IntegrationData.groups.get(4).getId()));
    }
    /*
     * upServer and downServer
     */
    @Test
    public void upDownServer()throws Exception{
        String ip = IntegrationData.groups.get(1).getGroupServers().get(0).getIp();
        String status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        GroupStatus gs = DefaultJsonParser.parse(GroupStatus.class, status);
        List<GroupServerStatus> statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            if(gss.getIp().equals(ip)){
                Assert.assertEquals(true,gss.getServer());
                break;
            }
        }
        IntegrationData.getReqClient().getstr("/api/op/downServer?ip="+ip);
        Thread.sleep(1000);
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            if(gss.getIp().equals(ip)){
                Assert.assertEquals(false,gss.getServer());
                break;
            }
        }
        IntegrationData.getReqClient().getstr("/api/op/upServer?ip="+ip);
        Thread.sleep(1000);
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            if(gss.getIp().equals(ip)){
                Assert.assertEquals(true,gss.getServer());
                break;
            }
        }
    }
    /*
     * upMember and downMember
     */
    @Test
    public void upDownMember()throws Exception{
        // activated group , health check fail
        String status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        GroupStatus gs = DefaultJsonParser.parse(GroupStatus.class, status);
        List<GroupServerStatus> statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            Assert.assertEquals(false,gss.getMember());
            Assert.assertEquals(false,gss.getUp());
        }
        IntegrationData.getReqClient().getstr("/api/op/upMember?groupName=__Test_app1&batch=true");
        Thread.sleep(1000);
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app1");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            Assert.assertEquals(true,gss.getMember());
            Assert.assertEquals(false,gss.getUp());
        }
        // activated group , health check suc
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app2");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            Assert.assertEquals(false,gss.getMember());
            Assert.assertEquals(false,gss.getUp());
        }
        IntegrationData.getReqClient().getstr("/api/op/upMember?groupName=__Test_app2&batch=true");
        Thread.sleep(1000);
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app2");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            Assert.assertEquals(true,gss.getMember());
            Assert.assertEquals(true,gss.getUp());
        }
        // unactivated group
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app5");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        statusList = gs.getGroupServerStatuses();
        Assert.assertEquals(false,gs.getActivated());
        for (GroupServerStatus gss : statusList){
            Assert.assertEquals(false,gss.getMember());
            Assert.assertEquals(false,gss.getUp());
        }
        IntegrationData.getReqClient().getstr("/api/op/upMember?groupName=__Test_app5&batch=true");
        Thread.sleep(1000);
        status = IntegrationData.getReqClient().getstr("/api/status/group?groupName=__Test_app5");
        gs = DefaultJsonParser.parse(GroupStatus.class, status);
        statusList = gs.getGroupServerStatuses();
        for (GroupServerStatus gss : statusList){
            Assert.assertEquals(true,gss.getMember());
            Assert.assertEquals(true,gss.getUp());
        }
    }
}
