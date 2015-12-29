package com.ctrip.zeus.task.nginx;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.nginx.RollingTrafficStatus;
import com.ctrip.zeus.task.AbstractTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2015/12/28.
 */
@Component("nginxStatusFetcher")
public class NginxStatusFetcher extends AbstractTask {
    @Resource
    private RollingTrafficStatus rollingTrafficStatus;
    private AtomicInteger tick = new AtomicInteger();

    @Override
    public void start() {

    }

    @Override
    public long getInterval() {
        return 60 * 1000;
    }

    @Override
    public void run() throws Exception {
        if (tick.incrementAndGet() == 10) {
            clearDirtyRecords(System.currentTimeMillis());
            tick.set(0);
        }
        fetchTrafficStatus();
    }

    @Override
    public void stop() {

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
