package com.ctrip.zeus.model;

import java.util.List;
import java.util.Map;

public class TrafficQueryCommand {
    private Map<String, String> tags;
    private List<String> groupBy;
    private Map<String, String> extraQuery;


    public Map<String, String> getExtraQuery() {
        return extraQuery;
    }

    public TrafficQueryCommand setExtraQuery(Map<String, String> extraQuery) {
        this.extraQuery = extraQuery;
        return this;
    }

    public TrafficQueryCommand() {
    }

    public TrafficQueryCommand(Map<String, String> tags, List<String> groupBy) {
        this.tags = tags;
        this.groupBy = groupBy;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public TrafficQueryCommand setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public TrafficQueryCommand setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
        return this;
    }
}
