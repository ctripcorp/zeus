package com.ctrip.zeus.model;


import com.ctrip.zeus.logstats.tools.ReqStats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficEntity {
    private Map<String, List<Integer>> tagMap = new HashMap<>();
    private ArrayList<Map<String, String>> tagsArray = null;
    private ArrayList<ReqStats> reqStats = null;


    public TrafficEntity(Map<String, List<Integer>> tagMap, ArrayList<Map<String, String>> tagsArray, ArrayList<ReqStats> reqStats) {
        this.tagMap = tagMap;
        this.tagsArray = tagsArray;
        this.reqStats = reqStats;
    }

    public Map<String, List<Integer>> getTagMap() {
        return tagMap;
    }

    public TrafficEntity setTagMap(Map<String, List<Integer>> tagMap) {
        this.tagMap = tagMap;
        return this;
    }

    public ArrayList<Map<String, String>> getTagsArray() {
        return tagsArray;
    }

    public TrafficEntity setTagsArray(ArrayList<Map<String, String>> tagsArray) {
        this.tagsArray = tagsArray;
        return this;
    }

    public ArrayList<ReqStats> getReqStats() {
        return reqStats;
    }

    public TrafficEntity setReqStats(ArrayList<ReqStats> reqStats) {
        this.reqStats = reqStats;
        return this;
    }
}
