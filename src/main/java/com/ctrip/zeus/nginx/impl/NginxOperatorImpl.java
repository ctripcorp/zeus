package com.ctrip.zeus.nginx.impl;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.nginx.NginxOperator;
import com.ctrip.zeus.nginx.conf.ConfReload;
import com.ctrip.zeus.nginx.conf.ConfWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/9/2015.
 */
@Service("nginxOperator")
public class NginxOperatorImpl implements NginxOperator {
    @Override
    public void writeNginxConf(Slb slb, String conf) throws IOException {
            ConfWriter.writeNginxConf(slb, conf);
    }

    @Override
    public void writeServerConf(Slb slb, VirtualServer vs, String conf) throws IOException {
        ConfWriter.writeServerConf(slb, vs, conf);
    }

    @Override
    public void writeUpstreamsConf(Slb slb, VirtualServer vs, String conf) throws IOException {
        ConfWriter.writeUpstreamsConf(slb, vs, conf);
    }

    @Override
    public String reloadConf(Slb slb) {
        return ConfReload.reload(slb);
    }

    @Override
    public void markdownServer(App app, String ip) {
        //ToDo:
    }

    @Override
    public void markupServer(App app, String ip) {
        //ToDo:
    }
}
