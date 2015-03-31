package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.build.NginxConfBuilder;
import com.ctrip.zeus.service.build.conf.NginxConf;
import com.ctrip.zeus.service.build.conf.ServerConf;
import com.ctrip.zeus.service.build.conf.UpstreamsConf;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/3/30.
 */
@Service("nginxConfigBuilder")
public class NginxConfBuilderImpl implements NginxConfBuilder {
    @Override
    public String generateNginxConf(Slb slb) {
        return NginxConf.generate(slb);
    }

    @Override
    public String generateServerConf(Slb slb, VirtualServer vs, List<App> apps) {
        return ServerConf.generate(slb,vs,apps);
    }

    @Override
    public String generateUpstreamsConf(Slb slb, VirtualServer vs, List<App> apps, Set<String> allDownServers, Set<String> allDownAppServers) {
        return UpstreamsConf.generate(slb,vs,apps,allDownServers,allDownAppServers);
    }
}
