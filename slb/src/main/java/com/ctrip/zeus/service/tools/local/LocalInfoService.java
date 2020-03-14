package com.ctrip.zeus.service.tools.local;

/**
 * Created by fanqq on 2017/4/7.
 */
public interface LocalInfoService {
    String getLocalIp();

    Long getLocalSlbId() throws Exception;

    String getEnv();

    Long getLocalSlbIdWithRetry() throws Exception;
}
