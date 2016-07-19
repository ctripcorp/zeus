package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;

import java.util.Set;

/**
 * Created by zhoumy on 2015/8/7.
 */
public interface GroupCriteriaQuery extends CriteriaQuery {

    Set<Long> queryByAppId(String appId) throws Exception;

    Set<Long> queryAllVGroups() throws Exception;

    Set<IdVersion> queryAllVGroups(SelectionMode mode) throws Exception;

    Set<IdVersion> queryByGroupServer(String groupServer) throws Exception;

    Set<IdVersion> queryByVsId(Long vsId) throws Exception;

    Set<IdVersion> queryByVsIds(Long[] vsIds) throws Exception;
}
