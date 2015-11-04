package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/9/24.
 */
public interface VirtualServerValidator {

    boolean exists(Long vsId) throws Exception;

    void validateVirtualServers(List<VirtualServer> virtualServers) throws Exception;

    void validateSslVirtualServer(VirtualServer virtualServer) throws Exception;

    void removable(VirtualServer virtualServer) throws Exception;
}
