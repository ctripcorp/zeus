package com.ctrip.zeus.service.model.handler.impl;

import java.util.Iterator;
import java.util.List;

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
        Integer[] statusVersion = getStatusByObjectId(object);
        List<T> recycled = getAll(objectId);
        Iterator<T> iter = recycled.iterator();
        while (iter.hasNext()) {
            if (getIdxKey(iter.next()).getVersion().intValue() == statusVersion[OFFSET_ONLINE].intValue()) iter.remove();
        }

        List<W> rels = getRelations(object);

        int i;
        for (i = 0; i < recycled.size(); i++) {
            setDo(object, rels.get(i), recycled.get(i));
        }

        groupByAction(object, rels, recycled, clazzT);
//        if (rels.size() >= recycled.size()) {
//            updateByPrimaryKey(recycled.toArray((T[]) Array.newInstance(clazz, recycled.size())));
//            if (rels.size() > recycled.size()) {
//                T[] dos = (T[]) Array.newInstance(clazz, rels.size() - i);
//                for (int j = i; j < rels.size(); j++) {
//                    dos[j - i] = getDo(object, rels.get(j));
//                }
//                insert(dos);
//            }
//        } else {
//            deleteByPrimaryKey(recycled.subList(i - 1, recycled.size()).toArray((T[]) Array.newInstance(clazz, recycled.size() - i + 1)));
//        }
    }

    protected abstract List<T> getAll(Long id) throws Exception;
}
