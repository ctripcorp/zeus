package com.ctrip.zeus.nginx.impl;

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
    public void writeNginxConf(String path, String conf) throws IOException {
            ConfWriter.writeNginxConf(path, conf);
    }

    @Override
    public void writeServerConf(String path, String conf) throws IOException {
        ConfWriter.writeServerConf(path, conf);
    }

    @Override
    public void writeUpstreamsConf(String path, String conf) throws IOException {
        ConfWriter.writeUpstreamsConf(path, conf);
    }

    @Override
    public String reloadConf(String command) {
        return ConfReload.reload(command);
    }

}
