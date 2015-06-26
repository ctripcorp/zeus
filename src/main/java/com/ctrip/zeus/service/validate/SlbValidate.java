package com.ctrip.zeus.service.validate;

import com.ctrip.zeus.model.entity.SlbValidateResponse;

/**
 * Created by fanqq on 2015/6/25.
 */
public interface SlbValidate {
    public SlbValidateResponse validate(Long slbId)throws Exception;
}
