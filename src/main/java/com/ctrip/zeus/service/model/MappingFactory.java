package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.VirtualServer;

/**
 * Created by zhoumy on 2016/1/19.
 */
public interface MappingFactory {

    ModelStatusMapping<Group> getByVsIds(Long[] vsIds) throws Exception;

    ModelStatusMapping<VirtualServer> getBySlbIds(Long slbId) throws Exception;
}
