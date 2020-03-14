package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.Action;
import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;

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
        public void execute(StateMachineContext ctxt, String separator) {
            StringBuilder sb = new StringBuilder();
            if (separator == null){
                separator = " ";
            }
            char[] schars = separator.toCharArray();
            char[] source = ctxt.getSource();
            char c;
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];

                if (c == schars[0] && i + schars.length <= source.length){
                    boolean isEnd = true;
                    for (int j = 1 ; j < schars.length ; j++){
                        if (schars[j] != source[i+j]){
                            isEnd =false;
                            break;
                        }
                    }
                    if (isEnd){
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(name, sb.toString());
                        return;
                    }
                }
                sb.append(c);
            }
        }

        @Override
        public void execute(StateMachineContext ctxt) {
            execute(ctxt," ");
        }
    }
}
