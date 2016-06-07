package com.ctrip.zeus.logstats.parser;

import com.ctrip.zeus.logstats.parser.state.AccessLogStateMachine;
import com.ctrip.zeus.logstats.parser.state.LogStatsStateMachine;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class AccessLogParser implements LogParser {
    private LogStatsStateMachine stateMachine = new AccessLogStateMachine();

    @Override
    public List<KeyValue> parse(String line) {

        return null;
    }

    @Override
    public String parseToJsonString(String line) {
        return null;
    }
}
