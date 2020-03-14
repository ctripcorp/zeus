package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;

import java.util.Set;

public interface DrCriteriaQuery extends CriteriaQuery {
    Set<IdVersion> queryByVsId(Long vsId) throws Exception;

    Set<Long> queryByGroupId(Long groupId) throws Exception;
}