package com.ctrip.zeus.service.verify;

import java.util.List;

/**
 * @Discription
 **/
public class VerifyPropertyResult extends VerifyResult {
    private final String pName;

    private final String pValue;

    public VerifyPropertyResult(String itemType, List<Long> itemIds, String pName, String pValue) {
        super(itemType, itemIds);
        this.pName = pName;
        this.pValue = pValue;
    }

    public String getpName() {
        return pName;
    }

    public String getpValue() {
        return pValue;
    }
}
