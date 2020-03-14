package com.ctrip.zeus.logstats.parser;

import com.ctrip.zeus.logstats.common.JsonStringWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2017/11/26.
 */
public class ErrorLogStateMachineParser implements LogParser {
    private List<ErrorLogParserFilter> filters;

    public ErrorLogStateMachineParser() {
        filters = new ArrayList<>();
    }

    public ErrorLogStateMachineParser addFilter(ErrorLogParserFilter filter) {
        if (filter != null) {
            filters.add(filter);
        }
        return this;
    }

    @Override
    public List<KeyValue> parse(String line) {
        for (ErrorLogParserFilter filter : filters) {
            List<KeyValue> res = filter.parser(line);
            if (res != null) {
                return res;
            }
        }
        return new ArrayList<>();
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
