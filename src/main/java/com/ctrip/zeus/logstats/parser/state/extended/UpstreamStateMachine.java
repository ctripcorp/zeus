package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateContext;
import com.ctrip.zeus.logstats.parser.state.StringState;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class UpstreamStateMachine implements LogStatsStateMachine {
    private LogStatsState stringState = new StringState();

    @Override
    public LogStatsState getStartState() {
        return stringState;
    }

    @Override
    public LogStatsState getNextState(LogStatsState current, StateContext ctxt) {
        if (current == stringState) {
            String v = ctxt.getLastStateValue();
            if (!"".equals(v)) {
                char[] transitId = ctxt.delay(3);
                if (transitId[0] == ' ' && transitId[1] == ':' && transitId[2] == ' ') {
                    ctxt.proceed(3);
                    return stringState;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public void transduce(StateContext ctxt) {
        stringState.getTranstition().execute(ctxt);
        while (ctxt.shouldProceed()) {
            LogStatsState next = getNextState(stringState, ctxt);
            if (next == null)
                return;
            next.getTranstition().execute(ctxt);
        }
    }

    @Override
    public void registerState(int idx, LogStatsState state) {

    }
}
