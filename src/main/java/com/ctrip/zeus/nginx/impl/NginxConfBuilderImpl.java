package com.ctrip.zeus.nginx.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.NginxConfBuilder;
import com.ctrip.zeus.nginx.conf.NginxConf;
import com.ctrip.zeus.nginx.conf.ServerConf;
import com.ctrip.zeus.nginx.conf.UpstreamsConf;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/9/2015.
 */
@Service("nginxConfBuilder")
public class NginxConfBuilderImpl implements NginxConfBuilder {

    @Override
    public String generateNginxConf(Slb slb) {
        return NginxConf.generate(slb);
    }

    @Override
    public String generateServerConf(Slb slb, VirtualServer vs, List<App> apps) {
        return ServerConf.generate(slb, vs, apps);
    }

    @Override
    public String generateUpstreamsConf(Slb slb, VirtualServer vs, List<App> apps) {
        return UpstreamsConf.generate(slb, vs, apps);
    }
}
