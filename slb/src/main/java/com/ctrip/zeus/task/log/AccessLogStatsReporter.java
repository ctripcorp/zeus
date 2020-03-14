package com.ctrip.zeus.task.log;

import com.ctrip.zeus.logstats.StatsDelegate;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzer;
import com.ctrip.zeus.logstats.analyzer.LogStatsAnalyzerConfig;
import com.ctrip.zeus.logstats.analyzer.nginx.AccessLogStatsAnalyzer;
import com.ctrip.zeus.logstats.common.AccessLogStateMachineFormat;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.tools.LongSlots;
import com.ctrip.zeus.logstats.tools.ReqStatsCollector;
import com.ctrip.zeus.logstats.tools.StatsKey;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.conf.LogFormat;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.report.AccessLogReporterConsts;
import com.ctrip.zeus.service.report.AccessLogStatsReporterInitialize;
import com.ctrip.zeus.service.report.MainReqStatsCollector;
import com.ctrip.zeus.service.report.MetricsReporter;
import com.ctrip.zeus.service.report.stats.DataCollector;
import com.ctrip.zeus.service.report.stats.DataCollectorFactory;
import com.ctrip.zeus.service.report.stats.MetricsFactory;
import com.ctrip.zeus.service.report.stats.MonitorMetrics;
import com.ctrip.zeus.service.traffic.TrafficMonitorService;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.DateUtils;
import com.ctrip.zeus.util.PropertyCache;
import com.netflix.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author:zhoumy
 * @date: 12/7/2015.
 */
@Component("accessLogStatsReporter")
public class AccessLogStatsReporter extends AbstractTask {
    @Resource
    private PropertyService propertyService;
    @Resource
    private SlbRepository slbRepository;
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private static final DynamicIntProperty TrackerReadSize = DynamicPropertyFactory.getInstance().getIntProperty("accesslog.tracker.readsize", 1024 * 5);
    private static final DynamicBooleanProperty ReportEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("reporter.req.metrics.enabled", true);
    private static final DynamicStringProperty ShouldReport = DynamicPropertyFactory.getInstance().getStringProperty(getShouldReportKey(), null);

    private static final DynamicBooleanProperty mainEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("main.req.enabled", true);

    @Resource
    private MetricsFactory metricsFactory;
    @Resource
    private DataCollectorFactory dataCollectorFactory;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private PropertyCache propertyCache;
    @Resource
    private TagInfoCache tagInfoCache;
    @Resource
    private TrafficMonitorService trafficMonitorService;
    @Autowired(required = false)
    private AccessLogStatsReporterInitialize accessLogStatsReporterInitialize;

    private final LogStatsAnalyzer logStatsAnalyzer;
    private ReqStatsCollector mainReq;
    private List<DataCollector> dataCollectors;
    private List<MetricsReporter> reporters = new ArrayList<>();

    private String slbId = null;
    private String slbName = null;
    private String idc = null;
    private static final String timeKey = AccessLogReporterConsts.TIME_KEY;
    private static final String costKey = AccessLogReporterConsts.COST_KEY;
    private static final String statusKey = AccessLogReporterConsts.STATUS_KEY;
    private static final String upCostKey = AccessLogReporterConsts.UP_COST_KEY;
    private static final String uriKey = AccessLogReporterConsts.URI_KEY;
    private static final String groupKey = AccessLogReporterConsts.GROUP_KEY;
    private static final String drKey = AccessLogReporterConsts.DR_KEY;
    private static final String wafCostKey = AccessLogReporterConsts.WAF_COST_KEY;
    private static final String sslProtocolKey = AccessLogReporterConsts.SSL_PROTOCOL_KEY;
    private static final String interceptStatus = AccessLogReporterConsts.INTERCEPT_STATUS;
    private static final Set<String> taggableKeys = AccessLogReporterConsts.TAGGABLEKEYS;
    private final String logFilename = "/opt/logs/nginx/access.log";
    private String appName = ConfigurationManager.getConfigInstance().getString("app-name", "slb");
    private AtomicBoolean flag = new AtomicBoolean("true".equals(ShouldReport.get()));

    private LongSlots msSlots = new LongSlots(">100s")
            .slot("0~1ms", 1l)
            .slot("1~2ms", 2l)
            .slot("2~3ms", 3l)
            .slot("3~5ms", 5l)
            .slot("5~8ms", 8l)
            .slot("8~10ms", 10l)
            .slot("10~50ms", 50l)
            .slot("50~100ms", 100l)
            .slot("100~200ms", 200l)
            .slot("200~500ms", 500l)
            .slot("500ms~1s", 1000l)
            .slot("1~2s", 2000l)
            .slot("2~3s", 3000l)
            .slot("3~4s", 4000l)
            .slot("4~5s", 5000l)
            .slot("5~10s", 10000l)
            .slot("10~20s", 20000l)
            .slot("20~30s", 30000l)
            .slot("30~50s", 50000l)
            .slot("50~100s", 100000l);

