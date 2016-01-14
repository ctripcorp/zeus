package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.ReqStatus;

import java.util.Date;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public interface NginxService {

    /**
     * get current slb servers
     *
     * @return list of slb servers
     * @throws Exception
     */
    List<SlbServer> getCurrentSlbServers(Long slbId, Integer slbVersion) throws Exception;

    /**
     * push config to slb servers , reload if needed.
     *
     * @return List of nginx Response
     * @throws Exception
     */
    List<NginxResponse> pushConf(List<SlbServer> slbServers, Long slbId, Integer slbVersion, List<Long> vsIds ,boolean needReload) throws Exception;

    /**
     * Local rollback  Conf
     *
     * @return the result of "ngnix -t"
     * @throws Exception
     */
    NginxResponse localRollbackConf(Long slbId, Integer slbVersion) throws Exception;

    /**
     * Rollback  All  Conf
     *
     * @return the result of "ngnix -t"
     * @throws Exception
     */
    boolean rollbackAllConf(Long slbId, Integer slbVersion) throws Exception;

    /**
     * write conf to disk
     *
     * @return the result of "ngnix -t"
     * @throws Exception
     */
    NginxResponse writeToDisk(List<Long> vsIds, Long slbId, Integer slbVersion) throws Exception;

    /**
     * write all server conf of nginx server conf in the slb
     *
     * @return is all success
     * @throws Exception
     */
    boolean writeALLToDisk(Long slbId, Integer slbVersion, List<Long> vsIds) throws Exception;

    /**
     * load the colocated nginx server conf from disk
     *
     * @return result of "ngnix -s reload"
     * @throws Exception
     */
    NginxResponse load(Long slbId, Integer version) throws Exception;
    /**
     * test the colocated nginx server conf from disk
     *
     * @return result of "ngnix -t"
     * @throws Exception
     */
    NginxResponse confTest(Long slbId, Integer version) throws Exception;

    /**
     * load all nginx server conf in the slb from disk
     *
     * @param slbId slbname
     * @return all response
     * @throws Exception
     */
    List<NginxResponse> loadAll(Long slbId, Integer version) throws Exception;
    /**
     * dy upstream ops api
     *
     * @param upsName     dy upstream name
     * @param upsCommands dy upstream commands
     */
    NginxResponse dyopsLocal(String upsName, String upsCommands) throws Exception;

    /**
     * dy upstream ops api
     *
     * @param slbId slbname
     * @param dyups dy upstream info
     */
    List<NginxResponse> dyops(Long slbId, List<DyUpstreamOpsData> dyups) throws Exception;

    /**
     * fetch the status of colocated nginx server status
     *
     * @return
     * @throws Exception
     */
    NginxServerStatus getStatus() throws Exception;

    /**
     * fetch the status of all nginx server in the slb
     *
     * @return
     * @throws Exception
     */
    List<NginxServerStatus> getStatusAll(Long slbId) throws Exception;

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
