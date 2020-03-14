package com.ctrip.zeus.service.verify;

import java.util.List;

/**
 * @Discription
 **/
public class VerifyResult {
    private final String targetItemType;
    private final List<Long> targetItemIds;

    /*
     * @Description
     * @Constraint: all items specified by targetItemIds must belong to targetItemType passed in
     * @return
     **/
    public VerifyResult(String itemType, List<Long> itemIds) {
        this.targetItemType = itemType;
        this.targetItemIds = itemIds;
    }

    public String getTargetItemType() {
        return targetItemType;
    }

    public List<Long> getTargetItemIds() {
        return targetItemIds;
    }
}
