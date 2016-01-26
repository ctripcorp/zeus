package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.Set;

/**
 * Created by zhoumy on 2016/1/19.
 */
public interface EntityFactory {

    ModelStatusMapping<Group> getGroupsByVsIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<VirtualServer> getVsesBySlbIds(Long slbId) throws Exception;

    ModelStatusMapping<Slb> getSlbsByIds(Long[] slbIds) throws Exception;

    ModelStatusMapping<VirtualServer> getVsesByIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<Group> getGroupsByIds(Long[] groupIds) throws Exception;

    Long[] getSlbIdsByIp(String ip, ModelMode mode) throws Exception;

    Long[] getVsIdsBySlbId(Long slbId, ModelMode mode) throws Exception;

    Long[] getGroupIdsByVsIds(Long[] vsIds, ModelMode mode) throws Exception;
}
