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
        for (int i = 0; i < formatChars.length; i++) {
            if (formatChars[i] == '$') {
                int start = i;
                // word characters
                sb.append("(\\w+)");
                while (Character.isLetter(formatChars[++i])) {
                }
                keyList.add(i - start == 0 ? "key" + i : new String(formatChars, start, i - start));
            }
            // whitespace or predefined
            sb.append(formatChars[i] == ' ' ? "\\s" : formatChars[i]);
        }
        keys = keyList.toArray(new String[keyList.size()]);
        patternString = sb.toString();
        pattern = Pattern.compile(patternString);
    }
}
