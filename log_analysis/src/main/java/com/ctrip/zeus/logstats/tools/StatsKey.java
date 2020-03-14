package com.ctrip.zeus.logstats.tools;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatsKey {
    private String name;
    private StringBuffer tags = new StringBuffer();
    private boolean needReportCount = true;
    private boolean needReportCost = true;
    private boolean needReportRequestSize = true;
    private boolean needReportResponseSize = true;
    private boolean needReportStatus = true;
    private Date time;

    public StatsKey(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must be not null.");
        }
        this.name = name;
    }

    public StatsKey reportCount(boolean needReportCount) {
        this.needReportCount = needReportCount;
        return this;
    }

    public StatsKey reportCost(boolean needReportCost) {
        this.needReportCost = needReportCost;
        return this;
    }

    public StatsKey reportRequestSize(boolean needReportRequestSize) {
        this.needReportRequestSize = needReportRequestSize;
        return this;
    }

    public StatsKey reportResponseSize(boolean needReportResponseSize) {
        this.needReportResponseSize = needReportResponseSize;
        return this;
    }

    public StatsKey reportStatus(boolean needReportStatus) {
        this.needReportStatus = needReportStatus;
        return this;
    }

    public StatsKey addTag(String name, String value) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must be not null.");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value must be not null.");
        }
        tags.append(name).append("[:::]").append(value).append("[,,,]");
        return this;
    }

    protected StatsKey setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getName() {
        return name;
    }

    public Date getTime() {
        return time;
    }

    public boolean reportCount() {
        return needReportCount;
    }

    public boolean reportCost() {
        return needReportCost;
    }

    public boolean reportRequestSize() {
        return needReportRequestSize;
    }

    public boolean reportResponseSize() {
        return needReportResponseSize;
    }

    public boolean reportStatus() {
        return needReportStatus;
    }

    public Map<String, String> getTags() {
        Map<String, String> m = new HashMap<String, String>();
        for (String pair : tags.toString().split("\\[,,,\\]")) {
            String[] ps = pair.split("\\[:::\\]");
            if (ps.length == 2) {
                m.put(ps[0], ps[1]);
            }
        }
        return m;
    }

    @Override
    public int hashCode() {
        int res = 17;
        res = 37 * res + name.hashCode();
        res = 37 * res + tags.toString().hashCode();
        if (time != null) {
            res = 37 * res + time.hashCode();
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof StatsKey)) {
            return false;
        }

        StatsKey s = (StatsKey) obj;
        if (time == null) {
            return name.equals(s.name) && tags.toString().equals(s.tags.toString());
        } else {
            return name.equals(s.name) && tags.toString().equals(s.tags.toString())
                    && time.getTime() == s.getTime().getTime();
        }
    }

    public static void main(String[] args) {
        StatsKey k = new StatsKey("aaa");
        k.addTag("a","va").addTag("b","vb");

        System.out.println(k.getName());
        System.out.println(k.getTags());
    }
}

