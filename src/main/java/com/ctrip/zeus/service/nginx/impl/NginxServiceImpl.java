package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.model.entity.AppList;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.NginxConfBuilder;
import com.ctrip.zeus.nginx.NginxOperator;
import com.ctrip.zeus.service.model.AppRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.model.SlbRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

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
    private NginxConfBuilder nginxConfBuilder;
    @Resource
    private NginxOperator nginxOperator;

    @Override
    public String load() throws IOException {
        Slb slb = slbRepository.get("default");

        String nginxConf = nginxConfBuilder.generateNginxConf(slb);
        nginxOperator.writeNginxConf(slb, nginxConf);
        for (VirtualServer vs : slb.getVirtualServers()) {
            AppList appList = appRepository.list(slb.getName(), vs.getName());

            String upstreamsConf = nginxConfBuilder.generateUpstreamsConf(slb, vs, appList.getApps());
            nginxOperator.writeUpstreamsConf(slb, vs, upstreamsConf);

            String serverConf = nginxConfBuilder.generateServerConf(slb, vs, appList.getApps());
            nginxOperator.writeServerConf(slb, vs, serverConf);
        }

        return nginxOperator.reloadConf(slb);
    }
}
