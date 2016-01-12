package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.ModelMode;

import java.util.Set;

/**
 * Created by zhoumy on 2015/8/7.
 */
public interface GroupCriteriaQuery {

    Long queryByName(String name) throws Exception;

    Set<Long> queryByAppId(String appId) throws Exception;

    Set<Long> queryAll() throws Exception;

    Set<Long> queryAllVGroups() throws Exception;

    Set<IdVersion> queryByIdsAndMode(Long[] groupIds, ModelMode mode) throws Exception;

    IdVersion[] queryByIdAndMode(Long groupId, ModelMode mode) throws Exception;

    Set<IdVersion> queryAll(ModelMode mode) throws Exception;

    Set<IdVersion> queryAllVGroups(ModelMode mode) throws Exception;

    Set<IdVersion> queryByVsId(Long vsId) throws Exception;

    Set<IdVersion> queryByVsIds(Long[] vsIds) throws Exception;

    Set<IdVersion> queryByGroupServerIp(String ip) throws Exception;
}
