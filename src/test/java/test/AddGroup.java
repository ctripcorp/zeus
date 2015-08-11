//package test;
//
//import com.ctrip.zeus.ao.ReqClient;
//import com.ctrip.zeus.model.entity.*;
//
///**
// * Created by fanqq on 2015/6/11.
// */
//public class AddGroup {
//    private static final String host = "http://10.2.25.83:8099";
//    private static ReqClient reqClient = new ReqClient(host);
//
//    public static void main(String[] args) {
//        VirtualServer v1 = new VirtualServer().setName("ws.pay.uat.qa.nt.ctripcorp.com").setPort("80").setSsl(false)
//                .addDomain(new Domain().setName("ws.pay.uat.qa.nt.ctripcorp.com")).setId(5L);
//
//        GroupServer groupServer1 = new GroupServer().setPort(80).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("UAT0116").setIp("10.2.24.207");
//        GroupServer groupServer2 = new GroupServer().setPort(80).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("UAT0267").setIp("10.2.25.174");
//
//        Group group = new Group().setName("api-app").setAppId("10001").setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
//                .setIntervals(2000).setPasses(1).setUri("/")).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
//                .setValue("test"))
//                .addGroupSlb(new GroupSlb().addVip(new Vip().setIp("10.2.25.83")).setSlbId(1L)
//                        .setSlbName("uat").setPath("~*/api-app").setVirtualServer(v1).setRewrite("")
//                        .setPriority(0)).addGroupServer(groupServer1).addGroupServer(groupServer2);
//
////        reqClient.post("/api/group/new", String.format(Group.JSON, group));
//
//        VirtualServer v2 = new VirtualServer().setName("ws.security.pay.uat.qa.nt.ctripcorp.com").setPort("443").setSsl(true)
//                .addDomain(new Domain().setName("ws.security.pay.uat.qa.nt.ctripcorp.com")).setId(632L);
//
//        GroupServer groupServer3 = new GroupServer().setPort(443).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("UAT0116").setIp("10.2.24.207");
//        GroupServer groupServer4 = new GroupServer().setPort(443).setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("UAT0267").setIp("10.2.25.174");
//
//        String groupName = "api-web";
//        String appid = "10004";
//        String healthCheck = "/";
//        String path = "~*/api-web";
//
//        group = new Group().setName(groupName).setAppId(appid).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
//                .setIntervals(2000).setPasses(1).setUri(healthCheck)).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
//                .setValue("test"))
//                .addGroupSlb(new GroupSlb().setSlbId(1L)
//                        .setSlbName("uat").setPath(path).setVirtualServer(v2).setRewrite("")
//                        .setPriority(0)).addGroupServer(groupServer3).addGroupServer(groupServer4).setSsl(true);
//        reqClient.post("/api/group/new", String.format(Group.JSON, group));
//
//    }
//}
