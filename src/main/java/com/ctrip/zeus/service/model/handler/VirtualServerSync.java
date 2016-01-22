package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.IdVersion;

import java.util.Set;

/**
 * Created by zhoumy on 2015/9/22.
 */
public interface VirtualServerSync {

    void add(VirtualServer virtualServer) throws Exception;

    void update(VirtualServer virtualServer) throws Exception;

    void updateStatus(IdVersion[] virtualServers) throws Exception;

    void delete(Long vsId) throws Exception;

    @Deprecated
    Set<Long> port(Long[] vsIds) throws Exception;
}
