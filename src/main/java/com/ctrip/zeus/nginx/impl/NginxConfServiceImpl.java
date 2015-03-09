package com.ctrip.zeus.nginx.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.NginxConfService;
import com.ctrip.zeus.nginx.conf.NginxConf;
import com.ctrip.zeus.nginx.conf.ServerConf;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/9/2015.
 */
@Service("nginxConfService")
public class NginxConfServiceImpl implements NginxConfService {

    @Override
    public String generateNginxConf(Slb slb) {
        return NginxConf.generate(slb);
    }

    @Override
    public String generateNginxServerConf(Slb slb, VirtualServer vs, List<App> apps) {
        return ServerConf.generate(slb, vs, apps);
    }
}
