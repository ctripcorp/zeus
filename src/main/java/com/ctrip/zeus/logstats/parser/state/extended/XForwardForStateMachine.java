package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateContext;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class XForwardForStateMachine implements LogStatsStateMachine {

    private LogStatsState xforwardState = new XForwardForState();

    @Override
    public LogStatsState getStartState() {
        return xforwardState;
    }

    @Override
    public LogStatsState getNextState(LogStatsState current, StateContext ctxt) {
        if (current == xforwardState) {
            String v = ctxt.getLastStateValue();
            if (!"-".equals(v) && !"".equals(v)) {
                char[] transitId = ctxt.delay(2);
                if (transitId[0] == ',' && transitId[1] == ' ') {
                    ctxt.proceed(2);
                    return xforwardState;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public void transduce(StateContext ctxt) {
        xforwardState.getTranstition().execute(ctxt);
        while (ctxt.shouldProceed()) {
            LogStatsState next = getNextState(xforwardState, ctxt);
            if (next == null)
                return;
            next.getTranstition().execute(ctxt);
        }
    }

    @Override
    public void registerState(int idx, LogStatsState state) {

    }
}
