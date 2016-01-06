package com.ctrip.zeus.restful.filter;

import com.ctrip.zeus.service.model.IdVersion;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by zhoumy on 2015/10/13.
 */
public class QueryExecuter {
    private final Queue<FilterSet> filterIdArray;
    private final Queue<FilterSet> filterIdVersionArray;
    private ResultHandler resultHandler;
    private final int offset;

    private final int filterNone = 0;
    private final int filterIdVersionOnly = 1;
    private final int filterIdOnly = 2;
    private final int filterBoth = 3;

    private QueryExecuter() {
        this(new LinkedList<FilterSet>(), new LinkedList<FilterSet>());
    }

    private QueryExecuter(Queue<FilterSet> filterIdArray, Queue<FilterSet> filterIdVersionArray) {
        this.filterIdArray = filterIdArray;
        this.filterIdVersionArray = filterIdVersionArray;
        this.offset = (filterIdArray.size() == 0 ? -1 : 1) + (filterIdVersionArray.size() == 0 ? 1 : 2);
    }

    public Long[] run() throws Exception {
        if (offset == filterNone)
            return new Long[0];

        Set<Long> filteredIds = null;
        // offset == filterIdOnly || offset == filterBoth
        if (offset != filterIdVersionOnly) {
            while (!filterIdArray.isEmpty() && filteredIds == null) {
                FilterSet filter = filterIdArray.poll();
                if (filter.shouldFilter()) filteredIds = filter.filter();
            }
            while (!filterIdArray.isEmpty()) {
                if (filteredIds.isEmpty()) return new Long[0];
                FilterSet filter = filterIdArray.poll();
                if (filter.shouldFilter()) filteredIds.retainAll(filter.filter());
            }
            if (offset == filterIdOnly)
                return filteredIds == null ? new Long[0] : filteredIds.toArray(new Long[filteredIds.size()]);
        }

        Set<IdVersion> filteredIdVersions = null;
        while (!filterIdVersionArray.isEmpty() && filteredIdVersions == null) {
            FilterSet filter = filterIdVersionArray.poll();
            if (filter.shouldFilter()) filteredIdVersions = filter.filter();
        }
        while (!filterIdVersionArray.isEmpty()) {
            if (filteredIdVersions.isEmpty()) return new Long[0];
            FilterSet filter = filterIdVersionArray.poll();
            if (filter.shouldFilter()) filteredIdVersions.retainAll(filter.filter());
        }

        // filterIdOnly
        if (filteredIdVersions == null) {
            return filteredIds == null ? new Long[0] : filteredIds.toArray(new Long[filteredIds.size()]);
        }

        Set<Long> result = new HashSet<>();
        for (IdVersion e : filteredIdVersions) {
            result.add(e.getId());
        }
        // if (filterIds == null) offset = filterIdVersionOnly
        if (filteredIds != null)
            result.retainAll(filteredIds);

        if (resultHandler != null)
            resultHandler.handle(result);
        return result.toArray(new Long[result.size()]);
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public static class Builder {
        private Queue<FilterSet> filterIdArray = new LinkedList<>();
        private Queue<FilterSet> filterIdVersionArray = new LinkedList<>();

        public Builder addFilterId(FilterSet<Long> item) {
            filterIdArray.add(item);
            return this;
        }

        public Builder addFilterIdVersion(FilterSet<IdVersion> item) {
            filterIdVersionArray.add(item);
            return this;
        }

        public QueryExecuter build() {
            return new QueryExecuter(filterIdArray, filterIdVersionArray);
        }
    }

    public interface ResultHandler {

        void handle(Set<Long> result) throws Exception;
    }
}
