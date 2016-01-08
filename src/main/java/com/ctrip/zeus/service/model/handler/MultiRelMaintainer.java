package com.ctrip.zeus.service.model.handler;

import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
public interface MultiRelMaintainer<T, W, X> {

    void addRel(X object, Class<T> clazz, List<W> input) throws Exception;

    void updateRel(X object, Class<T> clazz, List<W> input) throws Exception;

    void deleteRel(Long objectId) throws Exception;

    void batchDeleteRel(Long[] objectIds) throws Exception;

    @Deprecated
    void port(X object, Class<T> clazz, List<W> input) throws Exception;
}
