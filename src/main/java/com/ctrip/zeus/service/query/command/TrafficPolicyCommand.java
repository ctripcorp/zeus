package com.ctrip.zeus.service.query.command;

/**
 * Created by zhoumy on 2017/1/18.
 */
public class TrafficPolicyCommand implements QueryCommand {
    public final static int id = 0;
    public final static int name = 1;
    public final static int group_id = 2;
    public final static int vs_id = 3;
    public final static int fuzzy_name = 4;

    private final String type;
    private final String[] values = new String[5];

    public TrafficPolicyCommand() {
        type = "policy";
    }

    @Override
    public boolean add(String queryName, String queryValue) {
        int idx;
        switch (queryName) {
            case "id":
            case "policyId":
                idx = id;
                break;
            case "name":
            case "policyName":
                idx = name;
                break;
            case "groupId":
                idx = group_id;
                break;
            case "vsId":
                idx = vs_id;
                break;
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
