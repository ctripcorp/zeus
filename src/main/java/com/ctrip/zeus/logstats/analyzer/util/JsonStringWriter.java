package com.ctrip.zeus.logstats.analyzer.util;

import com.ctrip.zeus.logstats.parser.KeyValue;

import java.util.List;

/**
 * Created by mengyizhou on 10/18/15.
 */
public class JsonStringWriter {

    public static String write(List<KeyValue> keyValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (KeyValue keyValue : keyValues) {
            sb.append("\"" + keyValue.getKey() + "\"")
                    .append(" : ")
                    .append("\"" + keyValue.getValue() + "\"")
                    .append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}
