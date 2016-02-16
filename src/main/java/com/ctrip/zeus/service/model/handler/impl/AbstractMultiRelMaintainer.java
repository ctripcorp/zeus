package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.service.model.handler.MultiRelMaintainer;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by zhoumy on 2015/12/22.
 */
public abstract class AbstractMultiRelMaintainer<T, W, X> implements MultiRelMaintainer<T, W, X> {
    protected static final int OFFSET_OFFLINE = 0;
    protected static final int OFFSET_ONLINE = 1;

    private final Map<Class, Method> methodLookup = new HashMap<>();

    @Override
    public void addRel(X object, Class<T> clazz) throws Exception {
        List<W> input = getRelations(object);
        T[] dos = (T[]) Array.newInstance(clazz, input.size());
        for (int i = 0; i < dos.length; i++) {
            dos[i] = getDo(object, input.get(i));
        }
        insert(dos);
    }

    @Override
    public void updateRel(X object, Class<T> clazz) throws Exception {
        List<W> input = getRelations(object);
        Integer[] versions = getStatusByObjectId(object);
        if (versions[OFFSET_OFFLINE].intValue() == versions[OFFSET_ONLINE].intValue()) {
            addRel(object, clazz);
            return;
        }
        int selectedVersion = versions[OFFSET_OFFLINE];
        Method getVersion = methodLookup.get(clazz);
        if (getVersion == null) {
            getVersion = clazz.getMethod("getVersion");
            methodLookup.put(clazz, getVersion);
        }
        List<T> data = new ArrayList<>();
        for (T t : getDosByObjectId(object)) {
            if ((int) getVersion.invoke(t) == selectedVersion) {
                data.add(t);
            }
        }

        Map<String, List<T>> actionMap = groupByAction(object, input, data);
        List<T> action = actionMap.get("update");
        if (action != null) updateByPrimaryKey(action.toArray((T[]) Array.newInstance(clazz, action.size())));

        action = actionMap.get("delete");
        if (action != null) deleteByPrimaryKey(action.toArray((T[]) Array.newInstance(clazz, action.size())));

        action = actionMap.get("insert");
        if (action != null) insert(action.toArray((T[]) Array.newInstance(clazz, action.size())));
//        int i = 0;
//        Iterator<T> iter = data.iterator();
//        while (iter.hasNext() && i < input.size()) {
//            setDo(object, input.get(i), iter.next());
//            i++;
//        }
//
//        final int offset = i;
//        if (offset > 0) {
//            updateByPrimaryKey(data.subList(0, offset).toArray((T[]) Array.newInstance(clazz, offset)));
//        }
//        if (offset < data.size()) {
//            deleteByPrimaryKey(data.subList(offset, data.size()).toArray((T[]) Array.newInstance(clazz, data.size() - offset + 1)));
//        }
//        if (offset < input.size()) {
//            T[] dos = (T[]) Array.newInstance(clazz, input.size() - offset + 1);
//            for (int j = offset; j < dos.length; j++) {
//                dos[j - offset] = getDo(object, input.get(j));
//            }
//            insert(dos);
//        }
    }

    @Override
    public void updateStatus(X[] objects, Class<T> clazz) throws Exception {
        List<T> add = new ArrayList<>();
        Map<String, List<T>> actionMap = new HashMap<>();
        Map<Integer, Integer[]> versionRef = getStatusByObjectId(objects);
        Map<Integer, T[]> dosRef = getDosByObjectId(objects);
        Method getVersion = methodLookup.get(clazz);
        if (getVersion == null) {
            getVersion = clazz.getMethod("getVersion");
            methodLookup.put(clazz, getVersion);
        }

        for (int i = 0; i < objects.length; i++) {
            X object = objects[i];
            List<W> input = getRelations(object);
            Integer[] versions = versionRef.get(i);
            if (versions[OFFSET_OFFLINE].intValue() == versions[OFFSET_ONLINE].intValue()) {
                for (W w : input) {
                    add.add(getDo(object, w));
                }
                continue;
            }

            int selectedVersion = versions[OFFSET_ONLINE];
            List<T> data = new ArrayList<>();
            for (T t : dosRef.get(i)) {
                if ((int) getVersion.invoke(t) == selectedVersion) {
                    data.add(t);
                }
            }
            actionMap.putAll(groupByAction(object, input, data));
        }

        List<T> action = actionMap.get("update");
        if (action != null) updateByPrimaryKey(action.toArray((T[]) Array.newInstance(clazz, action.size())));

        action = actionMap.get("delete");
        if (action != null) deleteByPrimaryKey(action.toArray((T[]) Array.newInstance(clazz, action.size())));

        action = actionMap.get("insert");
        if (action != null) insert(action.toArray((T[]) Array.newInstance(clazz, action.size())));
    }

    private Map<String, List<T>> groupByAction(X object, List<W> input, List<T> data) {
        Map<String, List<T>> result = new HashMap<>();
        int i = 0;
        Iterator<T> iter = data.iterator();
        while (iter.hasNext() && i < input.size()) {
            setDo(object, input.get(i), iter.next());
            i++;
        }

        final int offset = i;
        if (offset > 0) {
            result.put("update", data.subList(0, offset));
        }
        if (offset < data.size()) {
            result.put("delete", data.subList(offset, data.size()));
        }
        if (offset < input.size()) {
            List<T> dos = new ArrayList<>();
            for (int j = offset; j < input.size(); j++) {
                dos.add(getDo(object, input.get(j)));
            }
        }
        return result;
    }

    protected abstract T getDo(X object, W value);

    protected abstract void setDo(X object, W value, T target);

    protected abstract T[] getDosByObjectId(X object) throws Exception;

    protected abstract Map<Integer, T[]> getDosByObjectId(X[] objects) throws Exception;

    protected abstract Integer[] getStatusByObjectId(X object) throws Exception;

    protected abstract Map<Integer, Integer[]> getStatusByObjectId(X[] objects) throws Exception;

    protected abstract void insert(T[] values) throws Exception;

    protected abstract void updateByPrimaryKey(T[] values) throws Exception;

    protected abstract void deleteByPrimaryKey(T[] values) throws Exception;
}
