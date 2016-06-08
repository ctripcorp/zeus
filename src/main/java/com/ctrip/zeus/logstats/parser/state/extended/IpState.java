package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;
import com.ctrip.zeus.logstats.parser.state.Action;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class IpState implements LogStatsState {
    private final String name;
    private final Action action;
    private LogStatsState next;

    public IpState(String name) {
        this.name = name;
        this.action = new IpAction();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LogStatsStateMachine getSubMachine() {
        return null;
    }

    @Override
    public Action getAction() {
        return action;
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
        return false;
    }

    private class IpAction implements Action {

        @Override
        public void execute(StateMachineContext ctxt) {
            StringBuilder sb = new StringBuilder();
            char[] source = ctxt.getSource();
            int idx = ctxt.getCurrentIndex();
            for (; idx < source.length; idx++) {
                char c = source[idx];
                if (('0' <= c && c <= '9') || c == '.' || c == '-') {
                    sb.append(c);
                } else {
                    idx--;
                    break;
                }
            }
            ctxt.addResult(name, sb.toString());
            ctxt.proceed(idx - ctxt.getCurrentIndex() + 1);
        }
    }
}
