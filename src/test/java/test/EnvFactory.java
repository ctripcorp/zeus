package test;

import com.ctrip.zeus.ao.ReqClient;
import com.ctrip.zeus.model.entity.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/5/8.
 */
public class EnvFactory {
    private static final String host = "http://127.0.0.1:8099";
//    private static final String host = "http://10.2.27.21:8099";
    private static final String vip="10.2.27.21";
    private static final int vsNum=10000;
    private static final int appNum=10000;
    private static final String slbName="test-env";
    private static final String checkHealthPath="/checkHealth";
    private static ReqClient reqClient = new ReqClient(host);
    private static String[] ipList = new String[]{"10.2.25.83","10.2.25.93","10.2.25.94","10.2.25.95","10.2.25.96"};
    private static int[] portList = new int[]{20001,20002,20003,20004};

    private static List<VirtualServer> vsList = new ArrayList<>();
    private static List<App> appList = new ArrayList<>();
    private static List<AppServer> appServerList = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        createVS(vsNum);
        createAppServer();

        Slb slb = new Slb().setName(slbName).addVip(new Vip().setIp(vip)).setNginxBin("/opt/app/nginx/sbin")
                .setNginxConf("/opt/app/nginx/conf").setNginxWorkerProcesses(1).setVersion(0)
                .addSlbServer(new SlbServer().setHostName("slbServer").setIp(vip).setEnable(true))
                .setStatus("Test");
        for ( VirtualServer vs : vsList )
        {
            slb.addVirtualServer(vs);
        }

        createApps();

        reqClient.post("/api/slb/add", String.format(Slb.JSON, slb));

        for ( App app : appList)
        {
            reqClient.post("/api/app/add", String.format(App.JSON, app));
        }

        System.out.println();

    }
    private static void createAppServer()
    {
        int count = ipList.length*portList.length;

        for ( int i = 0 ; i < count ; i++ ){
            String ip = ipList[i / portList.length];
            int port = portList[i % portList.length];
            appServerList.add(new AppServer().setPort(port)
                    .setFailTimeout(30).setWeight(1).setMaxFails(10).setHostName("appServer_"+i)
                    .setIp(ip));
        }
    }

    private static void createVS(int count){
        VirtualServer tmp ;

        for (int i = 0 ; i < count ; i++ )
        {
            tmp = new VirtualServer().setName("VS_"+i).setPort("80").setSsl(false)
                    .addDomain(new Domain().setName(""+i+".ctrip.com"));
            vsList.add(tmp);
        }
    }
    private static void createApps(){
        App apptmp = null;
        for ( int i = 0 ; i < appNum ; i++)
        {
            apptmp = new App().setName("App_" + i).setAppId(String.valueOf(100000 + i)).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                    .setIntervals(2000).setPasses(1).setUri(checkHealthPath)).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                    .setValue("test"))
                    .addAppSlb(new AppSlb().setSlbName(slbName).setPath("/App" + i)
                            .setVirtualServer(vsList.get(i % vsNum)));
            int tmp = i % portList.length;
            int portlength = portList.length;
            for ( int j = 0 ; j < ipList.length ; j++ )
            {
                apptmp.addAppServer(appServerList.get(j*portlength+tmp));
            }
            appList.add(apptmp);
        }
    }
}
