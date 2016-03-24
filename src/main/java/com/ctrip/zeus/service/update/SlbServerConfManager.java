package com.ctrip.zeus.service.update;

import com.ctrip.zeus.nginx.entity.NginxResponse;

/**
 * Created by fanqq on 2016/3/15.
 */
public interface SlbServerConfManager {
    /**
     * update slb Server nginx configs
     */
    NginxResponse update(boolean refresh , boolean reload) throws Exception;

    /**
     * refresh slb Server nginx configs;
     */
    NginxResponse update() throws Exception;
}
