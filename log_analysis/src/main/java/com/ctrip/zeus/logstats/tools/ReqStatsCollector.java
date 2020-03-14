package com.ctrip.zeus.logstats.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ReqStatsCollector {
    Logger logger = LoggerFactory.getLogger(ReqStatsCollector.class);

    AtomicReference<ConcurrentMap<StatsKey, ReqStats>> ref = new AtomicReference<ConcurrentMap<StatsKey, ReqStats>>(new ConcurrentHashMap<StatsKey, ReqStats>());
    private long reportInterval = 1000 * 60;

    private LongSlots requestSizeSlots;
    private LongSlots responseSizeSlots;
    private LongSlots costSlots;

    public ReqStatsCollector(LongSlots requestSizeSlots, LongSlots responseSizeSlots, LongSlots costSlots, long reportInterval) {
        this.requestSizeSlots = requestSizeSlots;
        this.responseSizeSlots = responseSizeSlots;
        this.costSlots = costSlots;
        this.reportInterval = reportInterval;
        start();
    }

    public ReqStatsCollector(LongSlots requestSizeSlots, LongSlots responseSizeSlots, LongSlots costSlots) {
        this(requestSizeSlots, responseSizeSlots, costSlots, 1000 * 60);
    }

    public void req(StatsKey key, long responseSize, long cost, String status) {
        req(key, 0l, responseSize, cost, status);
    }

    public void req(StatsKey key, long requestSize, long responseSize, long cost, String status) {
        ReqStats stats = ref.get().get(key);
        if (stats == null) {
            stats = new ReqStats(requestSizeSlots, responseSizeSlots, costSlots);
            ReqStats found = ref.get().putIfAbsent(key, stats);
            if (found != null) {
                stats = found;
            }
        }
        stats.addReqInfo(requestSize, responseSize, cost, status);
    }

    public void req(StatsKey key, long requestSize, long responseSize, long cost, String status, Date time) {
        key.setTime(time);
        req(key, requestSize, responseSize, cost, status);
    }

    public long getReportInterval() {
        return reportInterval;
    }

    public void setReportInterval(long reportInterval) {
        this.reportInterval = reportInterval;
    }

    private ConcurrentMap<StatsKey, ReqStats> drainDry() {
        return ref.getAndSet(new ConcurrentHashMap<StatsKey, ReqStats>());
    }

    protected abstract void reportMetrics(ConcurrentMap<StatsKey, ReqStats> statsKeyStatsConcurrentMap, Date date);

    static AtomicInteger NO = new AtomicInteger(0);

    private void start() {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(reportInterval);

                        logger.info("start to report req metrics.Class:" + this.getClass().getSimpleName());
                        Date date = new Date();
                        reportMetrics(drainDry(), date);

                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.warn("Encounter an error while reporting req metrics.");
                    } finally {
                        logger.info("report req metrics over.");
                    }
                }
            }
        };
        t.setName(this.getClass().getSimpleName() + "-" + NO.incrementAndGet());
        t.setDaemon(true);
        t.start();
    }
}
