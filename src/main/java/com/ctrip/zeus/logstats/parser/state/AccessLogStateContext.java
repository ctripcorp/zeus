package com.ctrip.zeus.logstats.parser.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class AccessLogStateContext implements StateContext<String> {
    private String value;
    private char[] valueArray;
    private int idx;
    private List<String> parsedValues = new ArrayList<>();

    @Override
    public void setSourceValue(String value) {
        this.value = value;
        valueArray = this.value.toCharArray();
    }

    @Override
    public int getCurrentIndex() {
        return idx;
    }

    @Override
    public int getStateHistoryCount() {
        return parsedValues.size();
    }

    @Override
    public void proceed(int length) {
        idx += length;
    }

    @Override
    public char[] delay(int length) {
        char[] result = new char[length];
        for (int i = 0; i < length && idx + i < valueArray.length; i++) {
            result[i] = valueArray[idx + i];
        }
        return result;
    }

    @Override
    public char[] getSource() {
        return valueArray;
    }

    @Override
    public void addResult(String result) {
        parsedValues.add(result);
    }

    @Override
    public String getLastStateValue() {
        return parsedValues.get(parsedValues.size() - 1);
    }

    @Override
    public List<String> getParsedValue() {
        return parsedValues;
    }

    @Override
    public boolean shouldProceed() {
        return idx < value.length() - 1;
    }
}
