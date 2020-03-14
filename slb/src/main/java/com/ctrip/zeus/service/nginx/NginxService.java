package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.model.model.DyUpstreamOpsData;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.model.nginx.NginxResponse;

import java.util.List;
import java.util.Set;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public interface NginxService {

    /**
     * update nginx configs
     *
     * @param nginxConf
     * @param entry
     * @param cleanVsIds
     * @param dyups
     * @param needReload
     * @param needTest
     * @return List of nginx Response
     * @throws Exception
     */
    List<NginxResponse> update(String nginxConf,
                               NginxConfEntry entry,
                               Set<Long> updateVsIds,
                               Set<Long> cleanVsIds,
                               DyUpstreamOpsData[] dyups,
                               boolean needReload,
                               boolean needTest,
                               boolean needDyups) throws Exception;

    /**
     * refresh nginx configs
     *
     * @param nginxConf
     * @param entry
     * @return List of nginx Response
     * @throws Exception
     */
    NginxResponse refresh(String nginxConf, NginxConfEntry entry, boolean reload) throws Exception;

    /**
     * push config to slb servers
     *
     * @return Nginx Response
     * @throws Exception
     */
    NginxResponse updateConf(List<SlbServer> slbServers) throws Exception;

    /**
     * Rollback  All  Conf
     *
     * @param slbServers
     * @return List of nginx Response
     * @throws Exception
     */
    void rollbackAllConf(List<SlbServer> slbServers) throws Exception;
}
