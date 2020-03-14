package com.ctrip.zeus.service.verify;

import java.util.List;

/**
 * @Discription
 **/
public class VerifyTaggingResult extends VerifyResult {
    private final String tagName;

    public VerifyTaggingResult(String itemType, List<Long> itemIds, String tagName) {
        super(itemType, itemIds);
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }
}
