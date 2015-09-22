package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/27.
 */
public interface VirtualServerRepository {

    List<VirtualServer> listAll(Long[] vsIds) throws Exception;

    VirtualServer getById(Long virtualServerId) throws Exception;

    VirtualServer addVirtualServer(Long slbId, VirtualServer virtualServer) throws Exception;

    void updateVirtualServer(VirtualServer virtualServer) throws Exception;

    void deleteVirtualServer(Long virtualServerId) throws Exception;

    void batchDeleteVirtualServers(Long slbId) throws Exception;

    List<GroupVirtualServer> listGroupVsByGroups(Long[] groupIds) throws Exception;

    void batchDeleteGroupVirtualServers(Long groupId) throws Exception;

    void updateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers) throws Exception;

    @Deprecated
    List<Long> portVirtualServerRel() throws Exception;

    @Deprecated
    void portVirtualServerRel(Long vsId) throws Exception;
}
