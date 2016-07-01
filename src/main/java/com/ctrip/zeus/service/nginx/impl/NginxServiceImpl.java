package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.nginx.handler.NginxConfOpsService;
import com.ctrip.zeus.service.nginx.handler.NginxOpsService;
import com.ctrip.zeus.util.TimerUtils;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Service("nginxService")
public class NginxServiceImpl implements NginxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NginxServiceImpl.class);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);
    private static DynamicIntProperty nginxFutureTimeout = DynamicPropertyFactory.getInstance().getIntProperty("nginx.client.future.timeout", 3000);
    private static DynamicIntProperty nginxFutureCheckTimes = DynamicPropertyFactory.getInstance().getIntProperty("nginx.client.future.check.times", 10);

    private final DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    @Resource
    private NginxOpsService nginxOpsService;
    @Resource
    private NginxConfOpsService nginxConfOpsService;

    private ThreadPoolExecutor threadPoolExecutor;

    NginxServiceImpl() {
        threadPoolExecutor = new ThreadPoolExecutor(10, 20, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
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
        Map<String, FutureTask<NginxResponse>> futureTasks = new HashMap<>();

        logger.info("[Push Conf] Assign updating conf job to slb servers.");
        for (SlbServer slbServer : slbServers) {
            final String ip = slbServer.getIp();
            final NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            FutureTask<NginxResponse> futureTask = new FutureTask<>(new Callable<NginxResponse>() {
                @Override
                public NginxResponse call() throws Exception {
                    logger.info("[Push Conf] Start updating conf on server " + ip + ".");
                    NginxResponse response = nginxClient.update(false);
                    if (!response.getSucceed()) {
                        logger.error("[Push Conf] Failed to update conf on server " + ip + ". Cause:" + response.toString());
                    } else {
                        logger.info("[Push Conf] Finish updating conf on server " + ip + ". Success:" + response.getSucceed() + ".");
                    }
                    return response;
                }
            });
            threadPoolExecutor.execute(futureTask);
            futureTasks.put(ip, futureTask);
        }

        logger.info("[Push Conf] Reading result from updating conf.");
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
                        logger.info("[Push Conf] Use success result from server " + sip + ". Cost:" + TimerUtils.nanoToMilli(System.nanoTime() - start) + "ms. Result:\n" + response.toString());
                        return response;
                    }
                }
            }

            if (finishedServer.size() == futureTasks.size()) {
                StringBuilder sb = new StringBuilder();
                for (NginxResponse nr : result) {
                    sb.append(nr.toString()).append('\n');
                }
                logger.error("[Push Conf] Update conf requests all failed. Check the correctness of the generating conf. Costs:" + TimerUtils.nanoToMilli(System.nanoTime() - start) + "ms. Results:\n" + sb.toString());
                throw new Exception("Update conf all failed. Cause: " + sb.toString());
            }

            try {
                Thread.sleep(sleepInterval);
            } catch (Exception e) {
                logger.warn("[Push Conf] Update conf sleep interrupted.");
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[Push Conf] Update conf failed. Result:\n");
        sb.append("Failed:\n");
        for (NginxResponse nr : result) {
            sb.append(nr.toString()).append('\n');
        }
        sb.append("Timeout:\n");
        for (SlbServer server : slbServers) {
            if (!finishedServer.contains(server.getIp())) {
                sb.append(server.getIp()).append(',');
            }
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
}
