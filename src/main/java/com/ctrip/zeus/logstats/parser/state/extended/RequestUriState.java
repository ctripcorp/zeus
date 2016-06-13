package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;
import com.ctrip.zeus.logstats.parser.state.Action;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class RequestUriState implements LogStatsState {
    private final String name;
    private final Action action;

    private LogStatsState next;

    public RequestUriState(String name) {
        this.name = name;
        this.action = new RequestUriAction();
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

    private class RequestUriAction implements Action {

        @Override
        public void execute(StateMachineContext ctxt) {
            char[] source = ctxt.getSource();
            boolean _ignore = false;
            StringBuilder sb = new StringBuilder();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                char c = source[i];
                switch (c) {
                    case ' ': {
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(name, sb.toString());
                        return;
                    }
                    case '?': {
                        _ignore = true;
                    }
                    default: {
                        if (!_ignore) {
                            sb.append(c);
                        }
                    }
                }
            }
        }
    }
}
