package com.ctrip.zeus.nginx;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.util.RollingTrafficStatus;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2015/6/3.
 */
public class NginxStatusFetcher extends QuartzJobBean {

    private RollingTrafficStatus rollingTrafficStatus;
    private AtomicInteger tick = new AtomicInteger();

    public List<TrafficStatus> getResult() {
        return rollingTrafficStatus.getResult();
    }

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

    private void setRollingTrafficStatus(RollingTrafficStatus rollingTrafficStatus) {
        this.rollingTrafficStatus = rollingTrafficStatus;
    }
}
