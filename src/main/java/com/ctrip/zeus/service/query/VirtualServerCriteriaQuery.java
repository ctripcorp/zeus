package com.ctrip.zeus.service.query;

import java.util.Set;

/**
 * Created by zhoumy on 2015/9/11.
 */
public interface VirtualServerCriteriaQuery {

    Set<Long> queryAll() throws Exception;

    Set<Long> queryBySlbId(Long slbId) throws Exception;

    Set<Long> queryByDomain(String domain) throws Exception;
}
