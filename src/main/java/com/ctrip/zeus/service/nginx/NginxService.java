package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Float;

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
    boolean writeALLToDisk(String slb) throws Exception;
    /**
     * write all server conf of nginx server conf in the slb
     * @return list the results
     * @throws Exception
     */
    List<NginxResponse> writeALLToDiskListResult(String slb) throws Exception;

    /**
     * load the colocated nginx server conf from disk
     * @return result of "ngnix -s reload"
     * @throws Exception
     */
    NginxResponse load() throws Exception;

    /**
     * load all nginx server conf in the slb from disk
     * @param slbName slbname
     * @return all response
     * @throws Exception
     */
    List<NginxResponse> loadAll(String slbName) throws Exception;

    /**
     *write all and then load all , throw Exception while write failed
     * @param slbName
     * @return List<NginxResponse>
     */
    List<NginxResponse> writeAllAndLoadAll(String slbName) throws Exception;

    /**
     *dy upstream ops api
     * @param upsName dy upstream name
     * @param upsCommands dy upstream commands
     */
    NginxResponse dyopsLocal(String upsName,String upsCommands)throws Exception;
    /**
     *dy upstream ops api
     * @param slbName slbname
     * @param dyups dy upstream info
     */
    List<NginxResponse> dyops(String slbName,List<DyUpstreamOpsData> dyups)throws Exception;

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
    List<NginxServerStatus> getStatusAll(String slbName) throws Exception;

    /**
     * get traffic status of nginx server cluster.
     * @param slbName the slb name
     * @return the traffic statuses
     */
    List<TrafficStatus> getTrafficStatusBySlb(String slbName) throws Exception;


    /**
     * get traffic status of local nginx server.
     * @return the traffic status
     */
    TrafficStatus getLocalTrafficStatus();
}
