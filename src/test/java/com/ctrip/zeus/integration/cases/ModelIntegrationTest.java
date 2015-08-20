package com.ctrip.zeus.integration.cases;

import com.ctrip.zeus.integration.AbstractCase;
import com.ctrip.zeus.integration.IntegrationData;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fanqq on 2015/8/20.
 */
public class ModelIntegrationTest  extends AbstractCase {

    @Test
    public void MemberModelTest()throws Exception{
        // /members
        Group group = IntegrationData.groups.get(5);
        String members = IntegrationData.getReqClient().getstr("/api/members?groupId="+group.getId());
        GroupServerList groupServerList = DefaultJsonParser.parse(GroupServerList.class,members);
        Assert.assertEquals(true,group.getGroupServers().containsAll(groupServerList.getGroupServers()));
        Assert.assertEquals(true, groupServerList.getGroupServers().containsAll(group.getGroupServers()));

        // member/add
        groupServerList.getGroupServers().get(0).setIp("10.2.25.96");
        GroupServer groupServer = groupServerList.getGroupServers().get(0);
        IntegrationData.getReqClient().post("/api/member/add",String.format(GroupServerList.JSON,groupServerList));
        members = IntegrationData.getReqClient().getstr("/api/members?groupId="+group.getId());
        groupServerList = DefaultJsonParser.parse(GroupServerList.class,members);
        Assert.assertEquals(true, groupServerList.getGroupServers().contains(groupServer));
        String groupStr = IntegrationData.getReqClient().getstr("/api/group?groupId="+group.getId());
        Group grouptmp = DefaultJsonParser.parse(Group.class,groupStr);
        Assert.assertEquals(true,grouptmp.getGroupServers().containsAll(groupServerList.getGroupServers()));
        Assert.assertEquals(true, groupServerList.getGroupServers().containsAll(grouptmp.getGroupServers()));

        //member/update
        for (GroupServer gs : groupServerList.getGroupServers()){
            if (gs.getIp().equals("10.2.25.96")){
                gs.setFailTimeout(22);
            }
        }
        IntegrationData.getReqClient().post("/api/member/update",String.format(GroupServerList.JSON,groupServerList));
        members = IntegrationData.getReqClient().getstr("/api/members?groupId=" + group.getId());
        groupServerList = DefaultJsonParser.parse(GroupServerList.class,members);
        for (GroupServer gs : groupServerList.getGroupServers()){
            if (gs.getIp().equals("10.2.25.96")){
                Assert.assertEquals(true,gs.getFailTimeout().equals(22));
            }
        }
        groupStr = IntegrationData.getReqClient().getstr("/api/group?groupId="+group.getId());
        grouptmp = DefaultJsonParser.parse(Group.class,groupStr);
        Assert.assertEquals(true,grouptmp.getGroupServers().containsAll(groupServerList.getGroupServers()));
        Assert.assertEquals(true, groupServerList.getGroupServers().containsAll(grouptmp.getGroupServers()));

        // remove
        IntegrationData.getReqClient().get("/api/member/remove?ip=10.2.25.96&groupId="+group.getId());
        members = IntegrationData.getReqClient().getstr("/api/members?groupId="+group.getId());
        groupServerList = DefaultJsonParser.parse(GroupServerList.class,members);
        for (GroupServer gs : groupServerList.getGroupServers()){
            Assert.assertNotEquals("10.2.25.96",gs.getIp());
        }
        groupStr = IntegrationData.getReqClient().getstr("/api/group?groupId="+group.getId());
        grouptmp = DefaultJsonParser.parse(Group.class,groupStr);
        Assert.assertEquals(true,grouptmp.getGroupServers().containsAll(groupServerList.getGroupServers()));
        Assert.assertEquals(true, groupServerList.getGroupServers().containsAll(grouptmp.getGroupServers()));
    }
    @Test
    public void VirtualServerModelTest()throws Exception{
        // /vses
        Slb slb = IntegrationData.slb1;
        String vses = IntegrationData.getReqClient().getstr("/api/vses?slbId="+slb.getId());
        VirtualServerList virtualServerList = DefaultJsonParser.parse(VirtualServerList.class,vses);
        Assert.assertEquals(true,slb.getVirtualServers().containsAll(virtualServerList.getVirtualServers()));
        Assert.assertEquals(true, virtualServerList.getVirtualServers().containsAll(slb.getVirtualServers()));
        // /vs
        VirtualServer vs = slb.getVirtualServers().get(0);
        String vsStr = IntegrationData.getReqClient().getstr("/api/vs?vsId="+vs.getId());
        VirtualServer virtualServer = DefaultJsonParser.parse(VirtualServer.class,vsStr);
        Assert.assertEquals(true,vs.equals(virtualServer));

        // /vs/new
        virtualServer.setName("Test");
        virtualServer.getDomains().clear();
        virtualServer.addDomain(new Domain().setName("Test.com"));
        IntegrationData.getReqClient().post("/api/vs/new",String.format(VirtualServer.JSON,virtualServer));
        vses = IntegrationData.getReqClient().getstr("/api/vses?slbId="+slb.getId());
        virtualServerList = DefaultJsonParser.parse(VirtualServerList.class,vses);
        for (VirtualServer virtualServer1 : virtualServerList.getVirtualServers()){
            if (virtualServer1.getName().equals("Test")){
                virtualServer.setId(virtualServer1.getId());
                Assert.assertEquals(true,virtualServer.equals(virtualServer1));
            }
        }
        Assert.assertEquals(true,virtualServerList.getVirtualServers().contains(virtualServer));
        String slbStr = IntegrationData.getReqClient().getstr("/api/slb?slbId="+slb.getId());
        Slb slbRes = DefaultJsonParser.parse(Slb.class,slbStr);
        Assert.assertEquals(true,slbRes.getVirtualServers().contains(virtualServer));

        // /vs/update
        virtualServer.setPort("443");
        IntegrationData.getReqClient().post("/api/vs/update",String.format(VirtualServer.JSON,virtualServer));
        vses = IntegrationData.getReqClient().getstr("/api/vses?slbId="+slb.getId());
        virtualServerList = DefaultJsonParser.parse(VirtualServerList.class,vses);
        Assert.assertEquals(true,virtualServerList.getVirtualServers().contains(virtualServer));
        slbStr = IntegrationData.getReqClient().getstr("/api/slb?slbId="+slb.getId());
        slbRes = DefaultJsonParser.parse(Slb.class,slbStr);
        Assert.assertEquals(true,slbRes.getVirtualServers().contains(virtualServer));

        // /vs/delete
        IntegrationData.getReqClient().get("/api/vs/delete?vsId="+virtualServer.getId());
        vses = IntegrationData.getReqClient().getstr("/api/vses?slbId="+slb.getId());
        virtualServerList = DefaultJsonParser.parse(VirtualServerList.class,vses);
        Assert.assertEquals(false,virtualServerList.getVirtualServers().contains(virtualServer));
        slbStr = IntegrationData.getReqClient().getstr("/api/slb?slbId="+slb.getId());
        slbRes = DefaultJsonParser.parse(Slb.class,slbStr);
        Assert.assertEquals(false,slbRes.getVirtualServers().contains(virtualServer));
    }
}
