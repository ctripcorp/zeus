package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.entity.ConfFile;
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
    public String generateServerConf(Slb slb, VirtualServer vs, List<Group> groups) throws Exception{
        return ServerConf.generate(slb,vs,groups);
    }

    @Override
    public List<ConfFile> generateUpstreamsConf(Slb slb, VirtualServer vs, List<Group> groups,
                                                Set<String> allDownServers, Set<String> allUpGroupServers,
                                                Set<String> visited) throws Exception {
        return UpstreamsConf.generate(slb, vs, groups, allDownServers, allUpGroupServers, visited);
    }
}
