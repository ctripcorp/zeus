package com.ctrip.zeus.service.query.command;

/**
 * Created by zhoumy on 2016/7/15.
 */
public class GroupQueryCommand implements QueryCommand {
    public final int id = 0;
    public final int name = 1;
    public final int app_id = 2;
    public final int member_ip = 3;
    public final int vs_id = 4;

    private final String type;
    private final String[] values = new String[5];

    public GroupQueryCommand() {
        this.type = "group";
    }

    @Override
    public boolean add(String queryName, String queryValue) {
        int idx;
        switch (queryName) {
            case "id":
            case "groupId":
                idx = id;
                break;
            case "name":
            case "groupName":
                idx = name;
                break;
            case "appId":
                idx = app_id;
                break;
            case "ip":
            case "member":
                idx = member_ip;
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
