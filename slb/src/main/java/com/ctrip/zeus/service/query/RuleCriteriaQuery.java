package com.ctrip.zeus.service.query;

import java.util.Set;

public interface RuleCriteriaQuery extends CriteriaQuery {
    Set<Long> queryByTarget(String targetId, String targetType);
}
