package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateContext;
import com.ctrip.zeus.logstats.parser.state.Transition;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class XForwardForState implements LogStatsState {
    private Transition transition = new XForwardForTransition();

    @Override
    public Object getOutput(StateContext ctxt) {
        return null;
    }

    @Override
    public boolean shouldDeplay() {
        return true;
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

    private class XForwardForTransition implements Transition {

        @Override
        public void execute(StateContext ctxt) {
            StringBuilder sb = new StringBuilder();
            char[] source = ctxt.getSource();
            int idx = ctxt.getCurrentIndex();
            for (; idx < source.length; idx++) {
                char c = source[idx];
                if (('0' <= c && c <= '9' ) || c == '.' || c == '-') {
                    sb.append(c);
                } else {
                    idx--;
                    break;
                }
            }
            ctxt.addResult(sb.toString());
            System.out.println(sb.toString());
            ctxt.proceed(idx - ctxt.getCurrentIndex() + 1);
        }
    }
}
