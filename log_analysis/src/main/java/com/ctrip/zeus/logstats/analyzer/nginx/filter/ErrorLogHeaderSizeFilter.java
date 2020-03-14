package com.ctrip.zeus.logstats.analyzer.nginx.filter;

import com.ctrip.zeus.logstats.parser.ErrorLogParserFilter;
import com.ctrip.zeus.logstats.parser.KeyValue;

import java.util.List;

/**
 * Created by fanqq on 2017/11/28.
 */
public class ErrorLogHeaderSizeFilter implements ErrorLogParserFilter {
    @Override
    public List<KeyValue> parser(String line) {
        return null;
    }
}
