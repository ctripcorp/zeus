package com.ctrip.zeus.service.nginx.util;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.util.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhoumy on 2016/3/17.
 */
public class UpstreamConfPickerTest {

    private UpstreamConfPicker upstreamConfPicker = new UpstreamConfPicker();

    @Test
    public void testPickGroups() throws IOException, NginxProcessingException {
        String conf_893 = "";
        String conf_1109 = "";

        InputStream confFile = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/782.conf");
        NginxConfEntry entry = new NginxConfEntry().setVhosts(new Vhosts()).setUpstreams(new Upstreams());
        entry.getUpstreams().addConfFile(new ConfFile().setName("782").setContent(IOUtils.inputStreamStringify(confFile)));
        Set<Long> groupIds = new HashSet<>();
        groupIds.add(893L);
        groupIds.add(1109L);

        DyUpstreamOpsData[] result = upstreamConfPicker.pickByGroupIds(entry, groupIds);
        Assert.assertEquals(2, result.length);
        for (int i = 0; i < result.length; i++) {
            DyUpstreamOpsData d = result[i];
            if (d.getUpstreamName().equals("backend_893")) {
                Assert.assertEquals(conf_893, d.getUpstreamCommands());
            } else if (d.getUpstreamName().equals("backend_1109")) {
                Assert.assertEquals(conf_1109, d.getUpstreamCommands());
            } else {
                Assert.assertTrue(false);
            }
        }
    }

    @Test(expected = NginxProcessingException.class)
    public void testNginxProcessingException() throws IOException, NginxProcessingException {
        InputStream confFile = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/782.conf");
        NginxConfEntry entry = new NginxConfEntry().setVhosts(new Vhosts()).setUpstreams(new Upstreams());
        entry.getUpstreams().addConfFile(new ConfFile().setName("782").setContent(IOUtils.inputStreamStringify(confFile)));
        Set<Long> groupIds = new HashSet<>();
        groupIds.add(893L);
        groupIds.add(1109L);
        groupIds.add(1076L);
        groupIds.add(1077L);

        try {
            upstreamConfPicker.pickByGroupIds(entry, groupIds);
        } catch (NginxProcessingException ex) {
            String message = ex.getMessage();
            Assert.assertTrue(message.contains("1076"));
            Assert.assertTrue(message.contains("1077"));
            throw ex;
        }
    }

    @Test
    public void testParse() throws IOException, NginxProcessingException {
        final String conf_backend_backend_t = " server 10.15.142.7:80 weight=5 max_fails=0 fail_timeout=0; server 10.15.142.6:80 weight=5 max_fails=0 fail_timeout=0; server 10.15.142.5:80 weight=5 max_fails=0 fail_timeout=0; server 10.15.142.4:80 weight=5 max_fails=0 fail_timeout=0; keepalive 100; keepalive_timeout 110s; check interval=10000 rise=1 fall=10 timeout=2000 type=http default_down=false; check_keepalive_requests 100; check_http_send \"GET /confuse?param={} HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:v v.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\"; check_http_expect_alive http_2xx http_3xx;";
        final String conf_upstream = " server 10.15.142.7:80 weight=5 max_fails=0 fail_timeout=0; server 10.15.142.6:80 weight=5 max_fails=0 fail_timeout=0; server 10.15.142.5:80 weight=5 max_fails=0 fail_timeout=0; server 10.15.142.4:80 weight=5 max_fails=0 fail_timeout=0; keepalive 100; keepalive_timeout 110s; check interval=10000 rise=1 fall=10 timeout=2000 type=http default_down=false; check_keepalive_requests 100; check_http_send \"GET /confuse?param= { HTTP/1.0\\r\\nConnection:keep-alive\\r\\nHost:dd}.com\\r\\nUserAgent:SLB_HealthCheck\\r\\n\\r\\n\"; check_http_expect_alive http_2xx http_3xx;";
        InputStream confFile = this.getClass().getClassLoader().getResourceAsStream("com.ctrip.zeus.service/confuse.conf");
        upstreamConfPicker.parse(IOUtils.inputStreamStringify(confFile), new UpstreamConfPicker.UpstreamDirectiveDelegate() {
            @Override
            public void delegate(String upstreamName, String content) {
                if (upstreamName.equals("backend_backend_t")) {
                    Assert.assertEquals(conf_backend_backend_t, content);
                } else if (upstreamName.equals("upstream")) {
                    Assert.assertEquals(conf_upstream, content);
                } else {
                    Assert.assertTrue(false);
                }
            }
        });
    }
}
