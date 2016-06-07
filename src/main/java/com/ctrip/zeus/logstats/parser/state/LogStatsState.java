package com.ctrip.zeus.logstats.parser.state;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public interface LogStatsState<V> {

    V getOutput(StateContext ctxt);

    boolean shouldDeplay();

    List<Transition> getDelayedTransition();

    LogStatsStateMachine getSubMachine();

    Transition getTranstition();
}
