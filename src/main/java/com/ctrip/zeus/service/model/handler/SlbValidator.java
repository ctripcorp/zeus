package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;

/**
 * Created by zhoumy on 2015/6/30.
 */
public interface SlbValidator {

    void validate(Slb slb) throws ValidationException;

    boolean removable(Slb slb) throws Exception;

    boolean modifiable(Slb slb) throws Exception;
}
