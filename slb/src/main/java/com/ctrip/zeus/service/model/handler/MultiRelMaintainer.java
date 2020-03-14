package com.ctrip.zeus.service.model.handler;

import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
public interface MultiRelMaintainer<W, X> {

    void insert(X object) throws Exception;

    void refreshOffline(X object) throws Exception;

    void refreshOnline(X[] object) throws Exception;

    void clear(Long objectId) throws Exception;

    List<W> get(X object) throws Exception;
}
