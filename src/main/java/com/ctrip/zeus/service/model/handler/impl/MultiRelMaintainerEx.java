package com.ctrip.zeus.service.model.handler.impl;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhoumy on 2016/1/8.
 */
public abstract class MultiRelMaintainerEx<T, W, X> extends AbstractMultiRelMaintainer<T, W, X> {

    @Override
    public void port(X object, Class<T> clazz, List<W> input) throws Exception {
        Long targetId = getTargetId(object);
        int retainedVersion = getOnlineVersion(targetId);
        List<T> cycle = getAll(targetId);
        Iterator<T> iter = cycle.iterator();
        while (iter.hasNext()) {
            if (getTargetVersion(iter.next()) == retainedVersion) iter.remove();
        }

        int i;
        for (i = 0; i < cycle.size(); i++) {
            reassign(object, cycle.get(i), input.get(i));
        }
        // update existing records, if size(new) > size(old), insert the rest new records.
        if (input.size() >= cycle.size()) {
            updateByPrimaryKey(cycle.toArray((T[]) Array.newInstance(clazz, cycle.size())));
            if (input.size() > cycle.size()) {
                T[] dos = (T[]) Array.newInstance(clazz, input.size() - i);
                for (int j = i; j < input.size(); j++) {
                    dos[j - i] = getDo(object, input.get(j));
                }
                insert(dos);
            }
        } else {
            // size(new) < size(old), delete the rest old records.
            deleteByPrimaryKey(cycle.subList(i - 1, cycle.size()).toArray((T[]) Array.newInstance(clazz, cycle.size() - i + 1)));
        }
    }

    protected abstract List<T> getAll(Long id) throws Exception;

    protected abstract Long getTargetId(X object) throws Exception;

    protected abstract int getTargetVersion(T target) throws Exception;

    protected abstract int getOnlineVersion(Long id) throws Exception;

    protected abstract void reassign(X object, T output, W input) throws Exception;
}
