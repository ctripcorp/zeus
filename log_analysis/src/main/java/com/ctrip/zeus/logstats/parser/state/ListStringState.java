package com.ctrip.zeus.logstats.parser.state;

import javax.ws.rs.NotSupportedException;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class ListStringState implements LogStatsState {
    private final String name;
    private final String subSeparator;
    private final Action action;

    private LogStatsState next;

    public ListStringState(String name, String subSeparator) {
        this.name = name;
        this.action = new StringAction();
        if (subSeparator == null || subSeparator.isEmpty()){
            throw new NotSupportedException("SubSeparator Cant Be Null Or Empty.");
        }
        this.subSeparator = subSeparator;
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
            execute(ctxt, " ");
        }

        @Override
        public void execute(StateMachineContext ctxt, String separator) {
            StringBuilder sb = new StringBuilder();

            if (separator == null) {
                separator = " ";
            }
            char[] schars = separator.toCharArray();
            char[] source = ctxt.getSource();
            char[] subChars = subSeparator.toCharArray();
            char c;
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];
                //Check For Sub Separator
                if (c == subChars[0] && i + subChars.length <= source.length) {
                    boolean isEnd = true;
                    for (int j = 1; j < subChars.length; j++) {
                        if (subChars[j] != source[i + j]) {
                            isEnd = false;
                            break;
                        }
                    }
                    if (isEnd) {
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(name, sb.toString());
                        return;
                    }
                }
                //Check For Separator
                if (c == schars[0] && i + schars.length <= source.length) {
                    boolean isEnd = true;
                    for (int j = 1; j < schars.length; j++) {
                        if (schars[j] != source[i + j]) {
                            isEnd = false;
                            break;
                        }
                    }
                    if (isEnd) {
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(name, sb.toString());
                        return;
                    }
                }
                sb.append(c);
            }
            ctxt.proceed(source.length - ctxt.getCurrentIndex());
            ctxt.addResult(name, sb.toString());
        }
    }
}
