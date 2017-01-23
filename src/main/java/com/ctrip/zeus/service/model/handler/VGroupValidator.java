package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.TrafficPolicy;

import java.util.Map;

/**
 * Created by zhoumy on 2015/11/23.
 */
public interface VGroupValidator extends ModelValidator<Group> {

    void validateForMerge(Long[] toBeMergedItems, Long vsId, Map<Long, Group> groupRef, Map<Long, TrafficPolicy> policyRef, boolean escapePathValidation) throws Exception;

    void validate(Group target, boolean escapePathValidation) throws Exception;
}
