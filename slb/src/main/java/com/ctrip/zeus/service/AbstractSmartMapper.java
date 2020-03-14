package com.ctrip.zeus.service;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractSmartMapper<R> implements SmartMapper<R> {

    private final Logger logger = LoggerFactory.getLogger(AbstractSmartMapper.class);

    protected final DynamicIntProperty STEP = DynamicPropertyFactory.getInstance().getIntProperty("in.query.step", 500);

    public final List<R> query(SplitConfig<R> config, Object... args) {
        List<R> res = new ArrayList<>();

        try {
            List<List<Object>> splitArgsList = config.splitArgs(args);
            for (List<Object> newArgs: splitArgsList) {
                res.addAll(config.doQuery(newArgs.toArray()));
            }
        } catch (ArgsSplitException e) {
            logger.warn("Exception happens when split args. Use original mapper. Msg: " + e.getMessage());
            return config.doQuery(args);
        }

        return res;
    }

    protected final <T> List<List<T>> split(T... array) {
        List<List<T>> res = new ArrayList<>();
        int step = STEP.get();
        for (int start = 0;start < array.length;start += step) {
            int end = Math.min(start + step, array.length);
            T[] sub = Arrays.copyOfRange(array, start, end);
            res.add(Arrays.asList(sub));
        }
        return res;
    }

    public interface SplitConfig<R> {
        List<R> doQuery(Object... args);
        List<List<Object>> splitArgs(Object... args) throws ArgsSplitException;
    }
}
