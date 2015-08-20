package com.ctrip.zeus.integration;

import com.ctrip.zeus.ao.ReqClient;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultJsonParser;
import com.ctrip.zeus.util.ModelAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/8/19.
 */
public class IntegrationData {
    public static final String host = "http://10.2.25.83:8099";
    public static final String server_83 = "10.2.25.83";
    public static final String server_95 = "10.2.25.95";
    public static final String slb1_name = "__Test_slb1";
    public static final String slb2_name = "__Test_slb2";
    public static final int GROUP_NUM = 10;
    public static ReqClient reqClient = new ReqClient(host);
    public static final List<Group> groups = new ArrayList<>();
    public static Slb slb1 = null;
    public static Slb slb2 = null;

    public static ReqClient getReqClient(){
        return reqClient;
    }
    public static void clean() throws IOException {
        String groupstr;
        Group groupres;
        for (int i = 0; i < GROUP_NUM; i++) {
            try{
                reqClient.getstr("/api/deactivate/group?groupName=__Test_app"+i);
                reqClient.markPass("/api/deactivate/group");
                groupstr = reqClient.getstr("/api/group?groupName=__Test_app"+i);
            }catch (Exception e)
            {
                System.out.print(e);
                continue;
            }
            groupres = DefaultJsonParser.parse(Group.class, groupstr);
            if (groupres!=null)
            {
                reqClient.getstr("/api/group/delete?groupId=" + groupres.getId());
                reqClient.markPass("/api/group/delete");
            }
        }
        try{
            String slb_res = reqClient.getstr("/api/slb?slbName=" + slb1_name);
            Slb slb_res_obj  = DefaultJsonParser.parse(Slb.class, slb_res);
            if (slb_res_obj!=null)
            {
                reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
                reqClient.markPass("/api/slb/delete");
            }
            slb_res = reqClient.getstr("/api/slb?slbName=" + slb2_name);
            slb_res_obj  = DefaultJsonParser.parse(Slb.class, slb_res);
            if (slb_res_obj!=null)
            {
                reqClient.getstr("/api/slb/delete?slbId=" + slb_res_obj.getId());
            }
        }catch (Exception e){
            System.out.print(e);
        }

    }
    public static void init()throws Exception{
        VirtualServer v1 = new VirtualServer().setName("__Test_vs1").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs1.ctrip.com"));
        VirtualServer v2 = new VirtualServer().setName("__Test_vs2").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs2.ctrip.com"))
                .addDomain(new Domain().setName("vs6.ctrip.com"));
        VirtualServer v3 = new VirtualServer().setName("__Test_vs3").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs3.ctrip.com"));
        VirtualServer v4 = new VirtualServer().setName("__Test_vs4").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs4.ctrip.com"));
        VirtualServer v5 = new VirtualServer().setName("__Test_vs5").setPort("80").setSsl(false)
                .addDomain(new Domain().setName("vs5.ctrip.com"));
        //up
        GroupServer groupServer1 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("server_83").setIp(server_83);
        //down
        GroupServer groupServer2 = new GroupServer().setPort(1000).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("server_95").setIp(server_95);
        Slb orgslb1 = new Slb().setName(slb1_name).addVip(new Vip().setIp(server_83)).setNginxBin("/opt/app/nginx/sbin")
                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addSlbServer(new SlbServer().setHostName("server_83").setIp(server_83))
                .addVirtualServer(v1)
                .addVirtualServer(v2)
                .addVirtualServer(v3)
                .addVirtualServer(v4)
                .addVirtualServer(v5)
                .setStatus("Test");
        Slb orgslb2 = new Slb().setName(slb2_name).addVip(new Vip().setIp(server_95)).setNginxBin("/opt/app/nginx/sbin")
                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addSlbServer(new SlbServer().setHostName("server_95").setIp(server_95))
                .addVirtualServer(v1)
                .addVirtualServer(v2)
                .addVirtualServer(v3)
                .addVirtualServer(v4)
                .addVirtualServer(v5)
                .setStatus("Test");
        reqClient.post("/api/slb/new", String.format(Slb.JSON, orgslb1));
        reqClient.post("/api/slb/new", String.format(Slb.JSON, orgslb2));
        String slb1_res = reqClient.getstr("/api/slb?slbName=" + slb1_name);
        slb1 = DefaultJsonParser.parse(Slb.class, slb1_res);
        ModelAssert.assertSlbEquals(slb1, orgslb1);
        slb1_res = reqClient.getstr("/api/slb?slbName=" + slb2_name);
        slb2 = DefaultJsonParser.parse(Slb.class, slb1_res);
        ModelAssert.assertSlbEquals(slb2, orgslb2);
        reqClient.markPass("/api/slb/new");
        reqClient.markPass("/api/slb");
        for (int i = 0 ; i < GROUP_NUM ; i++ )
        {
            Group group = new Group().setName("__Test_app" + i).setAppId("1000" + i).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                    .setIntervals(5000).setPasses(1).setUri("/status.json").setFails(1)).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                    .setValue("test"))
                    .addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(i % 3 == 0 ? slb2.getVirtualServers().get(i % 4) : slb1.getVirtualServers().get(i % 4))
                            .setPath("~* ^/app" + i).setRewrite(i % 2 == 0 ? null : "\"/app1\" /app?sleep=1&size=2;\"/app\" /app0?sleep=1&size=1" + i)
                            .setPriority(i)).addGroupServer(i % 2 == 0 ? groupServer1 : groupServer2);
            reqClient.post("/api/group/new", String.format(Group.JSON, group));
            String groupstr = reqClient.getstr("/api/group?groupName=__Test_app"+i);
            Group groupres = DefaultJsonParser.parse(Group.class, groupstr);
            ModelAssert.assertGroupEquals(group, groupres);
            groups.add(groupres);
        }
        reqClient.markPass("/api/group/new");
        Thread.sleep(1000);
        reqClient.getstr("/api/activate/slb?slbName=__Test_slb1&slbName=__Test_slb2");
    }
}
