package com.ctrip.zeus.model;

public class TrafficPoint {
    private String groupBy;
    private long count;
    private double qps;


    public double getQps() {
        return qps;
    }

    public TrafficPoint setQps(double qps) {
        this.qps = qps;
        return this;
    }

    public TrafficPoint() {
    }

    public TrafficPoint(String groupBy, long count, double qps) {
        this.groupBy = groupBy;
        this.count = count;
        this.qps = qps;
    }

    public TrafficPoint(String groupBy, long count) {
        this.groupBy = groupBy;
        this.count = count;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public TrafficPoint setGroupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public long getCount() {
        return count;
    }

    public TrafficPoint setCount(long count) {
        this.count = count;
        return this;
    }
}
