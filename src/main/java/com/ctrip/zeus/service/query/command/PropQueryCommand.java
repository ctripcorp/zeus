package com.ctrip.zeus.service.query.command;

import com.ctrip.zeus.tag.entity.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2016/7/19.
 */
public class PropQueryCommand implements QueryCommand {
    public final static int union_prop = 0;
    public final static int join_prop = 1;
    public final static int id = 2;
    public final static int item_type = 3;

    private final String type;
    private PropQueryCommand next;
    private PropQueryCommand last;

    private String[] values = new String[4];

    public PropQueryCommand() {
        type = "prop";
        last = this;
    }

    @Override
    public boolean add(String queryName, String queryValue) {
        int idx;
        switch (queryName) {
            case "anyProp":
            case "unionProp":
                idx = union_prop;
                break;
            case "prop":
            case "joinProp":
                idx = join_prop;
                break;
            case "targetId":
                idx = id;
                break;
            case "targetType":
                idx = item_type;
                break;
            default:
                return false;
        }
        return addAtIndex(idx, queryValue);
    }

    @Override
    public boolean addAtIndex(int idx, String queryValue) {
        if (idx < values.length) {
            last.values[idx] = queryValue;
            last.next = new PropQueryCommand();
            last = last.next;
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

    public List<Property> getProperties(int idx) {
        List<Property> properties = new ArrayList<>();
        for (String s : getValue(idx)) {
            int ps = s.trim().indexOf(':');
            if (ps == -1) continue;
            properties.add(new Property().setName(s.substring(0, ps)).setValue(s.substring(ps + 1)));
        }
        return properties;
    }

    @Override
    public String getType() {
        return type;
    }

    public PropQueryCommand next() {
        return next;
    }
}