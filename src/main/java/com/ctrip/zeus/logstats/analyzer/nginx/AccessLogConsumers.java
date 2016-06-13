package com.ctrip.zeus.logstats.analyzer.nginx;

import com.ctrip.zeus.logstats.StatsDelegate;
import com.ctrip.zeus.logstats.parser.AccessLogRegexParser;
import com.ctrip.zeus.logstats.parser.KeyValue;
import com.ctrip.zeus.logstats.parser.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private AtomicBoolean running = new AtomicBoolean(true);

    public AccessLogConsumers(AccessLogStatsAnalyzer producer) {
        this.producer = new WeakReference<>(producer);
        this.consumerPool = Executors.newFixedThreadPool(10);
        this.source = new ConcurrentLinkedQueue<>();
        this.logParser = new AccessLogRegexParser(producer.getConfig().getLineFormats());
        this.delegator = producer.getConfig().getDelegators();
    }

    public void consume() {
        for (int i = 0; i < 19; i++) {
            consumerPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (producer.get() != null && running.get()) {
                        String value;
                        while ((value = source.poll()) != null) {
                            List<KeyValue> result = logParser.parse(value);
                            for (StatsDelegate d : delegator) {
                                d.delegate(result);
                            }
                        }
                    }
                }
            });
        }
    }

    public void accept(String value) {
        source.offer(value);
        if (source.size() > 15000) {
            System.out.println("source " + source.size());
        }
    }

    public void shutDown() {
        running.set(false);
        try {
            if (!consumerPool.awaitTermination(1, TimeUnit.SECONDS)) {
                consumerPool.shutdownNow();
                if (!consumerPool.awaitTermination(1, TimeUnit.SECONDS)) {
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
