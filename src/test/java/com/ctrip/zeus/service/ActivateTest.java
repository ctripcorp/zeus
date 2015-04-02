package com.ctrip.zeus.service;

import com.ctrip.zeus.ao.AbstractAPITest;
import com.ctrip.zeus.ao.AopSpring;
import com.ctrip.zeus.ao.Checker;
import com.ctrip.zeus.ao.ReqClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * Created by fanqq on 2015/3/31.
 */
public class ActivateTest extends AbstractAPITest {

    @Resource
    private ConfAppActiveDao confAppActiveDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;

    private String expectedSlbContent = null;
    private String expectedAppContent = null;

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
                "            \"ip\": \"10.2.6.201\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"port\": 80,\n" +
                "            \"weight\": 2,\n" +
                "            \"max-fails\": 2,\n" +
                "            \"fail-timeout\": 30,\n" +
                "            \"host-name\": \"0\",\n" +
                "            \"ip\": \"10.2.6.202\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

    }

    @Test
    public void activeteTest(){
        AopSpring.addChecker("com.ctrip.zeus.service.Activate.impl.ActivateServiceImpl.activate", new Checker() {
            @Override
            public void check() {
                try {
                    List<ConfAppActiveDo> d = confAppActiveDao.findAllByNames(new String[]{"Test"}, ConfAppActiveEntity.READSET_FULL);
                    ConfAppActiveDo tmp = d.get(0);

                    Assert.assertEquals("Test",tmp.getName());
                    Assert.assertEquals(1,tmp.getVersion());

                    App app = DefaultSaxParser.parseEntity(App.class, tmp.getContent());

                    Assert.assertEquals("921812",app.getAppId());
//                    Assert.assertEquals("roundrobin",app.getLoadBalancingMethod().getType());
//                    Assert.assertEquals("test",app.getLoadBalancingMethod().getValue());

                    ConfSlbActiveDo slb = confSlbActiveDao.findByName("default", ConfSlbActiveEntity.READSET_FULL);

                    Assert.assertEquals("default",slb.getName());
                    Assert.assertEquals(1,slb.getVersion());
                    Slb slbentity = DefaultSaxParser.parseEntity(Slb.class,slb.getContent());

                    Assert.assertEquals("/opt/app/nginx/sbin",slbentity.getNginxBin());
                    Assert.assertEquals(2,slbentity.getNginxWorkerProcesses().intValue());
                    Assert.assertEquals("/opt/app/nginx/conf",slbentity.getNginxConf());


                } catch (DalException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        AopSpring.addChecker("BuildInfoServiceImpl.", new Checker() {

            @Override
            public void check() {

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

    }
}
