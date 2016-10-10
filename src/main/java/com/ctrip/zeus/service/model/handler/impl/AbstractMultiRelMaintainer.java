package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.handler.MultiRelMaintainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by zhoumy on 2015/12/22.
 */
public abstract class AbstractMultiRelMaintainer<T, W, X> implements MultiRelMaintainer<W, X> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final int OFFSET_OFFLINE = 0;
    protected static final int OFFSET_ONLINE = 1;
    private static final Map<String, Method> MethodCache = new HashMap<>();

    protected final Class<T> domainClazz;
    protected final String clazzName;

    private Method m_getId;
    private Method m_getVersion;

    protected AbstractMultiRelMaintainer(Class<T> domainClazz, Class<X> viewClazz) {
        this.domainClazz = domainClazz;
        clazzName = viewClazz.getSimpleName();
        m_getId = MethodCache.get(clazzName + "#getId");
        m_getVersion = MethodCache.get(clazzName + "#getVersion");
        if (m_getId == null) {
            try {
                m_getId = viewClazz.getMethod("getId");
                MethodCache.put(clazzName + "#getId", m_getId);
            } catch (NoSuchMethodException e) {
                logger.error("Cannot find getId() method from class " + clazzName + ".", e);
            }
        }
        if (m_getVersion == null) {
            try {
                m_getVersion = viewClazz.getMethod("getVersion");
                MethodCache.put(clazzName + "#getVersion", m_getVersion);
            } catch (NoSuchMethodException e) {
                logger.error("Cannot find getVersion() method from class " + clazzName + ".", e);
            }
        }
    }

    @Override
    public void insert(X object) throws Exception {
        List<W> rels = get(object);
        T[] dos = (T[]) Array.newInstance(domainClazz, rels.size());
        for (int i = 0; i < dos.length; i++) {
            T d = domainClazz.newInstance();
            setDo(object, rels.get(i), d);
            dos[i] = d;
        }
        insert(dos);
    }

    @Override
    public void refreshOffline(X object) throws Exception {
        List<W> rels = get(object);
        Integer[] versions = getStatusByObjectId(object);
        if (versions[OFFSET_OFFLINE].intValue() == versions[OFFSET_ONLINE].intValue()) {
            insert(object);
            return;
        }
        int onlineVersion = versions[OFFSET_ONLINE];
        List<T> offline = new ArrayList<>();
        for (T t : getRelsByObjectId(object)) {
            if (getIdxKey(t).getVersion().intValue() != onlineVersion) {
                offline.add(t);
            }
        }

        Map<String, List<T>> actionMap = groupByAction(object, rels, offline, domainClazz);
        List<T> action = actionMap.get("update");
        if (action != null) updateByPrimaryKey(action.toArray((T[]) Array.newInstance(domainClazz, action.size())));

        action = actionMap.get("delete");
        if (action != null) deleteByPrimaryKey(action.toArray((T[]) Array.newInstance(domainClazz, action.size())));

        action = actionMap.get("insert");
        if (action != null) insert(action.toArray((T[]) Array.newInstance(domainClazz, action.size())));
    }

    @Override
    public void refreshOnline(X[] objects) throws Exception {
        Long[] ids = new Long[objects.length];
        Map<Long, Integer> idx = new HashMap<>();

        List<T>[] dosRef = new List[objects.length];
        List<Integer[]> versionRef = new ArrayList<>(objects.length);
        final Integer[] initValue = new Integer[]{0, 0};

        for (int i = 0; i < objects.length; i++) {
            Long id = getObjectId(objects[i]);
            idx.put(id, i);
            ids[i] = id;
            dosRef[i] = new ArrayList<>();
            versionRef.add(initValue);
        }

        for (T d : getRelsByObjectId(ids)) {
            dosRef[idx.get(getIdxKey(d).getId())].add(d);
        }
        for (Map.Entry<Long, Integer[]> e : getStatusByObjectId(ids).entrySet()) {
            versionRef.set(idx.get(e.getKey()), e.getValue());
        }

        Map<String, List<T>> actionMap = new HashMap<>();
        List<T> add = new ArrayList<>();

        for (int i = 0; i < objects.length; i++) {
            List<W> rels;
            X object = objects[i];
            Integer[] versions = versionRef.get(i);
            if (versions[OFFSET_OFFLINE].intValue() == versions[OFFSET_ONLINE].intValue() ||
                    versions[OFFSET_OFFLINE].intValue() == getObjectVersion(object).intValue()) {
                rels = new ArrayList<>();
            } else {
                rels = get(object);
            }

            int retainedVersion = versions[OFFSET_OFFLINE];
            List<T> discard = new ArrayList<>();
            for (T t : dosRef[i]) {
                if (getIdxKey(t).getVersion() != retainedVersion) {
                    discard.add(t);
                }
            }
            for (Map.Entry<String, List<T>> e : groupByAction(object, rels, discard, domainClazz).entrySet()) {
                List<T> v = actionMap.get(e.getKey());
                if (v == null) {
                    actionMap.put(e.getKey(), e.getValue());
                } else {
                    v.addAll(e.getValue());
                }
            }
        }

        List<T> action = actionMap.get("update");
        if (action != null) updateByPrimaryKey(action.toArray((T[]) Array.newInstance(domainClazz, action.size())));

        action = actionMap.get("delete");
        if (action != null) deleteByPrimaryKey(action.toArray((T[]) Array.newInstance(domainClazz, action.size())));

        action = actionMap.get("insert");
        if (action != null) add.addAll(action);
        if (action != null) insert(add.toArray((T[]) Array.newInstance(domainClazz, add.size())));
    }

    protected Map<String, List<T>> groupByAction(X object, List<W> rels, List<T> update, Class<T> clazzT) throws Exception {
        Map<String, List<T>> result = new HashMap<>();
        int i = 0;
        Iterator<T> iter = update.iterator();
        while (iter.hasNext() && i < rels.size()) {
            setDo(object, rels.get(i), iter.next());
            i++;
        }

        final int offset = i;
        if (offset > 0) {
            result.put("update", update.subList(0, offset));
        }
        if (offset < update.size()) {
            result.put("delete", update.subList(offset, update.size()));
        }
        if (offset < rels.size()) {
            List<T> dos = new ArrayList<>();
            for (int j = offset; j < rels.size(); j++) {
                T d = clazzT.newInstance();
                setDo(object, rels.get(j), d);
                dos.add(d);
            }
            result.put("insert", dos);
        }
        return result;
    }

    protected Long getObjectId(X object) {
        if (m_getId != null) {
            try {
                return (Long) m_getId.invoke(object);
            } catch (Exception e) {
                logger.error("Error occurred when invoke getId() from " + clazzName + ".", e);
            }
        }
        return 0L;
    }

    protected Integer getObjectVersion(X object) {
        if (m_getVersion != null) {
            try {
                return (Integer) m_getVersion.invoke(object);
            } catch (Exception e) {
                logger.error("Error occurred when invoke getVersion() from " + clazzName + ".", e);
            }
        }
        return 0;
    }

    protected abstract IdVersion getIdxKey(T rel) throws Exception;

    protected abstract void setDo(X object, W value, T target);

    protected abstract List<T> getRelsByObjectId(X object) throws Exception;

    protected abstract List<T> getRelsByObjectId(Long[] objectIds) throws Exception;

    protected abstract Integer[] getStatusByObjectId(X object) throws Exception;

    protected abstract Map<Long, Integer[]> getStatusByObjectId(Long[] objectIds) throws Exception;

    protected abstract void insert(T[] values) throws Exception;

    protected abstract void updateByPrimaryKey(T[] values) throws Exception;

    protected abstract void deleteByPrimaryKey(T[] values) throws Exception;
}
