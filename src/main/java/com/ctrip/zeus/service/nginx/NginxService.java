package com.ctrip.zeus.service.nginx;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.NginxServerStatus;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public interface NginxService {
    /**
     * load the colocated nginx server conf
     * @return
     * @throws Exception
     */
    NginxResponse load() throws Exception;

    /**
     * fetch the status of colocated nginx server status
     * @return
     * @throws Exception
     */
    NginxServerStatus getStatus() throws Exception;

    /**
     * load all nginx server conf in the slb
     * @param slbName
     * @return
     * @throws Exception
     */
    List<NginxResponse> loadAll(String slbName) throws Exception;

    List<NginxServerStatus> getStatusAll(String slbName) throws Exception;

}
