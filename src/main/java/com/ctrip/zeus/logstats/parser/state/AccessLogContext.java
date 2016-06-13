package com.ctrip.zeus.logstats.parser.state;

import com.ctrip.zeus.logstats.parser.KeyValue;

import java.util.LinkedList;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class AccessLogContext implements StateMachineContext<KeyValue> {
    private final String value;
    private final char[] valueArray;
    private int idx;
    private ContextState state;
    private LinkedList<KeyValue> parsedValues = new LinkedList<>();

    public AccessLogContext(String value) {
        this.value = value;
        this.valueArray = this.value.toCharArray();
    }

    @Override
    public int getCurrentIndex() {
        return idx;
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
    public String peekLastParsedValue() {
        KeyValue last = parsedValues.getLast();
        return last.getValue();
    }

    @Override
    public LinkedList<KeyValue> getResult() {
        if (state == ContextState.FAILURE) return new LinkedList<>();
        return parsedValues;
    }

    @Override
    public ContextState getState() {
        return state;
    }

    @Override
    public void setState(ContextState state) {
        this.state = state;
    }

    @Override
    public boolean shouldProceed() {
        return state == ContextState.PROCESSING && idx < value.length() - 1;
    }

    @Override
    public void addResult(String key, String value) {
        parsedValues.addLast(new KeyValue(key, value));
    }
}