    public AccessLogStatsReporter() {
        LogStatsAnalyzerConfig config = new AccessLogStatsAnalyzer.LogStatsAnalyzerConfigBuilder()
                .isStartFromHead(false)
                .setDefaultLogFormat(new AccessLogStateMachineFormat(LogFormat.getDefaultMainCompactString(), LogFormat.getDefaultSeparator()).generate())
                .addLogFormat(new AccessLogStateMachineFormat(LogFormat.getMainCompactString(), LogFormat.getSeparator()).generate())
                .setLogFilename(logFilename)
                .setReadBufferSize(TrackerReadSize.get())
                .setNumberOfConsumers(getConsumerCount())
                .registerLogStatsDelegator(new StatsDelegate<List<KeyValue>>() {
                    @Override
                    public void delegate(List<KeyValue> input) {
                    }

                    @Override
                    public void delegate(String raw, List<KeyValue> input) {
                        if (input.isEmpty()) {
                            logger.warn("[[LogParsing=failed]]Access log parsing failed.\n" + raw);
                            return;
                        }
                        try {
                            sendMetrics(raw, input);
                        } catch (NumberFormatException nfe) {
                            // todo: swallow number format issue
                            logger.warn("Collect metrics data failed with message:" + nfe.getMessage());
                        }
                    }
                }).build();
        logStatsAnalyzer = new AccessLogStatsAnalyzer(config);
    }

    public void registerReporter(MetricsReporter reporter) {
        this.reporters.add(reporter);
    }

    @Override
    public void start() {
        try {
            mainReq = metricsFactory.createIfAbsent("mainReq", MainReqStatsCollector.class);
            MainReqStatsCollector.registerReporter(new MonitorMetrics().setTrafficMonitorService(trafficMonitorService));
            if (accessLogStatsReporterInitialize != null) {
                accessLogStatsReporterInitialize.init(this);
            }
            if (mainReq == null) {
                logger.error("Fail to create req collector.");
            }
        } catch (Exception ex) {
            logger.error("Unexpected exception occurred when creating cat/clog req collector.");
        }


        if (flag.get()) {
            try {
                logStatsAnalyzer.start();
            } catch (IOException e) {
                try {
                    Thread.sleep(500L);
                    logStatsAnalyzer.start();
                } catch (InterruptedException e1) {
                } catch (IOException e1) {
                    logger.error("Fail to start logStatsAnalyzer", e1);
                    flag.compareAndSet(true, false);
                }
            }
        }
    }

    @Override
    public long getInterval() {
        return 60 * 1000;
    }

    @Override
    public void run() throws Exception {
        if (logStatsAnalyzer == null) return;
        if (!ReportEnabled.get()
                || (ReportEnabled.get() && "false".equals(ShouldReport.get()))) {
            stop();
            return;
        }

        if (flag.compareAndSet(false, true)) {
            start();
        }
        while (flag.get() && !logStatsAnalyzer.reachFileEnd()) {
            logStatsAnalyzer.run();
        }
    }

    @Override
    public void stop() {
        if (flag.compareAndSet(true, false) && logStatsAnalyzer != null) {
            try {
                logStatsAnalyzer.stop();
            } catch (IOException e) {
                flag.compareAndSet(false, true);
                logger.error("Fail to stop logStatsAnalyzer.", e);
            }
        }
    }

    public LogStatsAnalyzer getLogStatsAnalyzer() {
        return logStatsAnalyzer;
    }

    public String getLogFilename() {
        return logFilename;
    }

    public void initializeDataCollectors(Class<? extends DataCollector>... collectorClasses) {
        List<DataCollector> dataCollectors = new ArrayList<>();
        for (Class<? extends DataCollector> clazz : collectorClasses) {
            try {
                DataCollector collector = dataCollectorFactory.getInstance(clazz);
                if (collector == null) {
                    logger.error("Failed to create " + clazz.getSimpleName());
                    continue;
                }
                dataCollectors.add(collector);
            } catch (Exception ex) {
                logger.error("Unexpected exception occurred when creating " + clazz.getSimpleName(), ex);
            }
        }
        this.dataCollectors = dataCollectors;
    }

