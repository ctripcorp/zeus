package com.ctrip.zeus.service.tools.check.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.tools.CheckResponse;
import com.ctrip.zeus.model.tools.CheckTarget;
import com.ctrip.zeus.restful.resource.tools.CheckerClient;
import com.ctrip.zeus.service.tools.check.CheckClientService;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by ygshen on 2016/12/15.
 */
@Service("checkClientService")
public class CheckClientServiceImpl implements CheckClientService {
    private final Logger logger = LoggerFactory.getLogger(CheckClientServiceImpl.class);
    private static DynamicIntProperty healthFutureTimeout = DynamicPropertyFactory.getInstance().getIntProperty("health.client.future.timeout", 2000);
    private static DynamicIntProperty healthFutureCheckTimes = DynamicPropertyFactory.getInstance().getIntProperty("health.client.future.check.times", 10);
    private static DynamicIntProperty healthFutureCheckQueueSize = DynamicPropertyFactory.getInstance().getIntProperty("health.client.future.queue.size", 1000);

    private ThreadPoolExecutor threadPoolExecutor;

    CheckClientServiceImpl() {
        threadPoolExecutor = new ThreadPoolExecutor(5, 20, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public Map<CheckTarget, CheckResponse> checkUrl(List<CheckTarget> targets, int timeOut) throws ExecutionException, InterruptedException {
        HashMap<CheckTarget, CheckResponse> responses = new HashMap<>();

        HashMap<CheckTarget, FutureTask<CheckResponse>> futureTasks = new HashMap<>();

        if (threadPoolExecutor.getQueue().size() > healthFutureCheckQueueSize.get()) {
            logger.error("Health checker queue size is bigger than " + healthFutureCheckQueueSize.get());
            return responses;
        }

        for (final CheckTarget target : targets) {
            String server = target.getProtocol() + "://" + target.getIp() + ":" + target.getPort();
            String uri = target.getUri();

            final String url = server + uri;
            final CheckerClient client = CheckerClient.getInstance(server, timeOut);


            FutureTask<CheckResponse> futureTask = new FutureTask<CheckResponse>(new Callable<CheckResponse>() {
                @Override
                public CheckResponse call() throws Exception {
                    logger.info("Start Check Url:" + url);
                    if (target == null) throw new ValidationException("Check target shall not be null");
                    CheckResponse response = client.check(url, target);
                    int code = response.getCode();
                    String message = response.getStatus();
                    if (code == -1) {
                        logger.info("Check Url " + url + " failed. Response message " + message);
                    } else {
                        logger.info("Check Url " + url + " succeed. Response message " + message);
                    }

                    return response;
                }
            });
            threadPoolExecutor.execute(futureTask);
            futureTasks.put(target, futureTask);
        }

        int sleepInterval = healthFutureTimeout.get() + timeOut / healthFutureCheckTimes.get();
        Set<CheckTarget> finishedServer = new HashSet<>();

        for (int i = 0; i <= healthFutureCheckTimes.get(); i++) {
            for (CheckTarget sip : futureTasks.keySet()) {
                FutureTask<CheckResponse> futureTask = futureTasks.get(sip);
                if (futureTask.isDone() && !finishedServer.contains(sip)) {
                    finishedServer.add(sip);
                    CheckResponse response = futureTask.get();
                    responses.put(sip, response);
                }
            }

            if (finishedServer.size() == targets.size()) {
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
