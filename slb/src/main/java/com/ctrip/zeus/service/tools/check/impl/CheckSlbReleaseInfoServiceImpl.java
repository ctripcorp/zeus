package com.ctrip.zeus.service.tools.check.impl;

import com.ctrip.zeus.model.tools.CheckSlbreleaseResponse;
import com.ctrip.zeus.model.tools.CheckTarget;
import com.ctrip.zeus.model.tools.CheckTargetList;
import com.ctrip.zeus.restful.resource.tools.CheckerClient;
import com.ctrip.zeus.service.tools.check.CheckSlbReleaseInfoService;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by ygshen on 2017/6/23.
 */
@Service("checkSlbReleaseInfoService")
public class CheckSlbReleaseInfoServiceImpl implements CheckSlbReleaseInfoService {
    private final Logger logger = LoggerFactory.getLogger(CheckClientServiceImpl.class);
    private static DynamicIntProperty healthFutureTimeout = DynamicPropertyFactory.getInstance().getIntProperty("health.client.future.timeout", 2000);
    private static DynamicIntProperty healthFutureCheckTimes = DynamicPropertyFactory.getInstance().getIntProperty("health.client.future.check.times", 10);
    private static DynamicIntProperty healthFutureCheckQueueSize = DynamicPropertyFactory.getInstance().getIntProperty("health.client.future.queue.size", 1000);

    private ThreadPoolExecutor threadPoolExecutor;

    CheckSlbReleaseInfoServiceImpl(){
        threadPoolExecutor = new ThreadPoolExecutor(1, 40, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
    @Override
    public Map<String, CheckSlbreleaseResponse> checkSlbReleaseInfo(CheckTargetList targets, int timeOut) throws ExecutionException, InterruptedException {
        HashMap<String, CheckSlbreleaseResponse> responses = new HashMap<>();

        HashMap<String, FutureTask<CheckSlbreleaseResponse>> futureTasks = new HashMap<>();

        if(threadPoolExecutor.getQueue().size()>healthFutureCheckQueueSize.get())
        {
            logger.error("Health checker queue size is bigger than "+healthFutureCheckQueueSize.get());
            return responses;
        }

        List<CheckTarget> targetList = targets.getTargets();
        for (final CheckTarget target : targetList) {
            String server = target.getProtocol() + "://" + target.getIp() + ":" + target.getPort();
            String uri = target.getUri();

            final String url = server + uri;
            final CheckerClient client = CheckerClient.getInstance(server, timeOut);


            FutureTask<CheckSlbreleaseResponse> futureTask = new FutureTask<CheckSlbreleaseResponse>(new Callable<CheckSlbreleaseResponse>() {
                @Override
                public CheckSlbreleaseResponse call() throws Exception {
                    logger.info("Start Check Url:" + url);

                    CheckSlbreleaseResponse response = client.checkSlbReleaseInfo(target.getUri());
                    return response;
                }
            });
            String key = target.getIp() + "/" + target.getPort();
            threadPoolExecutor.execute(futureTask);
            futureTasks.put(key, futureTask);
        }

        int sleepInterval = healthFutureTimeout.get()+timeOut / healthFutureCheckTimes.get();
        Set<String> finishedServer = new HashSet<>();

        for (int i = 0; i <= healthFutureCheckTimes.get(); i++) {
            for (String sip : futureTasks.keySet()) {
                FutureTask<CheckSlbreleaseResponse> futureTask = futureTasks.get(sip);
                if (futureTask.isDone() && !finishedServer.contains(sip)) {
                    finishedServer.add(sip);
                    CheckSlbreleaseResponse response = futureTask.get();
                    responses.put(sip, response);
                }
            }

            if(finishedServer.size()==targetList.size()){
                return responses;
            }
            try {
                Thread.sleep(sleepInterval);
            } catch (Exception e) {
                logger.warn("Check health sleep interrupted.");
            }
        }

        return responses;
    }
}
