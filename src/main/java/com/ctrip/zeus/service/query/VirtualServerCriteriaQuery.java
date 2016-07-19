package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;

import java.util.Set;

/**
 * Created by zhoumy on 2015/9/11.
 */
public interface VirtualServerCriteriaQuery extends CriteriaQuery {

    Set<Long> queryByGroup(IdVersion[] searchKeys) throws Exception;

    Set<IdVersion> queryBySlbId(Long slbId) throws Exception;

    Set<IdVersion> queryBySlbIds(Long[] slbIds) throws Exception;

    Set<IdVersion> queryByDomain(String domain) throws Exception;
}
