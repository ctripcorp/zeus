package test;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.nginx.entity.S;
import com.ctrip.zeus.nginx.entity.Servers;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.nginx.impl.DefaultNginxStatus;

import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/19/2015.
 */
public class Demo3 {

    public static void main(String[] args) throws IOException {
        UpstreamStatus s1 = new UpstreamStatus().setServers(
                new Servers().setTotal(1)
                        .setGeneration(2)
                        .addS(new S().setIndex(1).setUpstream("hello").setName("10.8.9.9").setStatus("up").setRise(111).setFall(222).setType("http").setPort(0))
        );
        System.out.println(String.format(UpstreamStatus.JSON, s1));
        NginxClient c = new NginxClient("http://10.2.25.93:10001");
        UpstreamStatus s = c.getUpstreamStatus();
        System.out.println(s.getServers().getServer());
        new DefaultNginxStatus(s);
    }
}
