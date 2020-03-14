package com.ctrip.zeus.service.app.impl;

import com.ctrip.zeus.service.app.AppQueryEngine;
import com.ctrip.zeus.service.app.CriteriaNodeQuery;
import com.ctrip.zeus.service.app.QueryNode;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * Created by fanqq on 2016/9/14.
 */
@Service("appQueryEngine")
public class AppQueryEngineImpl implements AppQueryEngine {
    @Override
    public QueryNode parseQueryNode(UriInfo uriInfo) {
        QueryNode res = QueryNode.createUnionNode();
        for (Map.Entry<String, List<String>> e : uriInfo.getQueryParameters().entrySet()) {
            QueryNode tmp = null;
            switch (e.getKey()) {
                case "id":
                case "groupId":
                case "name":
                case "groupName":
                case "appId":
                case "ip":
                case "member":
                case "fuzzyName":
                case "vsId":
                case "vsName":
                case "domain":
                case "slbId":
                case "slbName":
                    tmp = QueryNode.createLeafNode();
                    tmp.putQueryParam(e.getKey(), e.getValue());
                    break;
                case "anyTag":
                case "unionTag":
                case "anyProp":
                case "unionProp":
                    tmp = QueryNode.createUnionNode();
                    for (String v : e.getValue()) {
                        List<String> l = new ArrayList<>();
                        l.add(v);
                        tmp.addChild(QueryNode.createLeafNode().putQueryParam(e.getKey(), l));
                    }
                    break;
                case "prop":
                case "joinProp":
                case "tags":
                case "joinTag":
                    tmp = QueryNode.createJoinNode();
                    for (String v : e.getValue()) {
                        List<String> l = new ArrayList<>();
                        l.add(v);
                        tmp.addChild(QueryNode.createLeafNode().putQueryParam(e.getKey(), l));
                    }
                    break;
                case "targetId":
                    List<String> t = uriInfo.getQueryParameters().get("targetType");
                    if (t != null) {
                        tmp = QueryNode.createLeafNode();
                        tmp.putQueryParam(e.getKey(), e.getValue()).putQueryParam("targetType", t);
                    }
                    break;
                default:
                    break;
            }
            if (tmp != null) {
                res.addChild(tmp);
            }
        }
        if (res.getChildren().size() == 0) {
            res.addChild(QueryNode.createLeafNode().putQueryParam("queryAll", new ArrayList<String>()));
        }
        return res;
    }

    @Override
    public Set<String> executeQuery(CriteriaNodeQuery<String> queryService, QueryNode queryNode) throws Exception {
        Set<String> res = new HashSet<>();
        switch (queryNode.getMode()) {
            case LEAF:
                if (queryService.shouldSkip(queryNode)) {
                    return res;
                } else {
                    return queryService.query(queryNode.getQueryParams());
                }
            case JOIN:
                for (QueryNode child : queryNode.getChildren()) {
                    if (child.isLeaf() && queryService.shouldSkip(child)) continue;
                    res.addAll(executeQuery(queryService, child));
                }
                return res;
            case UNION:
                boolean first = true;
                for (QueryNode child : queryNode.getChildren()) {
                    if (child.isLeaf() && queryService.shouldSkip(child)) continue;
                    if (first) {
                        res.addAll(executeQuery(queryService, child));
                        first = false;
                    } else {
                        res.retainAll(executeQuery(queryService, child));
                    }
                    if (res.size() == 0) {
                        return res;
                    }
                }
                return res;
            default:
                return res;
        }
    }
}
