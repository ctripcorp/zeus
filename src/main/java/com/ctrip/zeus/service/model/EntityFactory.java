package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.util.Set;

/**
 * Created by zhoumy on 2016/1/19.
 */
public interface EntityFactory {

    ModelStatusMapping<Group> getByVsIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<VirtualServer> getBySlbIds(Long slbId) throws Exception;

    ModelStatusMapping<Slb> getSlbById(Long[] slbId) throws Exception;

    ModelStatusMapping<VirtualServer> getVsByVsIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<Group> getGroupById(Long[] groupId) throws Exception;

    Long[] getSlbIdsByIp(String ip, ModelMode mode) throws Exception;

    Long[] getVsIdsBySlbId(Long slbId, ModelMode mode) throws Exception;

    Long[] getGroupIdsByVsIds(Long[] vsIds, ModelMode mode) throws Exception;


}
