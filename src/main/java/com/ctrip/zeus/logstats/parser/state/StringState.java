package com.ctrip.zeus.logstats.parser.state;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class StringState implements LogStatsState<String> {
    private Transition transition = new StringTransition();

    @Override
    public String getOutput(StateContext ctxt) {
        transition.execute(ctxt);
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

    private class StringTransition implements Transition {

        @Override
        public void execute(StateContext ctxt) {
            StringBuilder sb = new StringBuilder();
            char c;
            char[] source = ctxt.getSource();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];
                switch (c) {
                    case ' ': {
                        ctxt.proceed(i - ctxt.getCurrentIndex());
                        ctxt.addResult(sb.toString());
                        System.out.println(sb.toString());
                        return;
                    }
                    default:
                        sb.append(c);
                }
            }
            ctxt.proceed(source.length - ctxt.getCurrentIndex());
            ctxt.addResult(sb.toString());
            System.out.println(sb.toString());
        }
    }
}
