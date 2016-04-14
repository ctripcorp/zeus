package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.NginxClient;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbServer;
import com.ctrip.zeus.nginx.RollingTrafficStatus;
import com.ctrip.zeus.nginx.TrafficStatusHelper;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
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
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private RollingTrafficStatus rollingTrafficStatus;

    private ThreadPoolExecutor threadPoolExecutor;

    NginxServiceImpl() {
        threadPoolExecutor = new ThreadPoolExecutor(10, 20, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    private final Logger logger = LoggerFactory.getLogger(NginxServiceImpl.class);


    @Override
    public List<NginxResponse> update(String nginxConf, Map<Long, VsConfData> vsConfDataMap, Set<Long> cleanVsIds, DyUpstreamOpsData[] dyups, boolean needReload, boolean needTest, boolean needDyups) throws Exception {
        List<NginxResponse> response = new ArrayList<>();
        try {
            //1. update config
            nginxConfOpsService.updateNginxConf(nginxConf);
            Long cleanConfFlag = nginxConfOpsService.cleanAndUpdateConf(cleanVsIds, vsConfDataMap);
            //2. test reload or dyups, if needed. undo config if failed.
            if (needTest) {
                NginxResponse res = nginxOpsService.test();
                response.add(res);
                if (!res.getSucceed()) {
                    logger.error("[NginxService]update failed.Test conf failed.");
                    nginxConfOpsService.undoUpdateNginxConf();
                    nginxConfOpsService.undoCleanAndUpdateConf(cleanVsIds, vsConfDataMap, cleanConfFlag);
                }
            }
            if (needReload) {
                NginxResponse res = nginxOpsService.reload();
                response.add(res);
                if (!res.getSucceed()) {
                    logger.error("[NginxService]update failed.Reload conf failed.");
                    nginxConfOpsService.undoUpdateNginxConf();
                    nginxConfOpsService.undoCleanAndUpdateConf(cleanVsIds, vsConfDataMap, cleanConfFlag);
                }
            }
            if (needDyups) {
                List<NginxResponse> dyupsRes = nginxOpsService.dyups(dyups);
                response.addAll(dyupsRes);
                for (NginxResponse res : dyupsRes) {
                    if (!res.getSucceed()) {
                        logger.error("[NginxService]update failed.Dyups conf failed.");
                        nginxConfOpsService.undoUpdateNginxConf();
                        nginxConfOpsService.undoCleanAndUpdateConf(cleanVsIds, vsConfDataMap, cleanConfFlag);
                        break;
                    }
                }
            }
        } finally {
            logger.info("Nginx update response. Responses:" + response.toString());
        }
        return response;
    }

    @Override
    public NginxResponse refresh(String nginxConf, Map<Long, VsConfData> vsConfDataMap, boolean reload) throws Exception {
        nginxConfOpsService.updateNginxConf(nginxConf);
        Long updateAllFlag = nginxConfOpsService.updateAll(vsConfDataMap);
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
                logger.error("[Push Conf] Update conf requests all failed. Costs:" + TimerUtils.nanoToMilli(System.nanoTime() - start) + "ms. Results:\n" + sb.toString());
                throw new Exception("Update conf requests all failed.");
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

    @Override
    public List<ReqStatus> getTrafficStatusBySlb(Long slbId, int count, boolean aggregatedByGroup,
                                                 boolean aggregatedBySlbServer) throws Exception {
        List<ReqStatus> result = getTrafficStatusBySlb(slbId, count);
        if (!(aggregatedByGroup && aggregatedBySlbServer)) {
            result = aggregateByKey(result, aggregatedByGroup, aggregatedBySlbServer, slbId);
        }
        if (aggregatedByGroup) {
            for (ReqStatus reqStatus : result) {
                if (reqStatus.getGroupId() != null && reqStatus.getGroupId() == -1L) {
                    reqStatus.setSlbId(slbId);
                    reqStatus.setGroupName("Not exist");
                    continue;
                }
                Group g = groupRepository.getById(reqStatus.getGroupId());
                if (g == null)
                    reqStatus.setGroupName("Not Found");
                else
                    reqStatus.setGroupName(g.getName());
                reqStatus.setSlbId(slbId);
            }
        } else {
            for (ReqStatus reqStatus : result) {
                reqStatus.setSlbId(slbId);
            }
        }
        return result;
    }

    private List<ReqStatus> getTrafficStatusBySlb(Long slbId, int count) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        List<ReqStatus> list = new ArrayList<>();
        for (SlbServer slbServer : slb.getSlbServers()) {
            NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            try {
                list.addAll(nginxClient.getTrafficStatus(System.currentTimeMillis() - 60 * 1000L, count).getStatuses());
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return list;
    }

    private List<ReqStatus> aggregateByKey(List<ReqStatus> raw, boolean group, boolean slbServer, Long slbId) {
        Map<String, ReqStatus> result = new ConcurrentHashMap<>();
        for (ReqStatus reqStatus : raw) {
            String key = genKey(reqStatus, group, slbServer);
            ReqStatus value = result.get(key);
            if (group) {
                result.put(key, TrafficStatusHelper.add(value, reqStatus, "", slbId, reqStatus.getGroupId(), null));
                continue;
            }
            if (slbServer) {
                result.put(key, TrafficStatusHelper.add(value, reqStatus, reqStatus.getHostName(), slbId, -1L, ""));
                continue;
            }
            result.put(key, TrafficStatusHelper.add(value, reqStatus, "", slbId, -1L, ""));
        }
        return new LinkedList<>(result.values());
    }

    private String genKey(ReqStatus reqStatus, boolean group, boolean slbServer) {
        String time = formatter.format(reqStatus.getTime());
        if (group)
            return time + "-" + reqStatus.getGroupId();
        if (slbServer)
            return time + "-" + reqStatus.getHostName();
        return time + "";
    }

    @Override
    public List<ReqStatus> getTrafficStatusBySlb(String groupName, Long slbId, int count) throws Exception {
        Slb slb = slbRepository.getById(slbId);
        List<ReqStatus> list = new ArrayList<>();
        for (SlbServer slbServer : slb.getSlbServers()) {
            NginxClient nginxClient = NginxClient.getClient(buildRemoteUrl(slbServer.getIp()));
            try {
                list.addAll(nginxClient.getTrafficStatusByGroup(System.currentTimeMillis() - 60 * 1000L, groupName, count).getStatuses());
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return list;
    }

    @Override
    public List<ReqStatus> getLocalTrafficStatus(Date time, int count) {
        LinkedList<TrafficStatus> l = (LinkedList<TrafficStatus>) rollingTrafficStatus.getResult();
        List<ReqStatus> result = new LinkedList<>();
        int size = l.size();
        TrafficStatus head = l.peekLast();
        // In case of time diff from server fetchers
        for (int i = 0; i < count + 1 && i < size; i++) {
            TrafficStatus ts = l.pollLast();
            if (formatter.format(time).equals(formatter.format(ts.getTime()))) {
                result.addAll(ts.getReqStatuses());
            }
        }
        if (result.size() == 0) {
            for (ReqStatus reqStatus : head.getReqStatuses()) {
                result.add(new ReqStatus().setGroupId(reqStatus.getGroupId())
                        .setGroupName(reqStatus.getGroupName())
                        .setSlbId(reqStatus.getSlbId()).setTime(time));
            }
        }
        return result;
    }

    @Override
    public List<ReqStatus> getLocalTrafficStatus(Date time, String groupName, int count) {
        LinkedList<TrafficStatus> l = (LinkedList<TrafficStatus>) rollingTrafficStatus.getResult();
        List<ReqStatus> result = new LinkedList<>();
        int size = l.size();
        for (int i = 0; i < count && i < size; i++) {
            for (ReqStatus reqStatus : l.pollLast().getReqStatuses()) {
                if (reqStatus.getGroupName().equalsIgnoreCase(groupName)) {
                    result.add(reqStatus);
                }
            }
        }
        return result;
    }

    private static String buildRemoteUrl(String ip) {
        return "http://" + ip + ":" + adminServerPort.get();
    }
}
