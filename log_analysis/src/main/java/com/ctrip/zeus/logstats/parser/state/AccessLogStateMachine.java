package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class AccessLogStateMachine implements LogStatsStateMachine {
    private final LogStatsState initState;
    private final DefaultTransition transition = new DefaultTransition();
    private String separator;

    public AccessLogStateMachine(LogStatsState initState) {
        this.initState = initState;
    }
    public AccessLogStateMachine(LogStatsState initState,String separator) {
        this.initState = initState;
        this.separator = separator;
        this.transition.setProceedLength(separator.length());
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
            current.getAction().execute(ctxt,separator);
            while ((current = transition.transit(current, ctxt)) != null) {
                if (current.runSubMachine()) {
                    current.getSubMachine().transduce(ctxt);
                } else {
                    current.getAction().execute(ctxt,separator);
                }
            }
            if (ctxt.shouldProceed()) {
                ctxt.setState(StateMachineContext.ContextState.FAILURE);
            } else if (!ctxt.getState().equals(StateMachineContext.ContextState.FAILURE)) {
                ctxt.setState(StateMachineContext.ContextState.SUCCESS);
            }
        } catch (Exception ex) {
            ctxt.setState(StateMachineContext.ContextState.FAILURE);
        }
    }
}