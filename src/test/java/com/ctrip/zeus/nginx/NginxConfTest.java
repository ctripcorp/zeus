package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.Slb;
import org.junit.Test;

public class NginxConfTest {

    @Test
    public void testGenerate() throws Exception {
        System.out.println(NginxConf.generate(new Slb().setNginxWorkerProcesses(2)));
    }
}