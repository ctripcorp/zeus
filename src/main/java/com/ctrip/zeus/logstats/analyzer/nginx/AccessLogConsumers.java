package com.ctrip.zeus.logstats.analyzer.nginx;

import com.ctrip.zeus.logstats.StatsDelegate;
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
    private final int safeLatch = 5000;
    private final WeakReference<AccessLogStatsAnalyzer> producer;
    private final List<StatsDelegate> delegator;
    private final ConcurrentLinkedQueue<String> source;
    private final LogParser logParser;
    private final ExecutorService consumerPool;
    private final int size;

    private AtomicBoolean running = new AtomicBoolean(true);

    public AccessLogConsumers(AccessLogStatsAnalyzer producer, LogParser logParser, int size) {
        this.producer = new WeakReference<>(producer);
        this.consumerPool = Executors.newFixedThreadPool(size);
        this.source = new ConcurrentLinkedQueue<>();
        this.logParser = logParser;
        this.delegator = producer.getConfig().getDelegators();
        this.size = size;
    }

    public void consume() {
        logger.info("Create " + size + " access log consumers.");
        for (int i = 0; i < size; i++) {
            consumerPool.execute(new Runnable() {
                @Override
                public void run() {
                    while ((producer.get() != null && running.get())
                            || (!running.get() && !source.isEmpty())) {
                        String value;
                        while ((value = source.poll()) != null) {
                            try {
                                List<KeyValue> result = logParser.parse(value);
                                for (StatsDelegate d : delegator) {
                                    try {
                                        d.delegate(result);
                                    } catch(Exception ex){
                                        logger.error("Delegator of AccessLogConsumers throws an exception.", ex);
                                    }
                                }
                            } catch (Exception ex) {
                                logger.error("Consumer throws exception when parsing " + value + ".", ex);
                            }
                        }
                    }
                }
            });
        }
    }

    public void accept(String value) {
        if (running.get()) {
            if (source.size() < safeLatch) {
                source.offer(value);
            } else {
                logger.warn("Too busy Consumers - new values are rejected.");
            }
        } else {
            logger.warn("Consumers are not running - new values are rejected.");
        }
    }

    public boolean available() {
        return running.get() && source.size() < safeLatch;
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

    public boolean isClosed() {
        return consumerPool.isTerminated();
    }
}
