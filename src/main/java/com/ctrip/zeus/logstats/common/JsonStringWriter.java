package com.ctrip.zeus.logstats.common;

import com.ctrip.zeus.logstats.parser.KeyValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class JsonStringWriter {
    private final StringBuilder sb;

    public JsonStringWriter() {
        sb = new StringBuilder();
    }

    public void start() {
        sb.append("{");
    }

    public void writeNode(String key, String value) {
        sb.append("\"" + key + "\"")
                .append(":")
                .append("\"" + value + "\"")
                .append(",");
    }

    public void end() {
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
    }

    public String get() {
        return sb.toString();
    }

    public String write(List<KeyValue> keyValues) {
        Set<String> key = new HashSet<>();
        sb.append("{");
        for (KeyValue keyValue : keyValues) {
            if (key.add(keyValue.getKey())) {
                sb.append("\"" + keyValue.getKey() + "\"")
                        .append(":")
                        .append("\"" + keyValue.getValue() + "\"")
                        .append(",");
            }
        }
        if (keyValues.size() > 0)
            sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}
