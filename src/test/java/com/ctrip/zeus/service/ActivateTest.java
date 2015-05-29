package com.ctrip.zeus.service;

import com.ctrip.zeus.ao.AbstractAPITest;
import com.ctrip.zeus.ao.AopSpring;
import com.ctrip.zeus.ao.Checker;
import com.ctrip.zeus.ao.ReqClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.NginxConfServerData;
import com.ctrip.zeus.model.entity.NginxConfUpstreamData;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.build.NginxConfService;
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
    @Resource
    private NginxConfService nginxConfService;

    @Before
    public void before() throws Exception {
        new ReqClient("http://127.0.0.1:8099").post("/api/slb/add","{\n" +
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

        new ReqClient("http://127.0.0.1:8099").post("/api/app/add","{\n" +
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
    public void activeteTest(){
        AopSpring.addChecker("com.ctrip.zeus.service.activate.impl.ActivateServiceImpl.activate", new Checker() {
            @Override
            public void check() {
                try {
                    List<ConfAppActiveDo> d = confAppActiveDao.findAllByNames(new String[]{"Test"}, ConfAppActiveEntity.READSET_FULL);
                    ConfAppActiveDo tmp = d.get(0);

                    Assert.assertEquals("Test",tmp.getName());
                    Assert.assertEquals(1,tmp.getVersion());

                    App app = DefaultSaxParser.parseEntity(App.class, tmp.getContent());

                    Assert.assertEquals("921812",app.getAppId());
                    Assert.assertEquals("roundrobin",app.getLoadBalancingMethod().getType());
                    Assert.assertEquals("test",app.getLoadBalancingMethod().getValue());
                    Assert.assertEquals(1,app.getVersion().intValue());
                    Assert.assertEquals("default",app.getAppSlbs().get(0).getSlbName());
                    Assert.assertEquals("/",app.getAppSlbs().get(0).getPath());
                    Assert.assertEquals("site1",app.getAppSlbs().get(0).getVirtualServer().getName());
                    Assert.assertEquals(false,app.getAppSlbs().get(0).getVirtualServer().getSsl());
                    Assert.assertEquals("80",app.getAppSlbs().get(0).getVirtualServer().getPort());
                    Assert.assertEquals("s1.ctrip.com",app.getAppSlbs().get(0).getVirtualServer().getDomains().get(0).getName());
                    Assert.assertEquals(5000,app.getHealthCheck().getIntervals().intValue());
                    Assert.assertEquals(1,app.getHealthCheck().getFails().intValue());
                    Assert.assertEquals(2,app.getHealthCheck().getPasses().intValue());
                    Assert.assertEquals("/domaininfo/OnService.html",app.getHealthCheck().getUri());
                    Assert.assertEquals("0",app.getAppServers().get(0).getHostName());
                    Assert.assertEquals("10.2.6.201",app.getAppServers().get(0).getIp());
                    Assert.assertEquals(30,app.getAppServers().get(0).getFailTimeout().intValue());
                    Assert.assertEquals(2,app.getAppServers().get(0).getMaxFails().intValue());
                    Assert.assertEquals(1,app.getAppServers().get(0).getWeight().intValue());
                    Assert.assertEquals(8080,app.getAppServers().get(0).getPort().intValue());


                    ConfSlbActiveDo slb = confSlbActiveDao.findByName("default", ConfSlbActiveEntity.READSET_FULL);

                    Assert.assertEquals("default",slb.getName());
                    Assert.assertEquals(1,slb.getVersion());
                    Slb slbentity = DefaultSaxParser.parseEntity(Slb.class,slb.getContent());

                    Assert.assertEquals("/opt/app/nginx/sbin",slbentity.getNginxBin());
                    Assert.assertEquals(2,slbentity.getNginxWorkerProcesses().intValue());
                    Assert.assertEquals("/opt/app/nginx/conf",slbentity.getNginxConf());
                    Assert.assertEquals(1,slbentity.getVersion().intValue());
                    Assert.assertEquals(2,slbentity.getNginxWorkerProcesses().intValue());
                    Assert.assertEquals("TEST",slbentity.getStatus());


                    Assert.assertEquals("101.2.25.93",slbentity.getVips().get(0).getIp());
                    Assert.assertEquals("101.2.25.93",slbentity.getSlbServers().get(0).getIp());
                    Assert.assertEquals("uat0358",slbentity.getSlbServers().get(0).getHostName());
                    Assert.assertEquals(true,slbentity.getSlbServers().get(0).getEnable());


                    Assert.assertEquals("site1",slbentity.getVirtualServers().get(0).getName());
                    Assert.assertEquals(false,slbentity.getVirtualServers().get(0).getSsl());
                    Assert.assertEquals("80",slbentity.getVirtualServers().get(0).getPort());
                    Assert.assertEquals("s1.ctrip.com",slbentity.getVirtualServers().get(0).getDomains().get(0).getName());


                    Assert.assertEquals("site2",slbentity.getVirtualServers().get(1).getName());
                    Assert.assertEquals(false,slbentity.getVirtualServers().get(1).getSsl());
                    Assert.assertEquals("80",slbentity.getVirtualServers().get(1).getPort());
                    Assert.assertEquals("s2a.ctrip.com",slbentity.getVirtualServers().get(1).getDomains().get(0).getName());
                    Assert.assertEquals("s2b.ctrip.com",slbentity.getVirtualServers().get(1).getDomains().get(1).getName());

                } catch (DalException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        AopSpring.addChecker("BuildServiceImpl.build", new Checker() {

            @Override
            public void check() {
                try {
                    int version = nginxConfService.getCurrentVersion("default");

                    String str = nginxConfService.getNginxConf("default",version);

                    List<NginxConfServerData> confserverdata = nginxConfService.getNginxConfServer("default", version);
                    List<NginxConfUpstreamData> confupstreamdata= nginxConfService.getNginxConfUpstream("default",version);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        new ReqClient("http://127.0.0.1:8099").post("/api/conf/activate","{\n" +
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
