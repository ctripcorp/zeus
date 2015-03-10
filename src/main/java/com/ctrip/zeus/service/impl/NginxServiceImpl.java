package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.NginxConfService;
import com.ctrip.zeus.nginx.NginxOperator;
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

    @Resource
    private NginxConfService nginxConfService;
    @Resource
    private NginxOperator nginxOperator;

    @Override
    public void load() throws IOException {
        Slb slb = slbRepository.get("default");

        String nginxConf = nginxConfService.generateNginxConf(slb);
        nginxOperator.writeNginxConf(slb, nginxConf);
        for (VirtualServer vs : slb.getVirtualServers()) {
            List<App> list = appRepository.list(slb.getName(), vs.getName());

            String upstreamsConf = nginxConfService.generateUpstreamsConf(slb, vs, list);
            nginxOperator.writeUpstreamsConf(slb, vs, upstreamsConf);

            String serverConf = nginxConfService.generateServerConf(slb, vs, list);
            nginxOperator.writeServerConf(slb, vs, serverConf);
        }

        nginxOperator.reloadConf(slb);
    }
}
