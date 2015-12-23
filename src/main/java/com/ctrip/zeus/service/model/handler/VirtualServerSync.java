package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/9/22.
 */
public interface VirtualServerSync {

    void add(VirtualServer virtualServer) throws Exception;

    void update(VirtualServer virtualServer) throws Exception;

    void updateStatus(VirtualServer[] virtualServers) throws Exception;

    void delete(Long vsId) throws Exception;

    void batchDelete(Long[] vsIds) throws Exception;

    @Deprecated
    List<Long> port(Long[] vsIds) throws Exception;
}
