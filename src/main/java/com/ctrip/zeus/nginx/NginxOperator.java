package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.io.IOException;

/**
 * @author:xingchaowang
 * @date: 3/9/2015.
 */
public interface NginxOperator {

    void writeNginxConf(Slb slb, String conf) throws IOException;

    void writeServerConf(Slb slb, VirtualServer vs, String conf) throws IOException;

    void writeUpstreamsConf(Slb slb, VirtualServer vs, String conf) throws IOException;

    String reloadConf(Slb slb);

    void markdownServer(App app, String ip);

    void markupServer(App app, String ip);
}
