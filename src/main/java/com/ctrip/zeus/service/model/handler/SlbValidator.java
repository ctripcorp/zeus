package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/6/30.
 */
public interface SlbValidator {

    void validate(Slb slb) throws Exception;

    void checkVirtualServerDependencies(Slb slb) throws Exception;

    void validateVirtualServer(List<VirtualServer> virtualServers) throws Exception;

    void removable(Slb slb) throws Exception;
}
