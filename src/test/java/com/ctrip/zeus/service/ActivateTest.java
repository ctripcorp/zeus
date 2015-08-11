//package com.ctrip.zeus.service;
//
//import com.ctrip.zeus.ao.AbstractAPITest;
//import com.ctrip.zeus.ao.AopSpring;
//import com.ctrip.zeus.ao.Checker;
//import com.ctrip.zeus.ao.ReqClient;
//import com.ctrip.zeus.dal.core.*;
//import com.ctrip.zeus.model.entity.*;
//import com.ctrip.zeus.model.transform.DefaultSaxParser;
//import com.ctrip.zeus.service.build.NginxConfService;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.unidal.dal.jdbc.DalException;
//import org.xml.sax.SAXException;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.List;
//
///**
// * Created by fanqq on 2015/3/31.
// */
//public class ActivateTest extends AbstractAPITest {
//
//    @Resource
//    private ConfGroupActiveDao confGroupActiveDao;
//    @Resource
//    private ConfSlbActiveDao confSlbActiveDao;
//    @Resource
//    private NginxConfService nginxConfService;
//    static final String slb1_server_0 = "10.2.25.83";
//    static final String slb1_server_1 = "10.2.27.21";
//    static final String slb_name = "__Test_slb1";
//
//    @Before
//    public void before() throws Exception {
//
//        VirtualServer v1 = new VirtualServer().setName("__Test_vs1").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("vs1.ctrip.com"));
//        VirtualServer v2 = new VirtualServer().setName("__Test_vs2").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("vs2.ctrip.com"));
//
//        Slb slb = new Slb().setName(slb_name).addVip(new Vip().setIp(slb1_server_0)).setNginxBin("/opt/app/nginx/sbin")
//                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
//                .addSlbServer(new SlbServer().setHostName("slb1_server_0").setIp(slb1_server_0))
//                .addSlbServer(new SlbServer().setHostName("slb1_server_1").setIp(slb1_server_1))
//                .addVirtualServer(v1)
//                .addVirtualServer(v2)
//                .setStatus("Test");
//
//        new ReqClient("http://127.0.0.1:8099").post("/api/slb/new",String.format(Slb.JSON, slb));
//
//        GroupServer groupServer1 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver1").setIp(slb1_server_0);
//        GroupServer groupServer2 = new GroupServer().setPort(10001).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appserver2").setIp(slb1_server_1);
//
//        Group group = new Group().setName("__Test_app").setAppId("1000").setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
//                .setIntervals(2000).setPasses(1).setUri("/status.json")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
//                .setValue("test"))
//                .addGroupSlb(new GroupSlb().setSlbId(1L).setPath("/app").setVirtualServer(v2).setRewrite("")
//                        .setPriority(1)).addGroupServer(groupServer1)
//                .addGroupServer(groupServer2);
//
//        new ReqClient("http://127.0.0.1:8099").post("/api/group/new", String.format(Group.JSON, group));
//
//        new ReqClient("http://127.0.0.1:8099").get("/api/activate/slb?slbName="+slb_name);
//
//    }
//
//    @Test
//    public void activeteTest(){
//        AopSpring.addChecker("com.ctrip.zeus.service.activate.impl.ActivateServiceImpl.activate", new Checker() {
//            @Override
//            public void check() {
//                try {
//                    List<ConfGroupActiveDo> d = confGroupActiveDao.findAllByGroupIds(new Long[]{1L}, ConfGroupActiveEntity.READSET_FULL);
//                    ConfGroupActiveDo tmp = d.get(0);
//
//                    Assert.assertEquals(1,tmp.getGroupId());
//                    Assert.assertEquals(1,tmp.getVersion());
//
//                    Group group = DefaultSaxParser.parseEntity(Group.class, tmp.getContent());
//
//                    Assert.assertEquals("1000",group.getAppId());
//                    Assert.assertEquals("roundrobin",group.getLoadBalancingMethod().getType());
//                    Assert.assertEquals("test",group.getLoadBalancingMethod().getValue());
//                    Assert.assertEquals(1,group.getVersion().intValue());
//                    Assert.assertEquals("__Test_slb1",group.getGroupSlbs().get(0).getSlbName());
//                    Assert.assertEquals("/app",group.getGroupSlbs().get(0).getPath());
//                    Assert.assertEquals("__Test_vs2",group.getGroupSlbs().get(0).getVirtualServer().getName());
//                    Assert.assertEquals(false,group.getGroupSlbs().get(0).getVirtualServer().getSsl());
//                    Assert.assertEquals("80",group.getGroupSlbs().get(0).getVirtualServer().getPort());
//                    Assert.assertEquals("vs2.ctrip.com",group.getGroupSlbs().get(0).getVirtualServer().getDomains().get(0).getName());
//                    Assert.assertEquals(2000,group.getHealthCheck().getIntervals().intValue());
//                    Assert.assertEquals(1,group.getHealthCheck().getFails().intValue());
//                    Assert.assertEquals(1,group.getHealthCheck().getPasses().intValue());
//                    Assert.assertEquals("/status.json",group.getHealthCheck().getUri());
//                    Assert.assertEquals("appserver1",group.getGroupServers().get(0).getHostName());
//                    Assert.assertEquals("10.2.25.83",group.getGroupServers().get(0).getIp());
//                    Assert.assertEquals(30,group.getGroupServers().get(0).getFailTimeout().intValue());
//                    Assert.assertEquals(10,group.getGroupServers().get(0).getMaxFails().intValue());
//                    Assert.assertEquals(1,group.getGroupServers().get(0).getWeight().intValue());
//                    Assert.assertEquals(10001,group.getGroupServers().get(0).getPort().intValue());
//
//
//                    ConfSlbActiveDo slb = confSlbActiveDao.findBySlbId(1L, ConfSlbActiveEntity.READSET_FULL);
//
//                    Assert.assertEquals(1L,slb.getId());
//                    Assert.assertEquals(1,slb.getVersion());
//                    Slb slbentity = DefaultSaxParser.parseEntity(Slb.class,slb.getContent());
//
//                    Assert.assertEquals("/opt/app/nginx/sbin",slbentity.getNginxBin());
//                    Assert.assertEquals(1,slbentity.getNginxWorkerProcesses().intValue());
//                    Assert.assertEquals("/opt/app/nginx/conf",slbentity.getNginxConf());
//                    Assert.assertEquals(1,slbentity.getVersion().intValue());
//                    Assert.assertEquals(1,slbentity.getNginxWorkerProcesses().intValue());
//                    Assert.assertEquals("Test",slbentity.getStatus());
//
//
//                    Assert.assertEquals("10.2.25.83",slbentity.getVips().get(0).getIp());
//                    Assert.assertEquals("10.2.25.83",slbentity.getSlbServers().get(0).getIp());
//                    Assert.assertEquals("slb1_server_0",slbentity.getSlbServers().get(0).getHostName());
//                    Assert.assertEquals(true,slbentity.getSlbServers().get(0));
//
//
//                    Assert.assertEquals("__Test_vs1",slbentity.getVirtualServers().get(0).getName());
//                    Assert.assertEquals(false,slbentity.getVirtualServers().get(0).getSsl());
//                    Assert.assertEquals("80",slbentity.getVirtualServers().get(0).getPort());
//                    Assert.assertEquals("vs1.ctrip.com",slbentity.getVirtualServers().get(0).getDomains().get(0).getName());
//
//
//                    Assert.assertEquals("__Test_vs2",slbentity.getVirtualServers().get(1).getName());
//                    Assert.assertEquals(false,slbentity.getVirtualServers().get(1).getSsl());
//                    Assert.assertEquals("80",slbentity.getVirtualServers().get(1).getPort());
//                    Assert.assertEquals("vs2.ctrip.com",slbentity.getVirtualServers().get(1).getDomains().get(0).getName());
//
//                } catch (DalException e) {
//                    e.printStackTrace();
//                } catch (SAXException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        AopSpring.addChecker("BuildServiceImpl.build", new Checker() {
//
//            @Override
//            public void check() {
//                try {
//                    int version = nginxConfService.getCurrentVersion(1L);
//
//                    String str = nginxConfService.getNginxConf(1L,version);
//
//                    List<NginxConfServerData> confserverdata = nginxConfService.getNginxConfServer(1L, version);
//                    List<NginxConfUpstreamData> confupstreamdata= nginxConfService.getNginxConfUpstream(1L,version);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        new ReqClient("http://127.0.0.1:8099").get("/api/activate/group?groupName=__Test_app");
//
//    }
//}
