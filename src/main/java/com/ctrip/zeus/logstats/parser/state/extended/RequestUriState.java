package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateContext;
import com.ctrip.zeus.logstats.parser.state.Transition;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class RequestUriState implements LogStatsState<String> {
    private Transition transition = new RequestUriTransition();

    @Override
    public String getOutput(StateContext ctxt) {
        return null;
    }

    @Override
    public boolean shouldDeplay() {
        return false;
    }

    @Override
    public List<Transition> getDelayedTransition() {
        return null;
    }

    @Override
    public LogStatsStateMachine getSubMachine() {
        return null;
    }

    @Override
    public Transition getTranstition() {
        return transition;
    }

    private class RequestUriTransition implements Transition {

        @Override
        public void execute(StateContext ctxt) {
            char[] source = ctxt.getSource();
            boolean _ignore = false;
            StringBuilder sb = new StringBuilder();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                char c = source[i];
                switch (c) {
                    case '?': {
                        _ignore = true;
                    }
                    case ' ': {
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        System.out.println(sb.toString());
                        ctxt.addResult(sb.toString());
                    }
                    default:
                        if (!_ignore) {
                            sb.append(c);
                        }
                }
            }
        }
    }
}
