package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public interface LogStatsStateMachine {
    LogStatsState getStartState();

    LogStatsState getNextState(LogStatsState current, StateContext ctxt);

    void transduce(StateContext ctxt);

    void registerState(int idx, LogStatsState state);
}
