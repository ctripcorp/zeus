package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/7/27.
 */
public interface VirtualServerRepository {

    List<GroupVirtualServer> listGroupVsByGroups(Long[] groupIds) throws Exception;

    List<GroupVirtualServer> listGroupVsByVsId(Long virtualServerId) throws Exception;

    List<GroupVirtualServer> listGroupVsBySlb(Long slbId) throws Exception;

    List<VirtualServer> listVirtualServerBySlb(Long slbId) throws Exception;

    VirtualServer getById(Long virtualServerId) throws Exception;

    VirtualServer getBySlbAndName(Long slbId, String virtualServerName) throws Exception;

    Long[] findGroupsByVirtualServer(Long virtualServerId) throws Exception;
}
