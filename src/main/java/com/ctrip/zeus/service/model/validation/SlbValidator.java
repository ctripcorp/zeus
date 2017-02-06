package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.model.entity.Slb;

/**
 * Created by zhoumy on 2015/6/30.
 */
public interface SlbValidator extends ModelValidator<Slb> {

    void exists(Long[] slbId) throws Exception;
}
