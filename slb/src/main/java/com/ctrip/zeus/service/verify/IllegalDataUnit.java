package com.ctrip.zeus.service.verify;

import java.util.List;
import java.util.Map;

/**
 * @Discription
 **/
public class IllegalDataUnit {
    private final List<IdItemType> ids;

    private final Map<String, Object> extraInfo;

    public IllegalDataUnit(List<IdItemType> ids, Map<String, Object> extraInfo) {
        this.ids = ids;
        this.extraInfo = extraInfo;
    }

    public List<IdItemType> getIds() {
        return ids;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }
}
