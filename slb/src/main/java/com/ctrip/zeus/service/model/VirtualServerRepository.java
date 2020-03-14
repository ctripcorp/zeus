package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.model.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/27.
 */
public interface VirtualServerRepository {

    List<VirtualServer> listAll(Long[] vsIds) throws Exception;

    List<VirtualServer> listAll(IdVersion[] keys) throws Exception;

    VirtualServer getById(Long vsId) throws Exception;

    VirtualServer getByKey(IdVersion key) throws Exception;

    VirtualServer add(VirtualServer virtualServer) throws Exception;

    VirtualServer update(VirtualServer virtualServer) throws Exception;

    /**
     * virtualServer: virtualServer to be updated
     * purpose: update virtualServer called by rule repository
     * **/
    VirtualServer updateVirtualServerRule(VirtualServer virtualServer) throws Exception;

    void delete(Long virtualServerId) throws Exception;

    void installCertificate(VirtualServer virtualServer) throws Exception;

    void updateStatus(IdVersion[] vses, SelectionMode state) throws Exception;

    void updateStatus(IdVersion[] vses) throws Exception;
}
