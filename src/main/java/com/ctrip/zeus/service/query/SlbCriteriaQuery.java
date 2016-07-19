package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;

import java.util.Set;

/**
 * Created by zhoumy on 2015/8/27.
 */
public interface SlbCriteriaQuery extends CriteriaQuery {

    Long queryByName(String name) throws Exception;

    Set<Long> queryByVs(IdVersion vsIdVersion) throws Exception;

    Set<Long> queryByVses(IdVersion[] vsIdVersions) throws Exception;

    Set<IdVersion> queryBySlbServerIp(String ip) throws Exception;
}
