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
}
