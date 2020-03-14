package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class DefaultTransition implements Transition {
    private int proceedLength = 1;
    @Override
    public LogStatsState transit(LogStatsState state, StateMachineContext ctxt) {
        if (ctxt.shouldProceed()) {
            ctxt.proceed(proceedLength);
            return state.getNext();
        }
        return null;
    }

    public int getProceedLength() {
        return proceedLength;
    }

    public DefaultTransition setProceedLength(int proceedLength) {
        this.proceedLength = proceedLength;
        return this;
    }
}