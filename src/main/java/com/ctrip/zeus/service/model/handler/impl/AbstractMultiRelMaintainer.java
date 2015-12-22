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
    public void relUpdate(X object, Class<T> clazz, List<W> input) throws Exception {
        // current version in use
        Long targetId = getTargetId(object);
        if (currentRetained(targetId)) {
            relAdd(object, clazz, input);
            return;
        }
        List<T> check = getAll(targetId);
        Iterator<T> iter = check.iterator();
        int currentVersion = getCurrentVersion(object);
        while (iter.hasNext()) {
            if (getTargetVersion(iter.next()) != (currentVersion - 1)) iter.remove();
        }
        int i;
        for (i = 0; i < check.size(); i++) {
            reassign(object, check.get(i), input.get(i));
        }
        // update existing records, if size(new) > size(old), insert the rest new records.
        if (input.size() >= check.size()) {
            updateByPrimaryKey(check.toArray((T[]) Array.newInstance(clazz, check.size())));
            if (input.size() > check.size()) {
                T[] dos = (T[]) Array.newInstance(clazz, input.size() - i);
                for (int j = i; j < input.size(); j++) {
                    dos[j - i] = getDo(object, input.get(j));
                }
                insert(dos);
            }
        } else {
            // size(new) < size(old), delete the rest old records.
            deleteByPrimaryKey(check.subList(i - 1, check.size()).toArray((T[]) Array.newInstance(clazz, check.size() - i + 1)));
        }
    }

    @Override
    public void relPort(X object, Class<T> clazz, List<W> input) throws Exception {
        // current version in use
        Long targetId = getTargetId(object);
        if (currentRetained(targetId)) {
            relAdd(object, clazz, input);
            return;
        }
        List<T> check = getAll(targetId);
        Iterator<T> iter = check.iterator();
        int currentVersion = getCurrentVersion(object);
        while (iter.hasNext()) {
            int targetVersion = getTargetVersion(iter.next());
            if (targetVersion != 0
                    && targetVersion != currentVersion
                    && targetVersion != (currentVersion - 1)) iter.remove();
        }
        int i;
        for (i = 0; i < check.size() && i < input.size(); i++) {
            reassign(object, check.get(i), input.get(i));
        }
        // update existing records, if size(new) > size(old), insert the rest new records.
        if (input.size() >= check.size()) {
            updateByPrimaryKey(check.toArray((T[]) Array.newInstance(clazz, check.size())));
            if (input.size() > check.size()) {
                T[] dos = (T[]) Array.newInstance(clazz, input.size() - i);
                for (int j = i; j < input.size(); j++) {
                    dos[j - i] = getDo(object, input.get(j));
                }
                insert(dos);
            }
        } else {
            // size(new) < size(old), delete the rest old records.
            deleteByPrimaryKey(check.subList(i - 1, check.size()).toArray((T[]) Array.newInstance(clazz, check.size() - i + 1)));
        }
    }

    protected abstract List<T> getAll(Long id) throws Exception;

    protected abstract int getCurrentVersion(X object);

    protected abstract int getTargetVersion(T target) throws Exception;

    protected abstract void updateByPrimaryKey(T[] values) throws Exception;

    protected abstract void insert(T[] values) throws Exception;

    protected abstract void deleteByPrimaryKey(T[] values) throws Exception;

    protected abstract Long getTargetId(X object) throws Exception;

    protected abstract T getDo(X object, W value) throws Exception;

    protected abstract void reassign(X object, T output, W input) throws Exception;

    protected abstract boolean currentRetained(Long id) throws Exception;
}
