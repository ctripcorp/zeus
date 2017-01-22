package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.TrafficPolicy;

import java.util.Map;

/**
 * Created by zhoumy on 2017/1/13.
 */
public interface TrafficPolicyValidator extends ModelValidator<TrafficPolicy> {

    void validate(TrafficPolicy target, boolean escapePathValidation) throws Exception;

    void validateForMerge(Long[] toBeMergedItems, Long vsId, Map<Long, Group> groupRef, Map<Long, TrafficPolicy> policyRef, boolean escapePathValidation) throws Exception;
}