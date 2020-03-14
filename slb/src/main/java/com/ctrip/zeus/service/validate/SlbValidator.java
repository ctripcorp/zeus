package com.ctrip.zeus.service.validate;

import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbValidateResponse;

/**
 * Created by fanqq on 2015/6/25.
 */
public interface SlbValidator {

    SlbValidateResponse validate(Slb slbId) throws Exception;
}
