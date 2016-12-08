package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.state.*;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class UpstreamState implements LogStatsState {
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
        private final LogStatsStateMachine innerStateMachine = new XForwardForStateMachine(new ContactComponentState(name), new String[]{""});

        @Override
        public void execute(StateMachineContext ctxt) {
            innerStateMachine.transduce(ctxt);
            String peek = ctxt.peekLastParsedValue();
            if (!"".equals(peek)) {
                char[] paralSplitter = ctxt.delay(3);
                if (paralSplitter[0] == ' ' && paralSplitter[1] == ':' && paralSplitter[2] == ' ') {
                    ctxt.proceed(3);
                    innerStateMachine.transduce(ctxt);
                    KeyValue second = (KeyValue) ctxt.getResult().pollLast();
                    KeyValue first = (KeyValue) ctxt.getResult().pollLast();
                    ctxt.addResult(name, first.getValue() + " : " + second.getValue());
                }
            }
        }
    }

    protected class ContactComponentState implements LogStatsState {
        private final String name;
        private final Action action;

        private LogStatsState next;

        public ContactComponentState(String name) {
            this.name = name;
            this.action = new ContactComponentAction();
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
    }

    protected class ContactComponentAction implements Action {
        @Override
        public void execute(StateMachineContext ctxt) {
            StringBuilder sb = new StringBuilder();
            char c;
            char[] source = ctxt.getSource();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];
                switch (c) {
                    case ' ':
                    case ',': {
                        String v = sb.toString();
                        v = v.isEmpty() ? "-" : v;
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(name, v);
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
