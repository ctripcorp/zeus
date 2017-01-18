package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.TrafficPolicy;

import java.util.List;

/**
 * Created by zhoumy on 2017/1/11.
 */
public interface TrafficPolicyRepository {

    List<TrafficPolicy> list() throws Exception;

    List<TrafficPolicy> list(IdVersion[] key) throws Exception;

    TrafficPolicy getById(Long id) throws Exception;

    TrafficPolicy getByKey(IdVersion key) throws Exception;

    TrafficPolicy add(TrafficPolicy trafficPolicy) throws Exception;

    TrafficPolicy add(TrafficPolicy trafficPolicy, boolean force) throws Exception;

    TrafficPolicy update(TrafficPolicy trafficPolicy) throws Exception;

    TrafficPolicy update(TrafficPolicy trafficPolicy, boolean force) throws Exception;

    void updateActiveStatus(IdVersion[] trafficPolicies) throws Exception;

    void delete(Long id) throws Exception;
}
