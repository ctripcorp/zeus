package com.ctrip.zeus.logstats.analyzer.nginx;

import com.ctrip.zeus.logstats.StatsDelegate;
import com.ctrip.zeus.logstats.parser.AccessLogParser;
import com.ctrip.zeus.logstats.parser.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhoumy on 2016/6/3.
 */
public class AccessLogConsumers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WeakReference<AccessLogStatsAnalyzer> producer;
    private final List<StatsDelegate> delegator;
    private final ConcurrentLinkedQueue<String> source;
    private final LogParser logParser;
    private final ExecutorService consumerPool;

    public AccessLogConsumers(AccessLogStatsAnalyzer producer) {
        this.producer = new WeakReference<>(producer);
        this.consumerPool = Executors.newFixedThreadPool(5);
        this.source = new ConcurrentLinkedQueue<>();
        this.logParser = new AccessLogParser(producer.getConfig().getLineFormats());
        this.delegator = producer.getConfig().getDelegators();
    }

    public void consume() {
        consumerPool.execute(new Runnable() {
            @Override
            public void run() {
                while (producer.get() != null) {
                    String value;
                    while ((value = source.poll()) != null) {
                        String result = logParser.parseToJsonString(value);
                        for (StatsDelegate d : delegator) {
                            d.delegate(result);
                        }
                    }
                    Thread.yield();
                }
            }
        });
    }

    public void accept(String value) {
        source.offer(value);
    }

    public void shutDown() {
        try {
            if (!consumerPool.awaitTermination(1000, TimeUnit.SECONDS)) {
                consumerPool.shutdownNow();
                if (!consumerPool.awaitTermination(1000, TimeUnit.SECONDS)) {
                    logger.error("Consumer pool did not terminate.");
                }
            }
        } catch (InterruptedException e) {
            // re-cancel if current thread is interrupted.
            consumerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
