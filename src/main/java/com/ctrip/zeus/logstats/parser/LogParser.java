package com.ctrip.zeus.logstats.parser;

import java.util.List;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LogParser {

    List<KeyValue> parse(String line);
}