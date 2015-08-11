//package test;
//
//import com.ctrip.zeus.ao.ReqClient;
//import com.ctrip.zeus.model.entity.*;
//import com.ctrip.zeus.model.transform.DefaultJsonParser;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
//* Created by fanqq on 2015/5/8.
//*/
//public class EnvFactory {
////    private static final String host = "http://127.0.0.1:8099";
//    private static final String host = "http://10.2.27.21:8099";
////    private static final String vip="127.0.0.1";
//    private static final String vip="10.2.27.21";
//    private static final int vsNum=1;
//    private static final int groupNum=10;
//    private static final String slbName="test-env";
//    private static final String checkHealthPath="/checkHealth";
//    private static final boolean isActivate=true;
//    private static ReqClient reqClient = new ReqClient(host);
//    private static String[] ipList = new String[]{"10.2.25.83"};
////    private static int[] portList = new int[]{20001,20002,20003,20004};
//    private static int[] portList = new int[]{20001};
//
//    private static List<VirtualServer> vsList = new ArrayList<>();
//    private static List<Group> groupList = new ArrayList<>();
//    private static List<GroupServer> groupServerList = new ArrayList<>();
//
//    public static void main(String[] args) throws IOException {
//
//        createVS(vsNum);
//        createGroupServer();
//
//        Slb slb = new Slb().setName(slbName).addVip(new Vip().setIp(vip)).setNginxBin("/opt/app/nginx/sbin")
//                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
//                .addSlbServer(new SlbServer().setHostName("slbServer").setIp(vip))
//                .setStatus("Test");
//        for ( VirtualServer vs : vsList )
//        {
//            slb.addVirtualServer(vs);
//        }
//
//        reqClient.post("/api/slb/add", String.format(Slb.JSON, slb));
//
//        createGroups();
//
//        for ( Group app : groupList)
//        {
//            reqClient.post("/api/group/add", String.format(Group.JSON, app));
//        }
//
//        if (!isActivate)
//        {
//            return;
//        }
//        reqClient.get("/api/conf/activateByName?slbName="+slbName);
//        for (int index = 0 ; index < groupNum ; index++)
//        {
//            reqClient.get("/api/conf/activateByName?groupName=App_"+index);
//        }
//        System.out.println();
//
//    }
//    private static void createGroupServer()
//    {
//        int count = ipList.length*portList.length;
//
//        for ( int i = 0 ; i < count ; i++ ){
//            String ip = ipList[i / portList.length];
//            int port = portList[i % portList.length];
//            groupServerList.add(new GroupServer().setPort(port)
//                    .setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("groupServer_"+i)
//                    .setIp(ip));
//        }
//    }
//
//    private static void createVS(int count){
//        VirtualServer tmp ;
//
//        for (int i = 0 ; i < count ; i++ )
//        {
//            tmp = new VirtualServer().setName("VS_"+i).setPort("80").setSsl(false)
//                    .addDomain(new Domain().setName(""+i+".ctrip.com"));
//            vsList.add(tmp);
//        }
//    }
//    private static void createGroups() throws IOException {
//        Group grouptmp = null;
//        String slbstr = reqClient.getstr("/api/slb/get/"+slbName);
//        Slb slb = DefaultJsonParser.parse(Slb.class, slbstr);
//        for ( int i = 0 ; i < groupNum ; i++)
//        {
//            grouptmp = new Group().setName("App_" + i).setAppId(String.valueOf(100000 + i)).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
//                    .setIntervals(2000 * groupNum / 100).setPasses(1).setUri(checkHealthPath)).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
//                    .setValue("test"))
//                    .addGroupSlb(new GroupSlb().setSlbId(slb.getId()).setSlbName(slbName).setPath("/App" + i).setRewrite("/App(01|02)(abb.+|app.+) /App$1/$2?sleep=1&size=1")
//                            .setVirtualServer(vsList.get(i % vsNum)).setPriority(i));
//            int tmp = i % portList.length;
//            int portlength = portList.length;
//            for ( int j = 0 ; j < ipList.length ; j++ )
//            {
//                grouptmp.addGroupServer(groupServerList.get(j * portlength + tmp));
//            }
//            groupList.add(grouptmp);
//        }
//    }
//}
