package com.ctrip.zeus.service.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2016/9/18.
 */
public final class QueryNode {
    private List<QueryNode> children = new ArrayList<>();
    private AggregationMode mode;
    private Map<String, List<String>> map = new HashMap<>();

    private QueryNode(AggregationMode mode) {
        this.mode = mode;
    }

    public static QueryNode createUnionNode() {
        return new QueryNode(AggregationMode.UNION);
    }


    public static QueryNode createJoinNode() {
        return new QueryNode(AggregationMode.JOIN);
    }

    public static QueryNode createLeafNode() {
        QueryNode res = new QueryNode(AggregationMode.LEAF);
        return res;
    }

    public QueryNode putQueryParam(String k, List<String> v) {
        if (!mode.equals(AggregationMode.LEAF)) return null;
        map.put(k, v);
        return this;
    }

    public Map<String, List<String>> getQueryParams() {
        return map;
    }

    public QueryNode addChild(QueryNode node) {
        if (mode.equals(AggregationMode.LEAF)) return null;
        children.add(node);
        return this;
    }

    public boolean isLeaf() {
        return mode.equals(AggregationMode.LEAF);
    }

    public AggregationMode getMode() {
        return mode;
    }

    public List<QueryNode> getChildren() {
        return children;
    }
}
