package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.state.Action;
import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class UpstreamState implements LogStatsState {
    private final String name;
    private final Action action;

    private LogStatsState next;

    public UpstreamState(String name, String subSeparator) {
        this.name = name;
        this.action = new UpstreamAction(subSeparator);
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
        private final LogStatsStateMachine innerStateMachine;

        UpstreamAction(String subSeparator) {
            innerStateMachine = new XForwardForStateMachine(new ContactComponentState(name), subSeparator, new String[]{""});
        }

        @Override
        public void execute(StateMachineContext ctxt, String separator) {
            execute(ctxt);
        }

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
        public void execute(StateMachineContext ctxt, String separator) {
            StringBuilder sb = new StringBuilder();
            char c;
            char[] source = ctxt.getSource();
            if (separator == null) {
                separator = " ";
            }
            char[] schars = separator.toCharArray();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];
                if (c == schars[0] && i + schars.length <= source.length) {
                    boolean isEnd = true;
                    for (int j = 1; j < schars.length; j++) {
                        if (source[i + j] != schars[j]) {
                            isEnd = false;
                            break;
                        }
                    }
                    if (isEnd) {
                        String v = sb.toString();
                        v = v.isEmpty() ? "-" : v;
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(name, v);
                        return;
                    }
                }
                switch (c) {
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

        @Override
        public void execute(StateMachineContext ctxt) {
            execute(ctxt, " ");
        }
    }

}
