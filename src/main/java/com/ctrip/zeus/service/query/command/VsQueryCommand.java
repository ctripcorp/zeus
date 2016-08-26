package com.ctrip.zeus.service.query.command;

/**
 * Created by zhoumy on 2016/7/15.
 */
public class VsQueryCommand implements QueryCommand {
    public final static int id = 0;
    public final static int name = 1;
    public final static int domain = 2;
    public final static int ssl = 3;
    public final static int slb_id = 4;
    public final static int group_search_key = 5;
    public final static int fuzzy_name = 6;

    private String[] values = new String[7];
    private final String type;

    public VsQueryCommand() {
        type = "vs";
    }

    @Override
    public boolean add(String queryName, String queryValue) {
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
            case "slbId":
                idx = slb_id;
                break;
            case "fuzzyName":
                idx = fuzzy_name;
                break;
            default:
                return false;
        }
        values[idx] = queryValue;
        return true;
    }

    @Override
    public boolean addAtIndex(int idx, String queryValue) {
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
        return value == null ? new String[0] : value.split(",");
    }

    @Override
    public String getType() {
        return type;
    }
}
