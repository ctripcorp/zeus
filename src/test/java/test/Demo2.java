package test;

import com.ctrip.zeus.model.entity.AppServerStatus;
import com.ctrip.zeus.model.entity.AppStatus;
import com.ctrip.zeus.model.entity.ServerStatus;

/**
 * @author:xingchaowang
 * @date: 3/19/2015.
 */
public class Demo2 {

    public static void main(String[] args) {
        AppStatus s = new AppStatus().setAppName("html5Home")
                .addAppServerStatus(new AppServerStatus()
                        .setIp("192.168.1.1")
                        .setServer(true).setMember(true).setUp(true))
                .addAppServerStatus(new AppServerStatus()
                        .setIp("192.168.1.2")
                        .setServer(true).setMember(true).setUp(true));


        ServerStatus s1 = new ServerStatus()
                .setIp("192.168.1.1").setUp(true)
                .addAppName("html5Home")
                .addAppName("html5Hotel")
                .addAppName("html5Flight");

        System.out.println(String.format(AppStatus.JSON, s));
        System.out.println(String.format(AppStatus.JSON, s1));
    }
}
