package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;

import java.util.Set;

/**
 * Created by zhoumy on 2017/1/18.
 */
public interface TrafficPolicyQuery extends CriteriaQuery {

    Set<IdVersion> queryByVsId(Long vsId) throws Exception;

    Set<IdVersion> queryByGroupId(Long groupId) throws Exception;
}
