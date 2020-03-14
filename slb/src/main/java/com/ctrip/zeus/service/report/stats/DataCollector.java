package com.ctrip.zeus.service.report.stats;

import java.util.Map;

public interface DataCollector {

    void collect(Map<String, String> fields);
}
