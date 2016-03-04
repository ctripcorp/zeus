package com.ctrip.zeus.service.model.handler.impl;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2016/1/8.
 */
public abstract class MultiRelMaintainerEx<T, W, X> extends AbstractMultiRelMaintainer<T, W, X> {

    protected MultiRelMaintainerEx(Class<T> clazzT, Class<X> clazzX) {
        super(clazzT, clazzX);
    }

    @Override
    public void port(X object) throws Exception {
        Long objectId = getObjectId(object);
        List<T> recycled = getAll(objectId);
        List<W> rels = getRelations(object);

        Map<String, List<T>> actionMap = groupByAction(object, rels, recycled, clazzT);

        List<T> action = actionMap.get("update");
        if (action != null) updateByPrimaryKey(action.toArray((T[]) Array.newInstance(clazzT, action.size())));

        action = actionMap.get("delete");
        if (action != null) deleteByPrimaryKey(action.toArray((T[]) Array.newInstance(clazzT, action.size())));

        action = actionMap.get("insert");
        if (action != null) insert(action.toArray((T[]) Array.newInstance(clazzT, action.size())));
    }

    protected abstract List<T> getAll(Long id) throws Exception;
}
