package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.service.model.common.ValidationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/6/30.
 */
public interface SlbValidator extends ModelValidator<Slb> {

    void validateFields(Slb slb, ValidationContext context) throws ValidationException;

    void validateSlbServers(Map<Long, List<SlbServer>> serversBySlb, ValidationContext context);

    boolean exists(Long[] slbId);
}