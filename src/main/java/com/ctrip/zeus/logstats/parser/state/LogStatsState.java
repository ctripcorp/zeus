package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public interface LogStatsState {

    String getName();

    LogStatsStateMachine getSubMachine();

    Action getAction();

    void setNext(LogStatsState next);

    LogStatsState getNext();

    boolean runSubMachine();
}
