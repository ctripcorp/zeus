package com.ctrip.zeus.logstats.parser;

import java.util.List;

/**
 * Created by fanqq on 2017/11/28.
 */
public interface ErrorLogParserFilter {

    List<KeyValue> parser(String line);

}
