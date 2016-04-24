package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.build.conf.NginxConf;
import org.junit.Test;

import javax.annotation.Resource;

public class NginxConfTest {

    @Resource
    NginxConf nginxConf;

    @Test
    public void testGenerate() throws Exception {
        System.out.println(nginxConf.generate(new Slb().setNginxWorkerProcesses(2)));
    }
}