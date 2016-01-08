package com.ctrip.zeus.restful.filter;

import com.ctrip.zeus.executor.impl.ResultHandler;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by zhoumy on 2015/10/13.
 */
public class QueryExecuter<T extends Comparable> {
    private final Class<T> clazz;
    private final Queue<FilterSet> filterQueue;
    private ResultHandler resultHandler;

    private QueryExecuter(Class<T> clazz) {
        this(clazz, new LinkedList<FilterSet>());
    }

    private QueryExecuter(Class<T> clazz, Queue<FilterSet> filterIdVersionArray) {
        this.clazz = clazz;
        this.filterQueue = filterIdVersionArray;
    }

    private QueryExecuter(Class<T> clazz, Queue<FilterSet> filterIdVersionArray, ResultHandler resultHandler) {
        this.clazz = clazz;
        this.filterQueue = filterIdVersionArray;
        this.resultHandler = resultHandler;
    }

    public T[] run() throws Exception {
        Set<Long> result = null;
        while (!filterQueue.isEmpty() && result == null) {
            FilterSet filter = filterQueue.poll();
            if (filter.shouldFilter()) result = filter.filter();
        }
        while (!filterQueue.isEmpty()) {
            if (result.isEmpty()) return (T[]) Array.newInstance(clazz, 0);
            FilterSet filter = filterQueue.poll();
            if (filter.shouldFilter()) result.retainAll(filter.filter());
        }

        if (resultHandler != null)
            resultHandler.handle(result);
        return result == null ? null : result.toArray((T[]) Array.newInstance(clazz, result.size()));
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public static class Builder<T extends Comparable> {
        private Queue<FilterSet> filterQueue = new LinkedList<>();
        private ResultHandler resultHandler;

        public Builder<T> addFilter(FilterSet<T> item) {
            filterQueue.add(item);
            return this;
        }

        public Builder<T> setResultHandler(ResultHandler<T> resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }

        public QueryExecuter<T> build(Class<T> clazz) {
            return new QueryExecuter<>(clazz, filterQueue, resultHandler);
        }
    }
}
