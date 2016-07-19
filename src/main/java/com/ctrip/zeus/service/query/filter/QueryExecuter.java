package com.ctrip.zeus.service.query.filter;

import com.ctrip.zeus.executor.impl.ResultHandler;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by zhoumy on 2015/10/13.
 */
public class QueryExecuter<T extends Comparable> {
    private final Class<T> clazz;
    private final Queue<FilterSet> filterQueue;

    private QueryExecuter(Class<T> clazz) {
        this(clazz, new LinkedList<FilterSet>());
    }

    private QueryExecuter(Class<T> clazz, Queue<FilterSet> filterQueue) {
        this.clazz = clazz;
        this.filterQueue = filterQueue;
    }

    public T[] run() throws Exception {
        Set<T> result = null;
        while (!filterQueue.isEmpty() && result == null) {
            FilterSet filter = filterQueue.poll();
            if (filter.shouldFilter()) result = filter.filter();
        }
        while (!filterQueue.isEmpty()) {
            if (result.isEmpty()) return (T[]) Array.newInstance(clazz, 0);
            FilterSet filter = filterQueue.poll();
            if (filter.shouldFilter()) result.retainAll(filter.filter());
        }
        return result == null ? null : result.toArray((T[]) Array.newInstance(clazz, result.size()));
    }

    public <W> W[] run(ResultHandler<T, W> resultHandler) throws Exception {
        Set<T> result = null;
        while (!filterQueue.isEmpty() && result == null) {
            FilterSet filter = filterQueue.poll();
            if (filter.shouldFilter()) result = filter.filter();
        }
        while (!filterQueue.isEmpty()) {
            if (result.isEmpty()) return resultHandler.handle(new HashSet<T>());
            FilterSet filter = filterQueue.poll();
            if (filter.shouldFilter()) result.retainAll(filter.filter());
        }
        return resultHandler.handle(result);
    }

    public static class Builder<T extends Comparable> {
        private Queue<FilterSet> filterQueue = new LinkedList<>();

        public Builder<T> addFilter(FilterSet<T> item) {
            filterQueue.add(item);
            return this;
        }

        public QueryExecuter<T> build(Class<T> clazz) {
            return new QueryExecuter<>(clazz, filterQueue);
        }
    }
}
