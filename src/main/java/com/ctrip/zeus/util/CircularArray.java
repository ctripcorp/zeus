package com.ctrip.zeus.util;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by zhoumy on 2016/12/5.
 */
public class CircularArray<T> implements Iterable<T> {
    private final LinkedList<T> buckets;
    private final int length;
    private T lastEntry;
    private Map<String, Long[]> lastReqStatus;

    public CircularArray(int length) {
        buckets = new LinkedList<>();
        this.length = length + 1;
    }

    public void add(T entry) {
        buckets.add(entry);
        if (buckets.size() == length) {
            buckets.removeFirst();
        }
        lastEntry = entry;
    }

    public T[] getAll() {
        return (T[]) Array.newInstance(buckets.getClass().getComponentType(), length);
    }

    public int size() {
        return buckets.size();
    }

    public void clear() {
        buckets.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(buckets).iterator();
    }
}
