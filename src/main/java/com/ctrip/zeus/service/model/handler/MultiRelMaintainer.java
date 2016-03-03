package com.ctrip.zeus.service.model.handler;

import java.util.List;

/**
 * Created by zhoumy on 2015/12/22.
 */
public interface MultiRelMaintainer<W, X> {

    void addRel(X object) throws Exception;

    void updateRel(X object) throws Exception;

    void updateStatus(X[] object) throws Exception;

    void deleteRel(Long objectId) throws Exception;

    void batchDeleteRel(Long[] objectIds) throws Exception;

    List<W> getRelations(X object) throws Exception;

    @Deprecated
    void port(X object) throws Exception;
}
