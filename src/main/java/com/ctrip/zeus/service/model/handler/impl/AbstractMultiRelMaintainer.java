package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.service.model.handler.MultiRelMaintainer;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
public abstract class AbstractMultiRelMaintainer<T, W, X> implements MultiRelMaintainer<T, W, X> {
    @Override
    public void addRel(X object, Class<T> clazz, List<W> input) throws Exception {
        T[] dos = (T[]) Array.newInstance(clazz, input.size());
        for (int i = 0; i < dos.length; i++) {
            dos[i] = getDo(object, input.get(i));
        }
        insert(dos);
    }

    @Override
    public void updateRel(X object, Class<T> clazz, List<W> input) throws Exception {
        T[] dos = (T[]) Array.newInstance(clazz, input.size());
        for (int i = 0; i < dos.length; i++) {
            dos[i] = getDo(object, input.get(i));
        }
        insert(dos);
    }

    protected abstract T getDo(X object, W value) throws Exception;

    protected abstract void insert(T[] values) throws Exception;

    protected abstract void updateByPrimaryKey(T[] values) throws Exception;

    protected abstract void deleteByPrimaryKey(T[] values) throws Exception;
}
