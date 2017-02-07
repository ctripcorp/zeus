package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.Collection;

/**
 * Created by zhoumy on 2015/9/24.
 */
public interface VirtualServerValidator extends ModelValidator<VirtualServer> {

    void validateFields(VirtualServer vs) throws ValidationException;

    void validateDomains(Long slbId, Collection<VirtualServer> vses, ValidationContext context);
}