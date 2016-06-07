package com.ctrip.zeus.logstats.parser.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class AccessLogStateMachine implements LogStatsStateMachine {
    private LogStatsState defaultState = new StringState();
    private final LogStatsState initState = new WrappedStringState();
    private LogStatsState currentState;

    private Map<Integer, LogStatsState> stateInterceptor = new HashMap<>();
    private Map<String, LogStatsState> stateRegitry = new HashMap<>();

    public AccessLogStateMachine() {
        currentState = initState;
        stateRegitry.put("stringState", defaultState);
    }

    @Override
    public LogStatsState getStartState() {
        return initState;
    }

    @Override
    public LogStatsState getNextState(LogStatsState current, StateContext ctxt) {
        LogStatsState next = stateInterceptor.get(ctxt.getStateHistoryCount());
        if (next == null) {
            return stateRegitry.get("stringState");
        } else {
            return next;
        }
    }

    @Override
    public void transduce(StateContext ctxt) {
        if (currentState.shouldDeplay()) {
            currentState.getSubMachine().transduce(ctxt);
        } else {
            currentState.getTranstition().execute(ctxt);
        }
        ctxt.proceed(1);
        while (ctxt.shouldProceed()) {
            currentState = getNextState(currentState, ctxt);
            if (currentState.shouldDeplay()) {
                currentState.getSubMachine().transduce(ctxt);
            } else {
                currentState.getTranstition().execute(ctxt);
                ctxt.proceed(1);
            }
        }
    }

    @Override
    public void registerState(int idx, LogStatsState state) {
        stateInterceptor.put(idx, state);
    }
}
