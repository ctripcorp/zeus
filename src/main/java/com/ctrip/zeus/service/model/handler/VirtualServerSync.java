package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/9/22.
 */
public interface VirtualServerSync {

    void addVirtualServer(VirtualServer virtualServer) throws Exception;

    void updateVirtualServer(VirtualServer virtualServer) throws Exception;

    void deleteVirtualServer(Long vsId) throws Exception;

    void deleteVirtualServers(Long[] vsIds) throws Exception;

    @Deprecated
    List<Long> port(VirtualServer[] vses) throws Exception;

    @Deprecated
    void port(VirtualServer vs) throws Exception;
}
