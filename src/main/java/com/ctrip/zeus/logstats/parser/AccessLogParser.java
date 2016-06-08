package com.ctrip.zeus.logstats.parser;

import com.ctrip.zeus.logstats.common.AccessLogFormat;
import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.parser.state.AccessLogContext;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class AccessLogParser implements LogParser {
    private AccessLogFormat accessLogFormat;
    private LogStatsStateMachine stateMachine;

    public AccessLogParser(AccessLogFormat accessLogFormat) {
        this.accessLogFormat = accessLogFormat;
        this.stateMachine = accessLogFormat.getStateMachine();
    }

    @Override
    public List<KeyValue> parse(String line) {
        StateMachineContext context = new AccessLogContext();
        context.setSourceValue(line);
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
