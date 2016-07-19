package com.ctrip.zeus.service.query.command;

/**
 * Created by zhoumy on 2016/7/19.
 */
public class TagQueryCommand implements QueryCommand {
    public final int union_tag = 0;
    public final int join_tag = 1;

    private final String type;

    private String[] values = new String[2];

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
        return value == null ? null : value.split(",");
    }

    @Override
    public String getType() {
        return type;
    }
}
