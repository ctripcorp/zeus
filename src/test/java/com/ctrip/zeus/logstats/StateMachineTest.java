package com.ctrip.zeus.logstats;

import com.ctrip.zeus.logstats.parser.state.*;
import com.ctrip.zeus.logstats.parser.state.extended.RequestUriState;
import com.ctrip.zeus.logstats.parser.state.extended.UpstreamStateMachine;
import com.ctrip.zeus.logstats.parser.state.extended.XForwardForStateMachine;
import org.junit.Test;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class StateMachineTest {
    @Test
    public void testWrappedStringState() {
        StateContext context = new AccessLogStateContext();
        context.setSourceValue("[01/Jun/2016:09:00:15 +0800] ws.mip.hotel.ctripcorp.com svr5153hw1288 10.8.208.7 POST ");

        LogStatsStateMachine accessLogStateMachine = new AccessLogStateMachine();
        accessLogStateMachine.transduce(context);
    }

    @Test
    public void testXForwardForState() {
        StateContext context = new AccessLogStateContext();
        context.setSourceValue("10.32.65.134, 10.15.202.207, 10.15.202.207, 10.15.202.207, 10.15.202.207, 10.15.202.207");
        LogStatsStateMachine xForwardForStateMachine = new XForwardForStateMachine();
        xForwardForStateMachine.transduce(context);

        context = new AccessLogStateContext();
        context.setSourceValue("10.32.65.134");
        xForwardForStateMachine.transduce(context);

        context = new AccessLogStateContext();
        context.setSourceValue("-");
        xForwardForStateMachine.transduce(context);
    }

    @Test
    public void testRequestUriState() {
        StateContext context = new AccessLogStateContext();
        context.setSourceValue("[dummy] /app/index.php?param=/api/home&method=config.getAppConfig&_fxpcqlniredt=09031130410105805720");
        LogStatsStateMachine accessLogStateMachine = new AccessLogStateMachine();
        accessLogStateMachine.registerState(1, new RequestUriState());
        accessLogStateMachine.transduce(context);
    }

    @Test
    public void testUpstreamState() {
        StateContext context = new AccessLogStateContext();
        context.setSourceValue("- : 0.006");
        LogStatsStateMachine upstreamStateMachine = new UpstreamStateMachine();
        upstreamStateMachine.transduce(context);
    }
}
