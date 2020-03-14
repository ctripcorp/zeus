package com.ctrip.zeus.service.change;


import com.ctrip.zeus.model.alert.AlertItem;

import java.util.List;

/**
 * @author:ygshen
 * @date: 2/26/2018.
 */
public interface StatusChangeService {
    /**
     * Batch add new status change records
     **/
    List<AlertItem> batchAddStatusChange(List<AlertItem> item) throws Exception;

    /**
     * Get Status with ids and types
     **/

    List<AlertItem> getStatusChangesByTypeAndIds(Long[] ids, String type);


    /**
     * Get Status with ids, types and closed status
     **/

    List<AlertItem> getStatusChangesByTypeIdsAndStatus(Long[] ids, String type, boolean status);


    /**
     * Get Status with ids, types and closed status
     **/

    List<AlertItem> getStatusChangesByTypeAndStatus(String type, boolean status);

    /**
     * Batch update status changes records
     **/

    List<AlertItem> batchUpdateStatusChanges(List<AlertItem> changes) throws Exception;
}
