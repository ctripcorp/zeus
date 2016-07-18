package com.ctrip.zeus.service.query;

import com.ctrip.zeus.exceptions.ValidationException;

/**
 * Created by zhoumy on 2016/7/15.
 */
public class VsQueryCommand implements QueryCommand {
    public int id = 0;
    public int name = 1;
    public int domain = 2;
    public int ssl = 3;
    public int slb_id = 4;
    public int group_search_key = 5;

    private String[] values = new String[6];
    private final String type;

    public VsQueryCommand() {
        type = "vs";
    }

    @Override
    public boolean add(String queryName, String queryValue) throws ValidationException {
        int idx;
        switch (queryName) {
            case "id":
            case "vsId":
                idx = id;
                break;
            case "name":
            case "vsName":
                idx = name;
                break;
            case "domain":
                idx = domain;
                break;
            case "ssl":
                idx = ssl;
                break;
            // preserved query name
            case "ip":
                return true;
            default:
                return false;
        }
        values[idx] = queryValue;
        return true;
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
