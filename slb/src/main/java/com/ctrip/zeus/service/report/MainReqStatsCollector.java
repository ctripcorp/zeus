package com.ctrip.zeus.service.report;

import com.ctrip.zeus.logstats.tools.LongSlots;
import com.ctrip.zeus.logstats.tools.ReqStats;
import com.ctrip.zeus.logstats.tools.ReqStatsCollector;
import com.ctrip.zeus.logstats.tools.StatsKey;
import com.ctrip.zeus.service.report.stats.ReportMetrics;
import com.ctrip.zeus.util.ObjectJsonWriter;
import com.ctrip.zeus.util.S;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by fanqq on 2019/09/27.
 */
public class MainReqStatsCollector extends ReqStatsCollector {

    private static List<ReportMetrics> reporters = new ArrayList<>();

    private static DynamicBooleanProperty enableAll = DynamicPropertyFactory.getInstance().getBooleanProperty("main.req.enable.all", true);
    private static DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();
    private String localIp = null;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public MainReqStatsCollector(LongSlots requestSizeSlots, LongSlots responseSizeSlots, LongSlots costSlots, long reportInterval) {
        super(requestSizeSlots, responseSizeSlots, costSlots, reportInterval);
        localIp = S.getIp();
    }

    public static void registerReporter(ReportMetrics reportMetrics) {
        if (reportMetrics == null) return;
        reporters.add(reportMetrics);
    }

    public MainReqStatsCollector(LongSlots requestSizeSlots, LongSlots responseSizeSlots, LongSlots costSlots) {
        super(requestSizeSlots, responseSizeSlots, costSlots);
        localIp = S.getIp();
    }

    @Override
    protected void reportMetrics(ConcurrentMap<StatsKey, ReqStats> statsKeyStatsConcurrentMap, Date reportTime) {
        if (localIp == null) {
            localIp = S.getIp();
        }
        if (!factory.getBooleanProperty("main.req.enable.ip" + localIp, false).get() && !enableAll.get()) {
            return;
        }
        logger.info("[[metrics=true]]Start:" + reporters.size() + ";Date:" + reportTime.getTime());
        for (ReportMetrics reportMetrics : reporters) {
            try {
                logger.info("[[metrics=true]]Start:" + reportMetrics.getClass().getSimpleName() + ";Date:" + reportTime.getTime());
                reportMetrics.reportMetrics(statsKeyStatsConcurrentMap, reportTime);
                logger.info("[[metrics=true]]End:" + reportMetrics.getClass().getSimpleName() + ";Date:" + reportTime.getTime());
                logger.info("[[metrics=true]]End:" + ObjectJsonWriter.write(statsKeyStatsConcurrentMap));
            } catch (Exception e) {
                logger.warn("Report Metrics Failed.Reporter:" + reportMetrics.getClass().getSimpleName(), e);
            }
        }
    }

}
