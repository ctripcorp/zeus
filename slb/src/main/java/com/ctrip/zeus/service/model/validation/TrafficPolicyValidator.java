package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.PolicyVirtualServer;
import com.ctrip.zeus.model.model.TrafficPolicy;
import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2017/2/6.
 */
public interface TrafficPolicyValidator extends ModelValidator<TrafficPolicy> {

    void validateFields(TrafficPolicy policy, ValidationContext context);

    Map<Long,String>  validatedForDeactivate(TrafficPolicy policy) throws Exception;

    Long[] validatePolicyControl(TrafficPolicy policy) throws ValidationException;

    Map<Long, PolicyVirtualServer> validatePolicyOnVses(Long policyId, Long[] controlIds,
                                                        Map<Long, List<LocationEntry>> groupEntries, List<PolicyVirtualServer> policyOnVses,
                                                        Map<Long, List<LocationEntry>> groupRelatedPolicyEntries,
                                                        ValidationContext context) throws ValidationException;
}
