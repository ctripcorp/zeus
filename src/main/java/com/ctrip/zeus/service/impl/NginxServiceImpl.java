package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.ConfReload;
import com.ctrip.zeus.nginx.ConfWriter;
import com.ctrip.zeus.nginx.NginxConf;
import com.ctrip.zeus.nginx.ServerConf;
import com.ctrip.zeus.service.AppRepository;
import com.ctrip.zeus.service.NginxService;
import com.ctrip.zeus.service.SlbRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Service("nginxService")
public class NginxServiceImpl implements NginxService {
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private AppRepository appRepository;

    @Override
    public void load() throws IOException {
        Slb slb = slbRepository.get("default");

        ConfWriter.writeNginxConf(slb, NginxConf.generate(slb));
        for (VirtualServer vs : slb.getVirtualServers()) {
            List<App> list = appRepository.list(slb.getName(), vs.getName());
            ConfWriter.writeServerConf(slb, vs, ServerConf.generate(slb, vs, list));
        }

        ConfReload.reload(slb);
    }
}
