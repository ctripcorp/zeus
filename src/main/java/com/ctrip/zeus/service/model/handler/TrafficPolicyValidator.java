package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.TrafficPolicy;

/**
 * Created by zhoumy on 2017/1/13.
 */
public interface TrafficPolicyValidator extends ModelValidator<TrafficPolicy> {

    void validate(TrafficPolicy target, boolean escapePathValidation) throws Exception;
}