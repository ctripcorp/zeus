package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.exceptions.NginxConfPushingTimeoutException;
import com.ctrip.zeus.model.model.DyUpstreamOpsData;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.nginx.handler.NginxConfOpsService;
import com.ctrip.zeus.service.nginx.handler.NginxOpsService;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.util.ObjectJsonWriter;
import com.ctrip.zeus.util.TimerUtils;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Service("nginxService")
public class NginxServiceImpl implements NginxService {
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);
    private static DynamicIntProperty nginxFutureTimeout = DynamicPropertyFactory.getInstance().getIntProperty("nginx.client.future.timeout", 8000);
    private static DynamicIntProperty nginxFutureCheckTimes = DynamicPropertyFactory.getInstance().getIntProperty("nginx.client.future.check.times", 10);
    private static DynamicIntProperty threadPoolSize = DynamicPropertyFactory.getInstance().getIntProperty("nginx.service.thread.pool.max.size", 500);
    private static DynamicIntProperty threadPoolCoreSize = DynamicPropertyFactory.getInstance().getIntProperty("nginx.service.thread.pool.core.size", 10);
    private static DynamicIntProperty threadPoolKeepAlive = DynamicPropertyFactory.getInstance().getIntProperty("nginx.service.thread.pool.keepalive", 60);
    private static DynamicIntProperty minSuccPctToComplete = DynamicPropertyFactory.getInstance().getIntProperty("nginx.service.minSuccPctToComplete", 30);
    private static DynamicIntProperty batchTimeoutThreshold = DynamicPropertyFactory.getInstance().getIntProperty("nginx.service.batch.timeout", 10 * 1000);

    @Resource
    private NginxOpsService nginxOpsService;
    @Resource
    private NginxConfOpsService nginxConfOpsService;
    @Resource
    private LocalInfoService localInfoService;

    private ThreadPoolExecutor threadPoolExecutor;
    private ThreadPoolExecutor monitorThreadPoolExecutor;

    NginxServiceImpl() {
        threadPoolExecutor = new ThreadPoolExecutor(threadPoolCoreSize.get(), threadPoolSize.get(), threadPoolKeepAlive.get(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1));
        threadPoolSize.addCallback(() -> {
            try {
                threadPoolExecutor.setMaximumPoolSize(threadPoolSize.get());
            } catch (Exception e) {
                logger.warn("Update Thread Pool Size Failed.", e);
            }
        });
        threadPoolCoreSize.addCallback(() -> {
            try {
                threadPoolExecutor.setCorePoolSize(threadPoolCoreSize.get());
            } catch (Exception e) {
                logger.warn("Update Thread Pool Size Failed.", e);
            }
        });
        threadPoolKeepAlive.addCallback(() -> {
            try {
                threadPoolExecutor.setKeepAliveTime(threadPoolKeepAlive.get(), TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("Update Thread Pool KeepAlive Failed.", e);
            }
        });

        monitorThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            private final AtomicInteger NO = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("NginxService-PushConfMonitorThread-" + NO.incrementAndGet());
                return thread;
            }
        });
    }

    private final Logger logger = LoggerFactory.getLogger(NginxServiceImpl.class);

    @Override
    public List<NginxResponse> update(String nginxConf, NginxConfEntry entry, Set<Long> updateVsIds, Set<Long> cleanVsIds, DyUpstreamOpsData[] dyups, boolean needReload, boolean needTest, boolean needDyups) throws Exception {
        List<NginxResponse> response = new ArrayList<>();
        try {
            logger.info("[NginxServiceImpl]Start update nginx config files.");
            //1. update config
            nginxConfOpsService.updateNginxConf(nginxConf);
            FileOpRecord fileOpRecord = nginxConfOpsService.cleanAndUpdateConf(cleanVsIds, updateVsIds, entry);
            //2. test reload or dyups, if needed. undo config if failed.
            if (needTest) {
                NginxResponse res = nginxOpsService.test();
                response.add(res);
                if (!res.getSucceed()) {
                    logger.error("[NginxService]update failed.Test conf failed.");
                    nginxConfOpsService.undoUpdateNginxConf();
                    nginxConfOpsService.undoCleanAndUpdateConf(cleanVsIds, entry, fileOpRecord);
                }
            }
            if (needReload) {
                NginxResponse res = nginxOpsService.reload();
                response.add(res);
                if (!res.getSucceed()) {
                    logger.error("[NginxService]update failed.Reload conf failed.");
                    nginxConfOpsService.undoUpdateNginxConf();
                    nginxConfOpsService.undoCleanAndUpdateConf(cleanVsIds, entry, fileOpRecord);
                }
            }
            if (needDyups) {
                List<NginxResponse> dyupsRes = nginxOpsService.dyups(dyups);
                response.addAll(dyupsRes);
                for (NginxResponse res : dyupsRes) {
                    if (!res.getSucceed()) {
                        logger.error("[NginxService]update failed.Dyups conf failed.");
                        nginxConfOpsService.undoUpdateNginxConf();
                        nginxConfOpsService.undoCleanAndUpdateConf(cleanVsIds, entry, fileOpRecord);
                        break;
                    }
                }
            }
            logger.info("[NginxServiceImpl]Finish update nginx config files.");
        } finally {
            logger.info("Nginx update response. Responses:" + response.toString());
        }
        return response;
    }

    @Override
    public NginxResponse refresh(String nginxConf, NginxConfEntry entry, boolean reload) throws Exception {
        logger.info("Start refresh nginx config files.");
        nginxConfOpsService.updateNginxConf(nginxConf);
        Long updateAllFlag = nginxConfOpsService.updateAll(entry);
        if (reload) {
            NginxResponse res = nginxOpsService.reload();
            if (!res.getSucceed()) {
                logger.error("[NginxService]update failed.Reload conf failed.");
                nginxConfOpsService.undoUpdateNginxConf();
                nginxConfOpsService.undoUpdateAll(updateAllFlag);
            }
            return res;
        }
        return new NginxResponse().setSucceed(true);
    }


    @Override
    public NginxResponse updateConf(List<SlbServer> slbServers) throws Exception {
        logger.info("[Push Conf] Assign updating conf job to slb servers.");
        int queueSize = threadPoolExecutor.getQueue().size();
        if (queueSize > threadPoolExecutor.getMaximumPoolSize() / 2) {
            logger.warn("Thread Pool Queue Size:" + threadPoolExecutor.getQueue().size());
        }

        String batchId = UUID.randomUUID().toString();
        logger.info("[[FutureTaskId={}]] [Push Conf Batch] Batch starts.", batchId);

        CompletionService<NginxResponse> completionService = new ExecutorCompletionService<>(threadPoolExecutor);
        int taskSize = slbServers.size();
        Map<PushConfCallable, Future<NginxResponse>> allFutures = new HashMap<>(taskSize);
        for (SlbServer slbServer : slbServers) {
            final String ip = slbServer.getIp();
            final NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(ip));
            PushConfCallable callable = new PushConfCallable(batchId, ip, nginxClient, false);
            Future<NginxResponse> future = completionService.submit(callable);
            allFutures.put(callable, future);
        }

        int successCount = 0;
        int successThreshold = minSuccPctToComplete.get();
        int minSuccessCountToComplete = (int) Math.ceil(taskSize * successThreshold * 0.01);
        long start = System.nanoTime();

        try {
            monitorThreadPoolExecutor.submit(new PushConfMonitorRunnable(batchId, allFutures));
        } catch (Exception ex) {
            logger.warn("Failed to start push conf monitor task.", ex);
        }

        List<NginxResponse> failureResponses = new ArrayList<>();
        for (int received = 0; received < taskSize; received++) {
            Future<NginxResponse> result = completionService.take();
            NginxResponse response = result.get();
            if (response.getSucceed()) {
                ++successCount;
                if (successCount >= minSuccessCountToComplete) {
                    long cost = TimerUtils.nanoToMilli(System.nanoTime() - start);
                    logger.info("[[FutureTaskId={}]][Push Conf] Update conf succeeded. Threshold={}% Success/Received/Total={}/{}/{} Cost: {}ms\nSample response from {}:\n{}",
                            batchId, successThreshold, successCount, received + 1, taskSize, cost, response.getServerIp(), response.toString());
                    return response;
                }
            } else {
                failureResponses.add(response);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[Push Conf] Update conf failed. Result:\n");
        sb.append("Failed:\n");
        for (NginxResponse nr : failureResponses) {
            sb.append(ObjectJsonWriter.write(nr)).append('\n');
        }
        logger.error(sb.toString());
        throw new Exception(sb.toString());
    }

    @Override
    public void rollbackAllConf(List<SlbServer> slbServers) throws Exception {
        Map<String, FutureTask<NginxResponse>> futureTasks = new HashMap<>();

        logger.info("[Rollback Conf] Assign rolling back conf job to slb servers");
        for (SlbServer slbServer : slbServers) {
            final String ip = slbServer.getIp();
            final NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            FutureTask<NginxResponse> futureTask = new FutureTask<>(new Callable<NginxResponse>() {
                @Override
                public NginxResponse call() throws Exception {
                    logger.info("[Rollback Conf] Start Rollback conf on server " + ip + ".");
                    NginxResponse response = nginxClient.update(true);
                    logger.info("[Rollback Conf] Finish Rollback conf on server " + ip + ". Success:" + response.getSucceed() + ".");
                    return response;
                }
            });
            threadPoolExecutor.execute(futureTask);
            futureTasks.put(ip, futureTask);
        }

        logger.info("[Rollback Conf] Reading result from rolling back conf result.");
        long start = System.nanoTime();

        int sleepInterval = nginxFutureTimeout.get() / nginxFutureCheckTimes.get();
        Set<String> finishedServer = new HashSet<>();
        List<NginxResponse> result = new ArrayList<>();
        for (int i = 0; i <= nginxFutureCheckTimes.get(); i++) {
            for (String sip : futureTasks.keySet()) {
                FutureTask<NginxResponse> futureTask = futureTasks.get(sip);
                if (futureTask.isDone() && !finishedServer.contains(sip)) {
                    finishedServer.add(sip);
                    NginxResponse response = futureTask.get();
                    result.add(response);
                    if (response.getSucceed()) {
                        logger.info("[Rollback Conf] Use success result from server " + sip + ". Cost:" + TimerUtils.nanoToMilli(System.nanoTime() - start) + "ms. Result:\n" + response.toString());
                        return;
                    }
                }
            }
            if (finishedServer.size() == futureTasks.size()) {
                break;
            }
            try {
                Thread.sleep(sleepInterval);
            } catch (Exception e) {
                logger.warn("[Rollback Conf] Rollback conf sleep interrupted.");
            }
        }

        StringBuilder sb = new StringBuilder();
        boolean success = true;
        sb.append("[Rollback Conf] Rollback conf finished. Cost:" + TimerUtils.nanoToMilli(System.nanoTime() - start) + "ms. Result:\n");
        for (NginxResponse nr : result) {
            success = success & nr.getSucceed().booleanValue();
            sb.append(nr.getServerIp()).append(':').append(nr.toString()).append('\n');
        }
        sb.append("Timeout:\n");
        for (SlbServer server : slbServers) {
            if (!finishedServer.contains(server.getIp())) {
                success = false;
                sb.append(server.getIp()).append(',');
            }
        }

        if (success) {
            logger.info(sb.toString());
        } else {
            logger.error(sb.toString());
        }
    }

    private static String buildRemoteUrl(String ip) {
        return "http://" + ip + ":" + adminServerPort.get();
    }

    class PushConfCallable implements Callable<NginxResponse> {
        private final String ip;
        private final NginxClient client;
        private final String fid;
        private final boolean refresh;
        private final long createNanoTime;
        private long startNanoTime;
        private long finishNanoTime;
        private long cost;

        PushConfCallable(String batchId, String ip, NginxClient client, boolean refresh) {
            this.refresh = refresh;
            this.ip = ip;
            this.client = client;
            this.fid = batchId;
            this.createNanoTime = System.nanoTime();
        }

        @Override
        public NginxResponse call() throws Exception {
            startNanoTime = System.nanoTime();

            logger.info("[[FutureTaskId=" + fid + "]][Push Conf] Start updating conf on server " + ip + "." + "Fid:" + fid + ";Refresh:" + refresh);
            long start = System.nanoTime();
            NginxResponse response = client.update(refresh);
            finishNanoTime = System.nanoTime();
            cost = TimerUtils.nanoToMilli(finishNanoTime - start);
            if (!response.getSucceed()) {
                logger.warn("[[FutureTaskId=" + fid + "]][Push Conf] Failed to update conf on server " + ip + ". Cause:" + response.toString()
                        + ". Cost:" + cost);
            } else {
                logger.info("[[FutureTaskId=" + fid + "]][Push Conf] Finish updating conf on server " + ip + ". Success:" + response.getSucceed()
                        + ". Cost:" + cost);
            }
            return response;
        }
    }

    class PushConfMonitorRunnable implements Runnable {

        private static final int MONITOR_INTERVAL = 20;

        private final String batchId;
        private final Map<PushConfCallable, Future<NginxResponse>> futures;

        PushConfMonitorRunnable(String batchId, Map<PushConfCallable, Future<NginxResponse>> futures) {
            this.batchId = batchId;
            this.futures = futures;
        }

        @Override
        public void run() {
            // Wait until all the futures are done.
            int finishedCount = 0;
            PushConfCallable[] callables = futures.keySet().toArray(new PushConfCallable[futures.size()]);
            boolean[] finishedFlags = new boolean[futures.size()];
            long batchStartNanoTime = Long.MAX_VALUE, batchFinishNanoTime = 0;
            while (finishedCount < callables.length) {
                for (int i = 0; i < callables.length; ++i) {
                    PushConfCallable callable = callables[i];
                    if (!finishedFlags[i] && futures.get(callables[i]).isDone()) {
                        finishedFlags[i] = true;
                        ++finishedCount;
                        batchStartNanoTime = Math.min(batchStartNanoTime, callable.createNanoTime);
                        batchFinishNanoTime = Math.max(batchFinishNanoTime, callable.finishNanoTime);
                    }
                }

                try {
                    Thread.sleep(MONITOR_INTERVAL);
                } catch (InterruptedException e) {
                }
            }

            // Build result message.
            StringBuilder logBuilder = new StringBuilder();
            long batchCost = TimerUtils.nanoToMilli(batchFinishNanoTime - batchStartNanoTime);
            logBuilder.append(String.format("[[FutureTaskId=%s]] [Push Conf Batch] TotalCost=%dms", batchId, batchCost));
            for (PushConfCallable callable : callables) {
                logBuilder.append("\n");
                try {
                    NginxResponse response = futures.get(callable).get();
                    logBuilder.append(String.format("%s: Queue=%dms, Cost=%dms Success=%s", callable.ip,
                            TimerUtils.nanoToMilli(Math.max(0, callable.startNanoTime - batchStartNanoTime)),
                            callable.cost, response.getSucceed().toString()));
                    if (!response.getSucceed()) {
                        logBuilder.append(String.format(" OutMsg=%s ErrMsg=%s", response.getOutMsg(), response.getErrMsg()));
                    }
                } catch (Exception e) {
                    logBuilder.append(String.format("%s: Cost=%dms NoResponse Exception=%s %s", callable.ip, callable.cost,
                            e.getClass().getName(), e.getMessage()));
                }
            }

            String message = logBuilder.toString();
            long timeoutThreshold = batchTimeoutThreshold.get();
            if (batchCost >= timeoutThreshold) {
                logger.warn(message);
            } else {
                logger.info(message);
            }
        }
    }
}

