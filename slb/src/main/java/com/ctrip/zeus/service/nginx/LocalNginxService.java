package com.ctrip.zeus.service.nginx;

import java.util.Map;
import java.util.Set;

/**
 * @Discription
 **/
public interface LocalNginxService {
    /*
     * @Description return all vs
     * @return
     **/
    Set<Long> getAllVsIds();

    String getNginxConf() throws Exception;

    String getVsConf(Long vsId) throws Exception;

    Map<String, String> getUpstreamConfs(Long vsId) throws Exception;
}
