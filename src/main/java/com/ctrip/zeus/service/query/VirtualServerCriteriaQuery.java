package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.ModelMode;

import java.util.Set;

/**
 * Created by zhoumy on 2015/9/11.
 */
public interface VirtualServerCriteriaQuery {

    Set<Long> queryAll() throws Exception;

    Set<IdVersion> queryAll(ModelMode mode) throws Exception;

    Set<IdVersion> queryBySlbId(Long slbId) throws Exception;

    Set<IdVersion> queryByGroupIds(Long[] groupIds) throws Exception;

    Set<IdVersion> queryByDomain(String domain) throws Exception;
}
