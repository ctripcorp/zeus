package com.ctrip.zeus.service.query;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.command.QueryCommand;

import java.util.Set;

/**
 * Created by zhoumy on 2016/7/19.
 */
public interface CriteriaQuery {

    Long queryByName(String name) throws Exception;

    Set<Long> fuzzyQueryByName(String name) throws Exception;

    IdVersion[] queryByCommand(QueryCommand query, SelectionMode mode) throws Exception;

    Set<Long> queryAll() throws Exception;

    Set<IdVersion> queryAll(SelectionMode mode) throws Exception;

    Set<IdVersion> queryByIdsAndMode(Long[] ids, SelectionMode mode) throws Exception;

    IdVersion[] queryByIdAndMode(Long id, SelectionMode mode) throws Exception;
}
