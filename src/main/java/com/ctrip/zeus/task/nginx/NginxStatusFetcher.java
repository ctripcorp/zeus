package com.ctrip.zeus.task.nginx;

import com.ctrip.zeus.client.LocalClient;
import com.ctrip.zeus.nginx.RollingTrafficStatus;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhoumy on 2015/12/28.
 */
@Component("nginxStatusFetcher")
public class NginxStatusFetcher extends AbstractTask {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RollingTrafficStatus rollingTrafficStatus;
    @Resource
    EntityFactory entityFactory;

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
        Long[] slbIds = entityFactory.getSlbIdsByIp(S.getIp(), SelectionMode.ONLINE_FIRST);
        if (slbIds == null || slbIds.length == 0){
            logger.error("Can Not Found Slb by Local Ip. NginxStatusFetcher ignore task! Local Ip : "+S.getIp());
            return;
        }

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
