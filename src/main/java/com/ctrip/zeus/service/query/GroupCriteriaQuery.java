package com.ctrip.zeus.service.query;

import java.util.Set;

/**
 * Created by zhoumy on 2015/8/7.
 */
public interface GroupCriteriaQuery {

    Long queryByName(String name) throws Exception;

    Set<Long> queryByAppId(String appId) throws Exception;

    Set<Long> queryAll() throws Exception;

    Set<Long> queryBySlbId(Long slbId) throws Exception;

    Set<Long> queryByVsIds(Long[] vsIds) throws Exception;
}
