package com.ctrip.zeus.logstats.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by zhoumy on 2015/11/16.
 */
public class AccessLogRegexFormat implements LineFormat<Pattern, String> {
    private String format;
    private String patternString;
    private Pattern pattern;
    private String[] keys;
    private final Map<String, String> patternRegistry = new HashMap<>();

    public AccessLogRegexFormat() {
    }

    public AccessLogRegexFormat(String format) {
        setFormat(format);
        registerComponentForKey("http_x_forwarded_for", "(-|(?:[0-9.]+(?:, [0-9.]+)*))");
        registerComponentForKey("request_time", "(-|\\d+\\.\\d+)");
        registerComponentForKey("request_uri", "([^?]*)(?:.*)");
        registerComponentForKey("upstream_response_time", "((?:-|\\d+\\.\\d+)(?: : (?:-|\\d+\\.\\d+))?)");
        registerComponentForKey("upstream_addr", "((?:-|\\S+)(?: : (?:-|\\S+))?)");
        registerComponentForKey("upstream_status", "((?:-|\\d{3})(?: : (?:-|\\d{3}))?)");
    }

    @Override
    public String getFormat() {
        return format;
    }

    public String getPatternString() {
        return patternString;
    }

    @Override
    public Pattern getEngine() {
        return pattern;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public LineFormat setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public LineFormat registerComponentForKey(String key, String component) {
        patternRegistry.put(key, component);
        return this;
    }

    @Override
    public LineFormat generate() {
        parsePattern();
        return this;
    }

    protected void parsePattern() {
        List<String> keyList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char[] formatChars = format.toCharArray();
        int depth = 0;
        for (int i = 0; i < formatChars.length; i++) {
            if (formatChars[i] == '$') {
                int start = i;
                // word characters
                while (++i < formatChars.length && (Character.isLetterOrDigit(formatChars[i]) || formatChars[i] == '_')) {
                }
                String k = i - start == 0 ? "key" + i : new String(formatChars, start + 1, i - start - 1);
                keyList.add(k);
                if (patternRegistry.containsKey(k))
                    sb.append(patternRegistry.get(k));
                else
                    sb.append(depth % 2 == 0 ? "(\\S+)" : "(.*)");
            }
            if (i >= formatChars.length)
                break;
            // whitespace or predefined
            switch (formatChars[i]) {
                case ' ':
                    sb.append("\\s+");
                    break;
                case '"':
                    depth = depth % 2 == 0 ? depth + 1 : depth - 1;
                    sb.append("\\\"");
                    break;
                case '\'':
                    depth = depth % 2 == 0 ? depth + 1 : depth - 1;
                    sb.append("\\'");
                    break;
                case '[':
                    depth++;
                    sb.append("\\[");
                    break;
                case ']':
                    depth--;
                    sb.append("\\]");
                    break;
                default:
                    sb.append(formatChars[i]);
                    break;
            }
        }
        keys = keyList.toArray(new String[keyList.size()]);
        patternString = sb.toString();
        pattern = Pattern.compile(patternString);
    }
}
