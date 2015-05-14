package com.ctrip.zeus.util;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.nginx.entity.TrafficStatus;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2015/5/7.
 */
public class TrafficStatusCollector {
    private final int syncInterval;
    private final int size;
    private final RollingTrafficStatus rollingTrafficStatus;
    private final static TrafficStatusCollector instance = new TrafficStatusCollector();

    public TrafficStatusCollector() {
        this(60, 10);
    }

    public static TrafficStatusCollector getInstance() {
        return instance;
    }

    public TrafficStatusCollector(int syncInterval, int size) {
        this.syncInterval = syncInterval;
        this.size = size;
        rollingTrafficStatus = new RollingTrafficStatus(this.size, this.syncInterval);
    }

    public void start() {
        final AtomicInteger tick = new AtomicInteger(0);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(syncInterval * 1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    if (tick.incrementAndGet() == 10) {
                        clearDirtyRecords(System.currentTimeMillis());
                        tick.set(0);
                    }
                    fetchTrafficStatus();
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }

    public TrafficStatus getResult() {
        return rollingTrafficStatus.getAccumulatedResult();
    }

    private void fetchTrafficStatus() {
        String stubStatus = LocalClient.getInstance().getStubStatus();
        String reqStatus = LocalClient.getInstance().getReqStatuses();
        rollingTrafficStatus.add(stubStatus, reqStatus);
    }

    private void clearDirtyRecords(long stamp) {
        rollingTrafficStatus.clearDirty(stamp);
    }
}
