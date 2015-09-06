package com.ctrip.zeus.util;

import com.ctrip.zeus.ao.ReqClient;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fanqq on 2015/9/6.
 */
public class ClientTest {

    @Test
    public void Test() throws InterruptedException {
        List<String> list = new ArrayList<>();

        list.add("/upstream/backend_5358");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /ask/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5359");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /comment/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5357");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /activitiesservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5367");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30 down;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /schedule/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5366");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /newmemberservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5365");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /memberservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5364");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /globalfood/checkhealth.json HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5363");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /destmerchant/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");

        list.add("/upstream/backend_5362");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /destinationservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5361");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /dangdiwebservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5360");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /dangdi/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5375");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /weatherservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5374");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /url/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5373");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /triptrack/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5376");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /official/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5372");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /travelservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");
        list.add("/upstream/backend_5371");
        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
                "check_keepalive_requests 1;\n" +
                "check_http_send \"GET /traveller/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
                "check_http_expect_alive http_2xx http_3xx;");

//        list.add("/upstream/backend_5370");
//        list.add("server 10.2.40.114:80 weight=1 max_fails=0 fail_timeout=30;\n" +
//                "server 10.2.40.115:80 weight=1 max_fails=0 fail_timeout=30;\n" +
//                "check interval=5000 rise=1 fall=5 timeout=1000 type=http;\n" +
//                "check_keepalive_requests 1;\n" +
//                "check_http_send \"GET /topservice/slbhealthcheck.html HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
//                "check_http_expect_alive http_2xx http_3xx;");
//        list.add("/upstream/backend_725");
//        list.add("server 10.2.40.114:80 weight=5 max_fails=0 fail_timeout=30;\n" +
//                "server 10.2.40.115:80 weight=5 max_fails=0 fail_timeout=30;\n" +
//                "check interval=2000 rise=1 fall=5 timeout=1000 type=http;\n" +
//                "check_keepalive_requests 1;\n" +
//                "check_http_send \"GET / HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:service.you.uat.qa.nt.ctripcorp.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\";\n" +
//                "check_http_expect_alive http_2xx http_3xx;");

        ReqClient reqClient = new ReqClient("http://10.2.25.95:8081");

        for (int i = 0; i < list.size() ; i ++)
        {
//            Thread.sleep(2000);
            Response response = reqClient.post(list.get(i),list.get(++i));
            System.out.println(response);
        }

    }
}
