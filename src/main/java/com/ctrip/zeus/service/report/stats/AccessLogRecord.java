package com.ctrip.zeus.service.report.stats;

import com.ctriposs.tools.reqmetrics.StatsKey;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.Stack;

/**
 * Created by zhoumy on 2015/11/20.
 */
public class AccessLogRecord {
    private final String costKey = "request_time";
    private final String statusKey = "status";
    private final String upCostKey = "upstream_response_time";
    private final Long slbId;
    private final Set<String> keys = ImmutableSet.of("host", "uri", "server_port", statusKey, costKey,
            upCostKey, "upstream_addr", "upstream_status", "proxy_host");
    private final StatsKey statsKey;

    public AccessLogRecord(Long slbId, String value) {
        this.slbId = slbId;
        this.statsKey = parse(value);
    }

    public StatsKey getStatsKey() {
        return statsKey;
    }

    public long getResponseSize() {
        return 0L;
    }

    public long getCost() {
        return Double.valueOf(statsKey.getTags().get(costKey)).longValue();
    }

    public String getStatus() {
        return statsKey.getTags().get(statusKey);
    }

    private StatsKey parse(String value) {
        StatsKey result = new StatsKey("slb.request")
                .addTag("slbId", slbId.toString())
                .reportCount(false)
                .reportStatus(false)
                .reportRequestSize(false)
                .reportResponseSize(false);
        Stack<String> grammarChecker = new Stack<>();
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '{':
                    grammarChecker.push("{");
                    break;
                case '"':
                    if (grammarChecker.peek().equals("\"")) {
                        grammarChecker.pop();
                        String tmp = sb.toString();
                        grammarChecker.push(tmp);
                        sb.setLength(0);
                    } else {
                        grammarChecker.push("\"");
                    }
                    break;
                case ',':
                case '}':
                    if (grammarChecker.peek().equals("\"")) {
                        sb.append(c);
                    } else {
                        String v = grammarChecker.pop();
                        String k = grammarChecker.pop();
                        if (keys.contains(k)) {
                            if (k.equals(costKey) || k.equals(upCostKey)) {
                                if (v.equals("-"))
                                    result.addTag(k, "0");
                                else
                                    result.addTag(k, Double.valueOf(Double.parseDouble(v) * 1000.0).toString());
                            } else {
                                result.addTag(k, v);
                            }
                        }
                    }
                    break;
                case ':':
                    if (grammarChecker.peek().equals("\"")) {
                        sb.append(c);
                    }
                    break;
                default:
                    sb.append(c);
            }
        }
        grammarChecker.pop();
        if (!grammarChecker.isEmpty())
            return null;
        return result;
    }
}
