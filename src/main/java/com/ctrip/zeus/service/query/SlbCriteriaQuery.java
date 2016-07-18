package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;

import java.util.Set;

/**
 * Created by zhoumy on 2015/8/27.
 */
public interface SlbCriteriaQuery {

    IdVersion[] queryByCommand(QueryCommand query, SelectionMode mode) throws Exception;

    Set<Long> queryAll() throws Exception;

    Set<IdVersion> queryAll(SelectionMode mode) throws Exception;

    Long queryByName(String name) throws Exception;

    Set<IdVersion> queryByIdsAndMode(Long[] slbIds, SelectionMode mode) throws Exception;

    IdVersion[] queryByIdAndMode(Long slbId, SelectionMode mode) throws Exception;

    Set<Long> queryByVs(IdVersion vsIdVersion) throws Exception;

    Set<Long> queryByVses(IdVersion[] vsIdVersions) throws Exception;

    Set<IdVersion> queryBySlbServerIp(String ip) throws Exception;
}
