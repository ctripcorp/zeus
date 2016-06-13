package com.ctrip.zeus.logstats.parser;

import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.state.AccessLogContext;
import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class AccessLogStateMachineParser implements LogParser {
    private LineFormat<LogStatsStateMachine, LogStatsState> mainLineFormat;
    private LogStatsStateMachine stateMachine;

    public AccessLogStateMachineParser(LineFormat lineFormat) {
        mainLineFormat = lineFormat;
        stateMachine = mainLineFormat.getEngine();
    }

    @Override
    public List<KeyValue> parse(String line) {
        StateMachineContext context = new AccessLogContext(line);
        stateMachine.transduce(context);
        return context.getResult();
    }

    @Override
    public String parseToJsonString(String line) {
        JsonStringWriter writer = new JsonStringWriter();
        writer.start();
        for (KeyValue kv : parse(line)) {
            writer.writeNode(kv.getKey(), kv.getValue());
        }
        writer.end();
        return writer.get();
    }
}
