package com.ctrip.zeus.restful.filter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by zhoumy on 2015/10/13.
 */
public class QueryExecuter {
    private final Queue<FilterSet> filterArray;

    private QueryExecuter() {
        this(new LinkedList<FilterSet>());
    }

    private QueryExecuter(Queue<FilterSet> filterArray) {
        this.filterArray = filterArray;
    }

    public Long[] run() throws Exception {
        Set<Long> result = new HashSet<>();
        if (!filterArray.isEmpty()) {
            result = filterArray.poll().filter(result);
        }
        while (!filterArray.isEmpty()) {
            if (result.isEmpty())
                break;
            FilterSet filter = filterArray.poll();
            result = filter.filter(result);
        }
        return result.toArray(new Long[result.size()]);
    }

    public static class Builder {
        private Queue<FilterSet> filterArray = new LinkedList<>();

        public Builder addFilterId(FilterSet<Long> item) {
            filterArray.add(item);
            return this;
        }

        public QueryExecuter build() {
            return new QueryExecuter(filterArray);
        }
    }
}
