package com.ctrip.zeus.service;

import com.ctrip.zeus.ao.AbstractAPITest;
import com.ctrip.zeus.ao.AopSpring;
import com.ctrip.zeus.ao.Checker;
import com.ctrip.zeus.ao.ReqClient;
import com.ctrip.zeus.service.status.StatusService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;


/**
 * Created by fanqq on 2015/4/2.
 */
public class StatusTest extends AbstractAPITest {

    @Resource
    StatusService statusService;

    @Before
    public void before() throws Exception {
        new ReqClient("http://127.0.0.1:8099").request("/api/slb/add","{\n" +
                "    \"name\": \"default\",\n" +
                "    \"version\": 7,\n" +
                "    \"nginx-bin\": \"/opt/app/nginx/sbin\",\n" +
                "    \"nginx-conf\": \"/opt/app/nginx/conf\",\n" +
                "    \"nginx-worker-processes\": 2,\n" +
                "    \"status\": \"TEST\",\n" +
                "    \"vips\": [\n" +
                "        {\n" +
                "            \"ip\": \"101.2.25.93\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"slb-servers\": [\n" +
                "        {\n" +
                "            \"ip\": \"101.2.25.93\",\n" +
                "            \"host-name\": \"uat0358\",\n" +
                "            \"enable\": true\n" +
                "        },\n" +
                "        {\n" +
                "            \"ip\": \"101.2.25.94\",\n" +
                "            \"host-name\": \"uat0359\",\n" +
                "            \"enable\": true\n" +
                "        },\n" +
                "        {\n" +
                "            \"ip\": \"101.2.25.95\",\n" +
                "            \"host-name\": \"uat0360\",\n" +
                "            \"enable\": true\n" +
                "        }\n" +
                "    ],\n" +
                "    \"virtual-servers\": [\n" +
                "        {\n" +
                "            \"name\": \"site1\",\n" +
                "            \"ssl\": false,\n" +
                "            \"port\": \"80\",\n" +
                "            \"domains\": [\n" +
                "                {\n" +
                "                    \"name\": \"s1.ctrip.com\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"site2\",\n" +
                "            \"ssl\": false,\n" +
                "            \"port\": \"80\",\n" +
                "            \"domains\": [\n" +
                "                {\n" +
                "                    \"name\": \"s2a.ctrip.com\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"s2b.ctrip.com\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        new ReqClient("http://127.0.0.1:8099").request("/api/app/add","{\n" +
                "    \"name\": \"Test\",\n" +
                "    \"app-id\": \"921812\",\n" +
                "    \"version\": 1,\n" +
                "    \"app-slbs\": [\n" +
                "        {\n" +
                "            \"slb-name\": \"default\",\n" +
                "            \"path\": \"/\",\n" +
                "            \"virtual-server\": {\n" +
                "                \"name\": \"site1\",\n" +
                "                \"ssl\": false,\n" +
                "                \"port\": \"80\",\n" +
                "                \"domains\": [\n" +
                "                    {\n" +
                "                        \"name\": \"s1.ctrip.com\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"health-check\": {\n" +
                "        \"intervals\": 5000,\n" +
                "        \"fails\": 1,\n" +
                "        \"passes\": 2,\n" +
                "        \"uri\": \"/domaininfo/OnService.html\"\n" +
                "    },\n" +
                "    \"load-balancing-method\": {\n" +
                "        \"type\": \"roundrobin\",\n" +
                "        \"value\": \"test\"\n" +
                "    },\n" +
                "    \"app-servers\": [\n" +
                "        {\n" +
                "            \"port\": 8080,\n" +
                "            \"weight\": 1,\n" +
                "            \"max-fails\": 2,\n" +
                "            \"fail-timeout\": 30,\n" +
                "            \"host-name\": \"0\",\n" +
                "            \"ip\": \"101.2.6.201\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"port\": 80,\n" +
                "            \"weight\": 2,\n" +
                "            \"max-fails\": 2,\n" +
                "            \"fail-timeout\": 30,\n" +
                "            \"host-name\": \"0\",\n" +
                "            \"ip\": \"101.2.6.202\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

    }

    @Test
    public void statusTest()
    {
        AopSpring.addChecker("StatusServiceImpl.upServer", new Checker() {

            @Override
            public void check() {
                try {
                    Assert.assertEquals(true,statusService.getServerStatus("101.2.6.201"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AopSpring.addChecker("StatusServiceImpl.downServer", new Checker() {

            @Override
            public void check() {
                try {
                    Assert.assertEquals(false,statusService.getServerStatus("101.2.6.201"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        new ReqClient("http://127.0.0.1:8099").request("/api/conf/activate","{\n" +
                "   \"conf-slb-names\": [\n" +
                "      {\n" +
                "         \"slbname\": \"default\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"conf-app-names\": [\n" +
                "      {\n" +
                "         \"appname\": \"Test\"\n" +
                "      }\n" +
                "   ]\n" +
                "}\n");

        String responseup = new ReqClient("http://127.0.0.1:8099/api/op/upServer?ip=101.2.6.201").request();
        String responsedown = new ReqClient("http://127.0.0.1:8099/api/op/downServer?ip=101.2.6.201").request();
        System.out.println(responseup);
        System.out.println(responsedown);

        String responseupM = new ReqClient("http://127.0.0.1:8099/api/op/upMember?appName=Test&ip=101.2.6.201").request();
        String responsedownM = new ReqClient("http://127.0.0.1:8099/api/op/downMember?appName=Test&ip=101.2.6.201").request();

        System.out.println(responseupM);
        System.out.println(responsedownM);
    }
}
