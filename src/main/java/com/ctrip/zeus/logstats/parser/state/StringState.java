package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class StringState implements LogStatsState {
    private final String name;
    private final Action action;

    private LogStatsState next;

    public StringState(String name) {
        this.name = name;
        this.action = new StringAction();
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

    private class StringAction implements Action {

        @Override
        public void execute(StateMachineContext ctxt) {
            StringBuilder sb = new StringBuilder();
            char c;
            char[] source = ctxt.getSource();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];
                switch (c) {
                    case ' ': {
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(name, sb.toString());
                        return;
                    }
                    default:
                        sb.append(c);
                }
            }
            ctxt.proceed(source.length - ctxt.getCurrentIndex());
            ctxt.addResult(name, sb.toString());
        }
    }
}
