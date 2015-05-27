package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public interface NginxService {

    /**
     * write conf to disk
     * @return the result of "ngnix -t"
     * @throws Exception
     */
    NginxResponse writeToDisk() throws Exception;

    /**
     * write all server conf of nginx server conf in the slb
     * @return is all success
     * @throws Exception
     */
    boolean writeALLToDisk(Long slbId) throws Exception;
    /**
     * write all server conf of nginx server conf in the slb
     * @return list the results
     * @throws Exception
     */
    List<NginxResponse> writeALLToDiskListResult(Long slbId) throws Exception;

    /**
     * load the colocated nginx server conf from disk
     * @return result of "ngnix -s reload"
     * @throws Exception
     */
    NginxResponse load() throws Exception;

    /**
     * load all nginx server conf in the slb from disk
     * @param slbId slbname
     * @return all response
     * @throws Exception
     */
    List<NginxResponse> loadAll(Long slbId) throws Exception;

    /**
     *write all and then load all , throw Exception while write failed
     * @param slbId
     * @return List<NginxResponse>
     */
    List<NginxResponse> writeAllAndLoadAll(Long slbId) throws Exception;

    /**
     *dy upstream ops api
     * @param upsName dy upstream name
     * @param upsCommands dy upstream commands
     */
    NginxResponse dyopsLocal(String upsName,String upsCommands)throws Exception;
    /**
     *dy upstream ops api
     * @param slbId slbname
     * @param dyups dy upstream info
     */
    List<NginxResponse> dyops(Long slbId,List<DyUpstreamOpsData> dyups)throws Exception;

    /**
     * fetch the status of colocated nginx server status
     * @return
     * @throws Exception
     */
    NginxServerStatus getStatus() throws Exception;

    /**
     * fetch the status of all nginx server in the slb
     * @return
     * @throws Exception
     */
    List<NginxServerStatus> getStatusAll(Long slbId) throws Exception;

    /**
     * get traffic status of nginx server cluster.
     * @param slbId the slb name
     * @return the traffic statuses
     */
    List<TrafficStatus> getTrafficStatusBySlb(Long slbId) throws Exception;


    /**
     * get traffic status of local nginx server.
     * @return the traffic status
     */
    TrafficStatus getLocalTrafficStatus();
}
