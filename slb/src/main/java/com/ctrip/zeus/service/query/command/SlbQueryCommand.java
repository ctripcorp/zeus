package com.ctrip.zeus.service.query.command;

/**
 * Created by zhoumy on 2016/7/15.
 */
public class SlbQueryCommand implements QueryCommand {
    public final static int id = 0;
    public final static int name = 1;
    public final static int ip = 2;
    public final static int vip = 3;
    public final static int vs_search_key = 4;
    public final static int fuzzy_name = 5;

    private final String type;
    private String[] values = new String[6];

    public SlbQueryCommand() {
        type = "slb";
    }

    @Override
    public boolean add(String queryName, String queryValue) {
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
            case "fuzzyName":
                idx = fuzzy_name;
                break;
            default:
                return false;
        }
        return addAtIndex(idx, queryValue);
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
