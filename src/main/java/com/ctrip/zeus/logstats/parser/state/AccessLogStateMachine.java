package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class AccessLogStateMachine implements LogStatsStateMachine {
    private final LogStatsState initState;
    private final Transition transition = new DefaultTransition();

    public AccessLogStateMachine(LogStatsState initState) {
        this.initState = initState;
    }

    @Override
    public LogStatsState getStartState() {
        return initState;
    }

    @Override
    public void transduce(StateMachineContext ctxt) {
        LogStatsState current = initState;
        ctxt.setState(StateMachineContext.ContextState.PROCESSING);
        try {
            current.getAction().execute(ctxt);
            while ((current = transition.transit(current, ctxt)) != null) {
                if (current.runSubMachine()) {
                    current.getSubMachine().transduce(ctxt);
                } else {
                    current.getAction().execute(ctxt);
                }
            }
            if (ctxt.shouldProceed()) {
                ctxt.setState(StateMachineContext.ContextState.FAILURE);
            } else {
                ctxt.setState(StateMachineContext.ContextState.SUCCESS);
            }
        } catch (Exception ex) {
            ctxt.setState(StateMachineContext.ContextState.FAILURE);
        }
    }
}