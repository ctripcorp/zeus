package com.ctrip.zeus.logstats.parser;

import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.LineFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhoumy on 2015/11/13.
 */
public class AccessLogRegexParser implements LogParser {
    private LineFormat<Pattern, String> mainLogFormat;
    private Pattern pattern;

    public AccessLogRegexParser(List<LineFormat> formats) {
        if (formats.size() >= 1) {
            mainLogFormat = formats.get(0);
            pattern = mainLogFormat.getEngine();
        }
    }

    @Override
    public List<KeyValue> parse(String line) {
        List<KeyValue> kvs = new ArrayList<>();
        Matcher matcher;
        if ((matcher = pattern.matcher(line)).matches()) {
            String[] keys = mainLogFormat.getKeys();
            for (int i = 0; i < keys.length; i++) {
                kvs.add(new KeyValue(keys[i], matcher.group(i + 1)));
            }
        }
        return kvs;
    }

    @Override
    public String parseToJsonString(String line) {
        JsonStringWriter writer = new JsonStringWriter();
        writer.start();
        Matcher matcher;
        if ((matcher = pattern.matcher(line)).matches()) {
            String[] keys = mainLogFormat.getKeys();
            for (int i = 0; i < keys.length; i++) {
                writer.writeNode(keys[i], matcher.group(i + 1));
            }
        }
        writer.end();
        return writer.get();
    }
}
