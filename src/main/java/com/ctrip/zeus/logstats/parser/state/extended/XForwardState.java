package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.*;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class XForwardState implements LogStatsState {
    private final String name;
    private final LogStatsStateMachine subMachine;
    private LogStatsState next;

    public XForwardState(String name) {
        this.name = name;
        this.subMachine = new XForwardForStateMachine(new IpState(name));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LogStatsStateMachine getSubMachine() {
        return subMachine;
    }

    @Override
    public Action getAction() {
        return null;
    }

    @Override
    public void setNext(LogStatsState next) {
        this.next = next;
    }

    @Override
    public LogStatsState getNext() {
        return next;
    }

    @Override
    public boolean runSubMachine() {
        return true;
    }
}
