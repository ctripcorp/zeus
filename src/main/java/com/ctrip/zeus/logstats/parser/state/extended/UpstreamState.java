package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.*;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class UpstreamState implements LogStatsState<String> {
    private final String name;
    private final Action action;

    private LogStatsState next;

    public UpstreamState(String name) {
        this.name = name;
        this.action = new UpstreamAction();
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

    private class UpstreamAction implements Action {
        private StringState innerState = new StringState("upstreamState");

        @Override
        public void execute(StateMachineContext ctxt) {
            innerState.getAction().execute(ctxt);
            String v = ctxt.getLastParsedValue();
            if (!"".equals(v)) {
                char[] paralSplitter = ctxt.delay(3);
                if (paralSplitter[0] == ' ' && paralSplitter[1] == ':' && paralSplitter[2] == ' ') {
                    ctxt.proceed(3);
                    innerState.getAction().execute(ctxt);
                }
            }
        }
    }

}
