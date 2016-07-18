package com.ctrip.zeus.service.query;

import com.ctrip.zeus.exceptions.ValidationException;

/**
 * Created by zhoumy on 2016/7/15.
 */
public class SlbQueryCommand implements QueryCommand {
    public final int id = 0;
    public final int name = 1;
    public final int ip = 2;
    public final int vip = 3;
    public final int vs_search_key = 4;

    private final String type;
    private String[] values = new String[5];

    public SlbQueryCommand() {
        type = "slb";
    }

    @Override
    public boolean add(String queryName, String queryValue) throws ValidationException {
        int idx;
        switch (queryName) {
            case "id":
            case "slbId":
                idx = id;
                break;
            case "name":
            case "slbName":
                idx = name;
                break;
            case "ip":
                idx = ip;
                break;
            case "vip":
                return true;
            default:
                return false;
        }
        return addAtIndex(idx, queryValue);
    }

    @Override
    public boolean addAtIndex(int idx, String queryValue) throws ValidationException {
        if (idx < values.length) {
            values[idx] = queryValue;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasValue(int idx) {
        return values[idx] != null;
    }

    @Override
    public String[] getValue(int idx) {
        String value = values[idx];
        return value == null ? null : value.split(",");
    }

    @Override
    public String getType() {
        return type;
    }
}
