package com.ctrip.zeus.util;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by zhoumy on 2016/12/5.
 */
public class CircularArray<T> implements Iterable<T> {
    private final LinkedList<T> buckets;
    private final Class<T> tClass;
    private final int length;
    private T lastEntry;

    public CircularArray(int length, Class<T> tClass) {
        buckets = new LinkedList<>();
        this.length = length + 1;
        this.tClass = tClass;
    }

    public void add(T entry) {
        buckets.add(entry);
        while (buckets.size() > length) {
            buckets.removeFirst();
        }
        lastEntry = entry;
    }

    public T[] getAll() {
        T[] result = (T[]) Array.newInstance(tClass, buckets.size() < length ? buckets.size() : length);
        int i = 0;
        Iterator<T> iter = buckets.iterator();
        while (iter.hasNext() && i < result.length) {
            T e = iter.next();
            result[i] = e;
            i++;
        }
        return result;
    }

    public int size() {
        return buckets.size();
    }

    public void clear() {
        buckets.clear();
    }

    public T getLast() {
        return lastEntry;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(buckets).iterator();
    }
}