    protected String getSlbId() {
        if (slbId == null || slbId.equals("0")) {
            try {
                Long[] slbIds = entityFactory.getSlbIdsByIp(LocalInfoPack.INSTANCE.getIp(), SelectionMode.ONLINE_FIRST);
                if (slbIds.length == 0) {
                    return "0";
                } else {
                    slbId = "" + slbIds[0];
                    Slb slb = slbRepository.getById(slbIds[0]);
                    slbName = slb.getName();
                }
            } catch (Exception e) {
                logger.error("Fail to get slb id by local server ip.", e);
                return "0";
            }
        }
        return slbId;
    }

    protected String getIdc(Long slbId) {
        if (idc == null && slbId != null) {
            try {
                Property property = propertyService.getProperty("idc_code", slbId, "slb");
                if (property != null) {
                    idc = property.getValue();
                } else {
                    idc = "-";
                }
            } catch (Exception e) {
                logger.warn("Get Property Failed.Idc_Code:" + slbId);
            }
        }
        return idc;
    }

    protected void sendMetrics(String raw, List<KeyValue> value) {
        if (value.size() == 0) return;
        long requestTime = -1L;
        long upResponseTime = -1L;
        String status = "0";
        Date localTime = null;
        Map<String, String> stats = new HashMap<>();
        String catMeta = null;
        Long headerSize = null;
        Long cookieSize = null;
        String userAgent = null;
        String refer = null;
        String pageId = null;
        String xff = null;
        String headers = null;
        String cookie = null;
        String remoteAddr = null;
        String requestUri = null;
        String drPrefix = null;
        for (KeyValue kv : value) {
            String v = kv.getValue();
            String k = kv.getKey();
            if (v == null || v.isEmpty()) {
                v = "-";
            }
            if (k.startsWith("upstream_")) {
                int idxUp = v.indexOf(" : ");
                if (idxUp > 0) v = v.substring(0, idxUp);
                idxUp = v.indexOf(", ");
                if (idxUp > 0) v = v.substring(0, idxUp);
            }
            switch (k) {
                case timeKey: {
                    try {
                        localTime = DateUtils.convert(v);
                    } catch (Exception e) {
                        logger.error("DateUtils fail to convert string values " + v + " into java.util.Date object.");
                        localTime = new Date();
                    }
                }
                break;
                case wafCostKey:
                    long wafCost;
                    if (!v.equals("-")) {
                        wafCost = Double.valueOf(Double.parseDouble(v) / 1000).longValue();
                        v = msSlots.getSlot(wafCost);
                    }
                    break;
                case costKey:
                case upCostKey: {
                    long time;
                    if (!v.equals("-")) {
                        time = Double.valueOf(Double.parseDouble(v) * 1000.0).longValue();
                        if (k.equals(costKey))
                            requestTime = time;
                        if (k.equals(upCostKey))
                            upResponseTime = time;
                        v = metricsFactory.getTimeSlots().getSlot(time);
                    }
                }
                break;
                case statusKey: {
                    status = v;
                }
                break;
                case groupKey: {
                    if (v.startsWith("backend_"))
                        v = v.substring(8);
                }
                break;
                case uriKey: {
                    requestUri = v;
                    int i = v.indexOf('?');
                    if (i > 0) {
                        v = v.substring(0, i);
                    }
                }
                break;
                case "policy_name": {
                    if (v.startsWith("policy_"))
                        v = v.substring(7);
                }
                break;
                case drKey: {
                    if (!v.equals("-")) {
                        if (v.startsWith("dr_in_")) {
                            v = v.substring(6);
                            drPrefix = "dr_in_";
                        } else if (v.startsWith("dr_out_")) {
                            v = v.substring(7);
                            drPrefix = "dr_out_";
                        } else if (v.startsWith("dr_pass_")) {
                            v = v.substring(8);
                            drPrefix = "dr_pass_";
                        }
                    }
                }
                break;
                case "http_cat_meta": {
                    if (v != null && !v.equalsIgnoreCase("-")) {
                        catMeta = v;
                    }
                }
                break;
                case "cookie_size": {
                    if (v != null && !v.isEmpty() && !v.equalsIgnoreCase("-")) {
                        cookieSize = Long.parseLong(v);
                    }
                }
                break;
                case "header_size": {
                    if (v != null && !v.isEmpty() && !v.equalsIgnoreCase("-")) {
                        headerSize = Long.parseLong(v);
                    }
                }
                break;
                case "x_ctrip_pageid": {
                    pageId = v;
                }
                break;
                case "http_user_agent": {
                    userAgent = v;
                }
                break;
                case "http_referer": {
                    refer = v;
                }
                break;
                case "http_x_forwarded_for": {
                    xff = v;
                }
                break;
                case "remote_addr": {
                    remoteAddr = v;
                }
                break;
                case "header_value": {
                    headers = v;
                }
                break;
                case "http_cookie": {
                    cookie = v;
                }
                break;
            }
            stats.put(k, v);
        }

        String groupId = stats.get(groupKey);
        if (groupId != null) {
            String sbu = propertyCache.getPropertyValue("group", groupId, "SBU");
            if (sbu != null) {
                stats.put("sbu", sbu);
            } else {
                stats.put("sbu", "unknown");
            }
            String appid = tagInfoCache.getAppId(groupId);
            if ("dr_out_".equals(drPrefix) || "dr_pass_".equals(drPrefix)) {
                appid = drPrefix + appid;
            }

            stats.put("group_appid", appid);

            String vsid = tagInfoCache.getVsId(slbId, stats.get("host"), stats.get("server_port"));
            stats.put("vsid", vsid);

        } else {
            stats.put("sbu", "unknown");
            stats.put("group_appid", "unknown");
            stats.put("vsid", "0");
        }
        stats.put("diff", upResponseTime == -1L ? "-" : metricsFactory.getDiffSlots().getSlot(requestTime - upResponseTime));

        for (MetricsReporter reporter : reporters) {
            reporter.metrics(stats, headerSize, cookieSize, pageId, userAgent, refer, localTime, remoteAddr, xff, headers, cookie, requestUri, requestTime, status, groupId, raw, catMeta);
        }

        metrics(stats, requestTime, status, localTime, groupId, raw);

        for (DataCollector collector : dataCollectors) {
            try {
                collector.collect(stats);
            } catch (Exception ex) {
                logger.error("Error occurs in " + collector.getClass().getSimpleName(), ex);
            }
        }
    }


