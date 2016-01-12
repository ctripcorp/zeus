package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2015/7/27.
 */
public interface VirtualServerRepository {

    List<VirtualServer> listAll(Long[] vsIds) throws Exception;

    List<VirtualServer> listAll(IdVersion[] keys) throws Exception;

    VirtualServer getById(Long vsId) throws Exception;

    VirtualServer getByKey(IdVersion key) throws Exception;

    VirtualServer add(Long slbId, VirtualServer virtualServer) throws Exception;

    void update(VirtualServer virtualServer) throws Exception;

    void delete(Long virtualServerId) throws Exception;

    void installCertificate(VirtualServer virtualServer) throws Exception;

    @Deprecated
    Set<Long> port(Long[] vsIds) throws Exception;
}
