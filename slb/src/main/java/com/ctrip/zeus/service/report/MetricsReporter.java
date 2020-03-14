package com.ctrip.zeus.service.report;

import java.util.Date;
import java.util.Map;

public interface MetricsReporter {
    void metrics(Map<String, String> stats, Long headerSize, Long cookieSize, String pageId, String userAgent,
                 String refer, Date localTime, String remoteAddr, String xff,
                 String headers, String cookie, String requestUri, long requestTime, String status, String groupId, String raw, String catMeta);
}
