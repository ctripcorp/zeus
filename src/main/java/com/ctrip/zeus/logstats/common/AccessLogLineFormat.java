package com.ctrip.zeus.logstats.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by zhoumy on 2015/11/16.
 */
public class AccessLogLineFormat implements LineFormat {
    private String format;
    private String patternString;
    private Pattern pattern;
    private String[] keys;

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String getPatternString() {
        return patternString;
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
        parsePattern(format);
    }

    protected void parsePattern(String format) {
        List<String> keyList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char[] formatChars = format.toCharArray();
        int depth = 0;
        for (int i = 0; i < formatChars.length; i++) {
            if (formatChars[i] == '$') {
                int start = i;
                // word characters
                sb.append(depth % 2 == 0 ? "(\\S+)" : "(.+)");
                while (++i < formatChars.length && (Character.isLetterOrDigit(formatChars[i]) || formatChars[i] == '_')) {
                }
                keyList.add(i - start == 0 ? "key" + i : new String(formatChars, start + 1, i - start - 1));
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
