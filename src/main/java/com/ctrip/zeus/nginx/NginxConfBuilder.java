package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/9/2015.
 */
public interface NginxConfBuilder {
    String generateNginxConf(Slb slb);

    String generateServerConf(Slb slb, VirtualServer vs, List<App> apps);

    String generateUpstreamsConf(Slb slb, VirtualServer vs, List<App> apps);
}
