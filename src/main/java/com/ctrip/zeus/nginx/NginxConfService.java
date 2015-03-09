package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/9/2015.
 */
public interface NginxConfService {
    String generateNginxConf(Slb slb);
    String generateNginxServerConf(Slb slb, VirtualServer vs, List<App> apps);
}
