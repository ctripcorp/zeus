package test;

import com.ctrip.zeus.model.entity.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/5/8.
 */
public class EnvFactory {
    private static final String host = "http://10.2.27.21:8099";
    private static final String vip="10.2.27.21";
    private static final int vsNum=10;
    private static final int appNum=100;
    private static final String slbName="test-env";
    private static final String checkHealthPath="/checkHealth";


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

        System.out.println();

    }
    private static void createAppServer()
    {
        String[] ipList = new String[]{"10.2.25.83","10.2.25.93","10.2.25.94","10.2.25.95","10.2.25.96"};
        int[] portList = new int[]{20001,20002,20003,20004};

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
            apptmp = new App().setName("App_"+ i).setAppId(String.valueOf(100000 + i)).setVersion(1).setHealthCheck(new HealthCheck().setFails(1)
                    .setIntervals(2000).setPasses(1).setUri(checkHealthPath)).setLoadBalancingMethod(new LoadBalancingMethod().setType("roundrobin")
                    .setValue("test"))
                    .addAppSlb(new AppSlb().setSlbName(slbName).setPath( "App" + i )
                            .setVirtualServer(vsList.get(i%vsNum)));
            for (AppServer appServer : appServerList){
                apptmp.addAppServer(appServer);
            }
            appList.add(apptmp);
        }
    }
}
