package com.ctrip.zeus.service.query.command;

public class RuleQueryCommand implements QueryCommand {
    public final static int id = 0;
    public final static int target_id = 1;
    public final static int target_type = 2;
    public final static int fuzzy_name = 3;

    private final String type;
    private final String[] values = new String[4];

    public RuleQueryCommand() {
        this.type = "Rule";
    }

    @Override
    public boolean add(String queryName, String queryValue) {
        int idx;
        switch (queryName) {
            case "id":
            case "ruleId":
                idx = id;
                break;

            case "targetId":
            case "target":
                idx = target_id;
                break;

            case "targetType":
                idx = target_type;
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
            // parse target type
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
