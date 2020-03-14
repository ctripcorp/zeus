package com.ctrip.zeus.service.app;

import javax.ws.rs.core.UriInfo;
import java.util.Set;

/**
 * Created by fanqq on 2016/9/14.
 */
public interface AppQueryEngine {
    QueryNode parseQueryNode(UriInfo uriInfo);

    Set<String> executeQuery(CriteriaNodeQuery<String> queryService, QueryNode queryNode) throws Exception;
}
