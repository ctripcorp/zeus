package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;

import java.util.Set;

/**
 * Created by zhoumy on 2015/9/11.
 */
public interface VirtualServerCriteriaQuery {

    Set<Long> queryAll() throws Exception;

    Set<IdVersion> queryAll(SelectionMode mode) throws Exception;

    Set<IdVersion> queryByIdsAndMode(Long[] vsIds, SelectionMode mode) throws Exception;

    IdVersion[] queryByIdAndMode(Long vsId, SelectionMode mode) throws Exception;

    Set<IdVersion> queryBySlbId(Long slbId) throws Exception;

    Set<IdVersion> queryBySlbIds(Long[] slbIds) throws Exception;

    Set<IdVersion> queryByDomain(String domain) throws Exception;
}
