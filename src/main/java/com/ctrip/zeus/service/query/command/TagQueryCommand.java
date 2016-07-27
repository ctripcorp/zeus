package com.ctrip.zeus.service.query.command;

/**
 * Created by zhoumy on 2016/7/19.
 */
public class TagQueryCommand implements QueryCommand {
    public final static int union_tag = 0;
    public final static int join_tag = 1;
    public final static int item_id = 2;
    public final static int item_type = 3;

    private final String type;

    private String[] values = new String[4];

    public TagQueryCommand() {
        type = "tag";
    }

    @Override
    public boolean add(String queryName, String queryValue) {
        int idx;
        switch (queryName) {
            case "anyTag":
            case "unionTag":
                idx = union_tag;
                break;
            case "tags":
            case "joinTag":
                idx = join_tag;
                break;
            case "targetId":
                idx = item_id;
                break;
            case "targetType":
                idx =item_type;
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