    private void metrics(Map<String, String> stats, long requestTime, String status, Date localTime, String groupId, String raw) {

        String tagIdc = getIdc(Long.parseLong(getSlbId()));
        if (mainReq != null) {
            StatsKey tran = new StatsKey(appName)
                    .addTag("slb_id", getSlbId())
                    .addTag("slb_name", slbName == null ? "unknown" : slbName)
                    .reportCount(true)
                    .reportCost(false)
                    .reportStatus(false)
                    .reportRequestSize(false)
                    .reportResponseSize(false);
            if (tagIdc != null) {
                tran.addTag("idc", tagIdc);
            }
            for (String rawKey : taggableKeys) {
                String value = stats.get(rawKey);
                if (value == null) {
                    continue;
                }
                value = value.trim();
                if (value.isEmpty()) {
                    continue;
                }
                String key = metricsFactory.getKey(rawKey);
                try {
                    //Do not tag request_uri if succeeds
                    if ((key.equals(uriKey) && (status.charAt(0) == '2' || status.charAt(0) == '3'))) {
                        continue;
                    }
                    if ("http_x_via".equalsIgnoreCase(key) && !value.equalsIgnoreCase("akamai")) {
                        value = "-";
                    }
                    tran.addTag(key, value);
                } catch (IllegalArgumentException ex) {
                    logger.warn("Key/value returns null when adding to tag. key=" + key + ", value=" + value + ".");
                    tran.addTag(key, "-");
                }
            }
            String hostIp = LocalInfoPack.INSTANCE.getIp();
            if (hostIp != null) {
                tran.addTag("slb_server", hostIp);
            }
            if (mainEnable.get()) {
                mainReq.req(tran, 0L, 0L, requestTime, status, localTime);
            }
            if (status.charAt(0) != '2' && status.charAt(0) != '3') {
                StringBuilder sb = new StringBuilder();
                sb.append("[[from=nginx.access.log,");
                if (groupId != null) {
                    sb.append("groupId=").append(groupId).append(",");
                }
                if (slbId != null) {
                    sb.append("slbId=").append(slbId).append(",");
                }
                sb.append("status=").append(status);
                sb.append("]] ");
                sb.append(raw);
                logger.info(sb.toString());
            }
        }
    }


    protected void setMetricsFactory(MetricsFactory metricsFactory) {
        this.metricsFactory = metricsFactory;
    }

    private static String getShouldReportKey() {
        return "reporter." + LocalInfoPack.INSTANCE.getIp() + ".metrics";
    }

    private static int getConsumerCount() {
        String defaultKey = "accesslog.consumer.count";
        int ipSpecific = DynamicPropertyFactory.getInstance().getIntProperty(defaultKey + ".ip." + LocalInfoPack.INSTANCE.getIp(), -1).get();
        if (ipSpecific == -1) {
            return DynamicPropertyFactory.getInstance().getIntProperty(defaultKey, 1).get();
        } else {
            return ipSpecific;
        }
    }
}
