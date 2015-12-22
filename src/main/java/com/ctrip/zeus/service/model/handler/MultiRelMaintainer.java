package com.ctrip.zeus.service.model.handler;

import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
public interface MultiRelMaintainer<T, W, X> {

    void relAdd(X object, Class<T> clazz, List<W> input) throws Exception;

    void relUpdate(X object, Class<T> clazz, List<W> input) throws Exception;

    void relDelete(Long objectId) throws Exception;

    @Deprecated
    void relPort(X object, Class<T> clazz, List<W> input) throws Exception;
}
