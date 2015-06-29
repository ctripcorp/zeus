package com.ctrip.zeus.service.validate;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbValidateResponse;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * Created by fanqq on 2015/6/25.
 */
public interface SlbValidator {

    SlbValidateResponse validate(Long slbId) throws Exception;

    void validate(Slb slb);

    boolean validateVirtualServer(VirtualServer virtualServer);
}
