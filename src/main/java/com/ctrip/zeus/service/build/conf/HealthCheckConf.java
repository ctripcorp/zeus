package com.ctrip.zeus.service.build.conf;


import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.HealthCheck;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringListProperty;
import com.netflix.config.DynamicStringProperty;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class HealthCheckConf {

    private static DynamicBooleanProperty disableHealthCheck = DynamicPropertyFactory.getInstance().getBooleanProperty("build.disable.healthCheck", false);
    private static DynamicStringProperty sslHelloEnableList = DynamicPropertyFactory.getInstance().getStringProperty("build.sslHello.enable", "");
    private static DynamicStringProperty disableHealthCheckList = DynamicPropertyFactory.getInstance().getStringProperty("build.disable.healthCheck.groupId", "");


    public static String generate(Slb slb, VirtualServer vs, Group group) throws Exception {
        if (disableHealthCheck.get())
        {
            return "";
        }
        String []disableList = disableHealthCheckList.get().split(";");
        for (String groupId : disableList)
        {
            if (String.valueOf(group.getId()).equals(groupId.trim()))
            {
                return "";
            }
        }
        HealthCheck h = group.getHealthCheck();
        if (h == null)
        {
            return "";
        }
        AssertUtils.assertNotNull(h.getIntervals(), "Group HealthCheck Intervals config is null!");
        AssertUtils.assertNotNull(h.getFails(), "Group HealthCheck Fails config is null!");
        AssertUtils.assertNotNull(h.getPasses(), "Group HealthCheck Passes config is null!");
        AssertUtils.assertNotNull(h.getUri(), "Group HealthCheck Uri config is null!");


        StringBuilder b = new StringBuilder(128);

        if (group.getSsl()&&sslHelloEnable(group.getId())) {
            b.append("check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(1000)
                    .append(" type=ssl_hello").append(";\n");
        }else {
            b.append("check interval=").append(h.getIntervals())
                    .append(" rise=").append(h.getPasses())
                    .append(" fall=").append(h.getFails())
                    .append(" timeout=").append(1000);
            if (group.getSsl()){
                b.append(" port=").append(80);
            }
            b.append(" type=http default_down=false").append(";\n")
                    .append("check_keepalive_requests 100").append(";\n")
                    .append("check_http_send \"")
                    .append("GET ").append(h.getUri()).append(" HTTP/1.0\\r\\n")
                    .append("Connection:keep-alive\\r\\n");
            if (!h.getUri().equalsIgnoreCase("/SlbHealthCheck.aspx")) {
                b.append("Host:").append(vs.getDomains().get(0).getName().trim()).append("\\r\\n");
            }
            b.append("UserAgent:SLB_HealthCheck").append("\\r\\n\\r\\n\"").append(";\n")
                    .append("check_http_expect_alive http_2xx http_3xx").append(";\n");
        }
        return b.toString();
    }
    private static boolean sslHelloEnable(Long groupId){
        String []list = sslHelloEnableList.get().split(",");
        for (String id : list)
        {
            if (id!=null && String.valueOf(groupId).equals(id.trim()))
            {
                return true;
            }
        }
        return  false;
    }
}
