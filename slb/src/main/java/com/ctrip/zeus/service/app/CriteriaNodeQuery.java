package com.ctrip.zeus.service.app;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fanqq on 2016/9/18.
 */
public interface CriteriaNodeQuery<T> {

    public boolean shouldSkip(QueryNode queryNode);

    public Set<T> query(Map<String, List<String>> query) throws Exception;

}
