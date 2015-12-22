package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.service.model.handler.MultiRelMaintainer;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
public abstract class AbstractMultiRelMaintainer<T, W, X> implements MultiRelMaintainer<T, W, X> {
    @Override
    public void relAdd(X object, Class<T> clazz, List<W> input) throws Exception {
        T[] dos = (T[]) Array.newInstance(clazz, input.size());
        for (int i = 0; i < dos.length; i++) {
            dos[i] = getDo(object, input.get(i));
        }
        insert(dos);
    }

    @Override
    public void relUpdateOffline(X object, Class<T> clazz, List<W> input) throws Exception {
        Long targetId = getTargetId(object);
        relUpdate(object, clazz, input, targetId, getOnlineVersion(targetId));
    }

    @Override
    public void relUpdateOnline(X object, Class<T> clazz, List<W> input) throws Exception {
        Long targetId = getTargetId(object);
        relUpdate(object, clazz, input, targetId, getOfflineVersion(targetId));
    }

    private void relUpdate(X object, Class<T> clazz, List<W> input, Long targetId, int retainedVersion) throws Exception {
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

    protected abstract int getOfflineVersion(Long id) throws Exception;

    protected abstract T getDo(X object, W value) throws Exception;

    protected abstract void updateByPrimaryKey(T[] values) throws Exception;

    protected abstract void insert(T[] values) throws Exception;

    protected abstract void deleteByPrimaryKey(T[] values) throws Exception;

    protected abstract void reassign(X object, T output, W input) throws Exception;
}
