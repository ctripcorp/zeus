package com.ctrip.zeus.service.query;

import java.util.Set;

/**
 * Created by zhoumy on 2015/8/27.
 */
public interface SlbCriteriaQuery {

    Long queryByName(String name) throws Exception;

    Long queryBySlbServer(String ip) throws Exception;

    Set<Long> queryByGroups(Long[] groupIds) throws Exception;

    Set<Long> queryAll() throws Exception;
}
