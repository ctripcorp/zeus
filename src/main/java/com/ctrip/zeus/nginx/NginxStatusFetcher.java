package com.ctrip.zeus.nginx;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2015/6/3.
 */
@DisallowConcurrentExecution
public class NginxStatusFetcher extends QuartzJobBean {

    private RollingTrafficStatus rollingTrafficStatus;
    private AtomicInteger tick = new AtomicInteger();

    @Override
    protected void executeInternal(org.quartz.JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (tick.incrementAndGet() == 10) {
            clearDirtyRecords(System.currentTimeMillis());
            tick.set(0);
        }
        fetchTrafficStatus();
    }

    private void fetchTrafficStatus() {
        String stubStatus = LocalClient.getInstance().getStubStatus();
        String reqStatus = LocalClient.getInstance().getReqStatuses();
        rollingTrafficStatus.add(stubStatus, reqStatus);
    }

    private void clearDirtyRecords(long stamp) {
        rollingTrafficStatus.clearDirty(stamp);
    }

    public void setRollingTrafficStatus(RollingTrafficStatus rollingTrafficStatus) {
        this.rollingTrafficStatus = rollingTrafficStatus;
    }
}
