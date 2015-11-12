package com.ctrip.zeus.service.status;

import com.ctrip.zeus.service.Repository;

import java.util.Map;

/**
 * Created by fanqq on 2015/11/11.
 */
public interface HealthCheckStatusService extends Repository {
    /**
     *  fresh HealthCheck Status
     */
    public void freshHealthCheckStatus() throws Exception;

    /**
     *  get HealthCheck Status By SlbId
     *  @param slbId SlbId
     *  @return {Map<String,Boolean>} key:{groupId}_{ip} value:{status}
     */
    public Map<String,Boolean> getHealthCheckStatusBySlbId(Long slbId)throws Exception;
    /**
     *  get HealthCheck Status By serverIp
     *  @param serverIp serverIp
     *  @return {Map<String,Boolean>} key:{groupId}_{ip} value:{status}
     */
    public Map<String,Boolean> getHealthCheckStatusBySlbServer(String serverIp)throws Exception;
}
