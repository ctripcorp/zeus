package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.StateMachineContext;

import java.util.LinkedList;

/**
 * Created by zhoumy on 2016/6/8.
 */
public class VariableContext implements StateMachineContext<String> {
    private StateMachineContext parent;
    private LinkedList<String> parsedValue;
    private ContextState state;

    public VariableContext(StateMachineContext parent) {
        this.parent = parent;
        this.parsedValue = new LinkedList<>();
    }

    @Override
    public void proceed(int length) {
        parent.proceed(length);
    }

    @Override
    public char[] delay(int length) {
        return parent.delay(length);
    }

    @Override
    public char[] getSource() {
        return parent.getSource();
    }

    @Override
    public int getCurrentIndex() {
        return parent.getCurrentIndex();
    }

    @Override
    public boolean shouldProceed() {
        return parent.shouldProceed();
    }

    @Override
    public void addResult(String key, String value) {
        parsedValue.addLast(value);
    }

    @Override
    public String peekLastParsedValue() {
        return parsedValue.getLast();
    }

    @Override
    public LinkedList<String> getResult() {
        return parsedValue;
    }

    @Override
    public ContextState getState() {
        return state;
    }

    @Override
    public void setState(ContextState state) {
        this.state = state;
    }
}
