package com.ctrip.zeus.service.nginx.handler;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.VsConfData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fanqq on 2016/3/14.
 */
public interface NginxConfOpsService {
    /**
     * write all to dist ;
     *
     * @return NginxResponse
     * @throws Exception
     */
    boolean updateAll(Map<Long, VsConfData> vsConfs) throws Exception;

    /**
     * update nginx conf  ;
     *
     * @return void
     * @throws Exception
     */
    void updateNginxConf(String nginxConf) throws Exception;

    /**
     * update nginx conf  ;
     *
     * @return NginxResponse
     * @throws Exception
     */
    boolean cleanAndUpdateConf(Set<Long> cleanVsIds, Map<Long, VsConfData> vsConfs) throws Exception;
}
