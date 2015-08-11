package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * Created by zhoumy on 2015/6/30.
 */
public interface SlbValidator {

    void validate(Slb slb) throws Exception;

    void checkVirtualServerDependencies(VirtualServer[] virtualServers) throws Exception;

    void validateVirtualServer(VirtualServer[] virtualServers) throws Exception;

    void removable(Long slbId) throws Exception;
}
