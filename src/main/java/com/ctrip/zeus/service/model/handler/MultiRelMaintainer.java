package com.ctrip.zeus.service.model.handler;

import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
public interface MultiRelMaintainer<T, W, X> {

    void relAdd(X object, Class<T> clazz, List<W> input) throws Exception;

    void relUpdateOffline(X object, Class<T> clazz, List<W> input) throws Exception;

    void relUpdateOnline(X object, Class<T> clazz, List<W> input) throws Exception;

    void relDelete(Long objectId) throws Exception;

    void relBatchDelete(Long[] objectIds) throws Exception;
}
