package com.ctrip.zeus.service.nginx.util;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.VsConfData;
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
        Map<Long, VsConfData> vsConf = new HashMap<>();
        vsConf.put(782L, new VsConfData().setUpstreamConf(IOUtils.inputStreamStringify(confFile)));
        Set<Long> groupIds = new HashSet<>();
        groupIds.add(893L);
        groupIds.add(1109L);

        DyUpstreamOpsData[] result = upstreamConfPicker.pickByGroupIds(vsConf, groupIds);
        Assert.assertEquals(2, result.length);
        for (int i = 0; i < result.length; i++) {
            DyUpstreamOpsData d = result[i];
            if (d.getUpstreamName().equals("upstream_893")) {
                Assert.assertEquals(conf_893, d.getUpstreamCommands());
            } else if (d.getUpstreamCommands().equals("upstream_1109")) {
                Assert.assertEquals(conf_1109, d.getUpstreamCommands());
            }
        }
    }
}
