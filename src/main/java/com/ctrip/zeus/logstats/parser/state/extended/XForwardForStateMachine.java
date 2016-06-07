package com.ctrip.zeus.logstats.parser.state.extended;

import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;
import com.ctrip.zeus.logstats.parser.state.Transition;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class XForwardForStateMachine implements LogStatsStateMachine {

    private final LogStatsState ipState;
    private final Transition transition;

    public XForwardForStateMachine(LogStatsState xforwardState) {
        this.ipState = xforwardState;
        this.transition = new XForwardTransition();
    }

    @Override
    public LogStatsState getStartState() {
        return ipState;
    }

    @Override
    public void transduce(StateMachineContext ctxt) {
        LogStatsState current = ipState;
        current.getAction().execute(ctxt);
        while ((current = transition.transit(ipState, ctxt)) != null) {
            current.getAction().execute(ctxt);
        }
    }

    private class XForwardTransition implements Transition {

        @Override
        public LogStatsState transit(LogStatsState state, StateMachineContext ctxt) {
            if (ipState.getName().equals(state.getName())) {
                String v = ctxt.getLastParsedValue();
                if (!"-".equals(v) && !"".equals(v)) {
                    char[] paralSplitter = ctxt.delay(2);
                    if (paralSplitter[0] == ',' && paralSplitter[1] == ' ') {
                        ctxt.proceed(2);
                        return ipState;
                    } else {
                        return null;
                    }
                }
            }
            return null;
        }
    }
}
