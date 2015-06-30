package com.ctrip.zeus.service.validate;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbValidateResponse;

/**
 * Created by fanqq on 2015/6/25.
 */
public interface SlbValidatorLocal {
    public SlbValidateResponse validate(Long slbId) throws Exception;
}
