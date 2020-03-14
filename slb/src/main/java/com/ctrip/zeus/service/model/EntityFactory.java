package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.model.*;

/**
 * Created by zhoumy on 2016/1/19.
 */
public interface EntityFactory {

    ModelStatusMapping<Group> getGroupsByVsIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<VirtualServer> getVsesBySlbIds(Long slbId) throws Exception;

    ModelStatusMapping<Slb> getSlbsByIds(Long[] slbIds) throws Exception;

    ModelStatusMapping<VirtualServer> getVsesByIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<Group> getGroupsByIds(Long[] groupIds) throws Exception;

    ModelStatusMapping<TrafficPolicy> getPoliciesByIds(Long[] policyIds) throws Exception;

    ModelStatusMapping<TrafficPolicy> getPoliciesByVsIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<Dr> getDrsByIds(Long[] drIds) throws Exception;

    ModelStatusMapping<Dr> getDrsByVsIds(Long[] vsIds) throws Exception;

    Long[] getGroupIdsByGroupServerIp(String ip, SelectionMode mode) throws Exception;

    Long[] getSlbIdsByIp(String ip, SelectionMode mode) throws Exception;

    Long[] getVsIdsBySlbId(Long slbId, SelectionMode mode) throws Exception;
}
