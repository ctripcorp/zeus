package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class DefaultTransition implements Transition {
    @Override
    public LogStatsState transit(LogStatsState state, StateMachineContext ctxt) {
        if (ctxt.shouldProceed()) {
            ctxt.proceed(1);
            return state.getNext();
        }
        return null;
    }
}