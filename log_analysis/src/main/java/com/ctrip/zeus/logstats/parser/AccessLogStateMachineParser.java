package com.ctrip.zeus.logstats.parser;

import com.ctrip.zeus.logstats.common.JsonStringWriter;
import com.ctrip.zeus.logstats.common.LineFormat;
import com.ctrip.zeus.logstats.parser.state.AccessLogContext;
import com.ctrip.zeus.logstats.parser.state.LogStatsState;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;
import com.ctrip.zeus.logstats.parser.state.StateMachineContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class AccessLogStateMachineParser implements LogParser {
    private LineFormat<LogStatsStateMachine, LogStatsState> mainLineFormat;
    private LogStatsStateMachine stateMachine;

    private Map<String, LineFormat<LogStatsStateMachine, LogStatsState>> specialLineFormat = new LinkedHashMap<>();
    private Map<String, LogStatsStateMachine> specialStateMachine = new LinkedHashMap<>();

    public AccessLogStateMachineParser(LineFormat lineFormat, List<LineFormat> lineFormats) {
        mainLineFormat = lineFormat;
        stateMachine = mainLineFormat.getEngine();

        for (LineFormat line : lineFormats) {
            specialLineFormat.put(line.getSeparator(), line);
            specialStateMachine.put(line.getSeparator(), ((LineFormat<LogStatsStateMachine, LogStatsState>) line).getEngine());
        }
    }

    @Override
    public List<KeyValue> parse(String line) {
        StateMachineContext context = new AccessLogContext(line);
        for (String sep : specialStateMachine.keySet()) {
            if (line.endsWith(sep)) {
                specialStateMachine.get(sep).transduce(context);
                return context.getResult();
            }
        }
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
