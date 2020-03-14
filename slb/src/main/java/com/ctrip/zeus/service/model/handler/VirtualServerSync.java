package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.model.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/9/22.
 */
public interface VirtualServerSync {

    void add(VirtualServer virtualServer) throws Exception;

    void update(VirtualServer virtualServer) throws Exception;

    void updateStatus(List<VirtualServer> virtualServers) throws Exception;

    void delete(Long vsId) throws Exception;
}
