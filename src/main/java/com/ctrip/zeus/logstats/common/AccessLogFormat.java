package com.ctrip.zeus.logstats.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class AccessLogFormat implements LineFormat {
    private String format;
    private final Map<String, String> patternRegistry = new HashMap<>();

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public String getPatternString() {
        return null;
    }

    @Override
    public Pattern getPattern() {
        return null;
    }

    @Override
    public String[] getKeys() {
        return new String[0];
    }

    @Override
    public LineFormat setFormat(String format) {
        return null;
    }

    @Override
    public LineFormat registerPatternForKey(String key, String pattern) {
        return null;
    }

    @Override
    public LineFormat generate() {
        return null;
    }

}
