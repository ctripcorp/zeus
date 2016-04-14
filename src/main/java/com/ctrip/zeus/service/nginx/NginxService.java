package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.VsConfData;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
     * @param vsConfDataMap
     * @param cleanVsIds
     * @param dyups
     * @param needReload
     * @param needTest
     * @return List of nginx Response
     * @throws Exception
     */
    List<NginxResponse> update(String nginxConf,
                               Map<Long, VsConfData> vsConfDataMap,
                               Set<Long> cleanVsIds,
                               DyUpstreamOpsData[] dyups,
                               boolean needReload,
                               boolean needTest,
                               boolean needDyups) throws Exception;

    /**
     * refresh nginx configs
     *
     * @param nginxConf
     * @param vsConfDataMap
     * @return List of nginx Response
     * @throws Exception
     */
    NginxResponse refresh(String nginxConf, Map<Long, VsConfData> vsConfDataMap, boolean reload) throws Exception;

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

    /**
     * get traffic status of nginx server cluster.
     *
     * @param slbId the slb name
     * @return the traffic statuses
     */
    List<ReqStatus> getTrafficStatusBySlb(Long slbId, int count, boolean aggregatedByGroup, boolean aggregatedBySlbServer) throws Exception;

    List<ReqStatus> getTrafficStatusBySlb(String groupName, Long slbId, int count) throws Exception;


    /**
     * get traffic status of local nginx server.
     *
     * @return the traffic status
     */
    List<ReqStatus> getLocalTrafficStatus(Date time, int count);

    List<ReqStatus> getLocalTrafficStatus(Date time, String groupName, int count);
}
