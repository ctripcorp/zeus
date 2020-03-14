package com.ctrip.zeus.service.ipblock.impl;

import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.model.model.SlbServer;
import com.ctrip.zeus.model.nginx.NginxResponse;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.ipblock.BlackIpListEntity;
import com.ctrip.zeus.service.ipblock.IpBlackListService;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

import static com.ctrip.zeus.service.ipblock.impl.IpBlockKeyConsts.GLOBAL_KEY;

@Service("ipBlackListService")
public class IpBlackListServiceImpl implements IpBlackListService {
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Autowired
    private AppService appService;
    @Resource
    private PropertyService propertyService;


    private ThreadPoolExecutor threadPoolExecutor;

    private static DynamicIntProperty threadPoolSize = DynamicPropertyFactory.getInstance().getIntProperty("ipblock.thread.pool.max.size", 200);
    private static DynamicIntProperty threadPoolCoreSize = DynamicPropertyFactory.getInstance().getIntProperty("ipblock.thread.pool.core.size", 30);
    private static DynamicIntProperty threadPoolKeepAlive = DynamicPropertyFactory.getInstance().getIntProperty("ipblock.thread.pool.keepalive", 60);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);
    private static DynamicIntProperty persent = DynamicPropertyFactory.getInstance().getIntProperty("ipblock.success.percent", 70);
    private static DynamicBooleanProperty onlySupportForInternet = DynamicPropertyFactory.getInstance().getBooleanProperty("ipblock.only.for.public", false);
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String splitFlag = "_";

    public IpBlackListServiceImpl() {
        threadPoolExecutor = new ThreadPoolExecutor(threadPoolCoreSize.get(), threadPoolSize.get(), threadPoolKeepAlive.get(), TimeUnit.SECONDS, new LinkedBlockingQueue<>(20000));
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
    }

    @Override
    public void setLocalIpBlackList(String data) throws Exception {
        IpBlockClient.getLocalClient().pushIpListToNginx(data);
    }

    @Override
    public Map<String, Boolean> setIpBlackList(BlackIpListEntity entity) throws Exception {
        Set<Long> slbIds = new HashSet<>();
        if (entity == null) return null;
        if (onlySupportForInternet.get()) {
            List<Long> ids = propertyService.queryTargets("zone", "外网", "slb");
            slbIds.addAll(ids);
        } else {
            slbIds = slbCriteriaQuery.queryAll();
        }
        Map<Long, String> slbData = new HashMap<>();
        for (Long slbId : slbIds) {
            Set<String> appIds = appService.getAppIdsBySlbId(slbId);
            StringBuilder tmp = new StringBuilder(123);
            boolean first = true;
            if (entity.getGlobal() != null && entity.getGlobal().size() > 0) {
                tmp.append(buildData(GLOBAL_KEY, entity.getGlobal()));
                first = false;
            }
            if (entity.getGlobalList() != null) {
                for (String key : entity.getGlobalList().keySet()) {
                    if (key.startsWith(GLOBAL_KEY)) {
                        if (!first) {
                            tmp.append("\n");
                        } else {
                            first = false;
                        }
                        tmp.append(buildData(key, entity.getGlobalList().get(key)));
                    }
                }
            }

            if (entity.getAppList() != null) {
                for (String key : entity.getAppList().keySet()) {
                    String[] keySplit = key.split(splitFlag);
                    String appId = keySplit[0];
                    if (appIds.contains(appId)) {
                        if (!first) {
                            tmp.append("\n");
                        } else {
                            first = false;
                        }
                        tmp.append(buildData(key, entity.getAppList().get(key)));
                    }
                }
            }
            slbData.put(slbId, tmp.toString());
        }

        List<Future<NginxResponse>> futures = new ArrayList<>();
        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(slbIds.toArray(new Long[slbIds.size()]));
        for (Slb slb : slbMap.getOnlineMapping().values()) {
            String data = slbData.get(slb.getId());
            if (Strings.isNullOrEmpty(data)) {
                continue;
            }
            for (SlbServer slbserver : slb.getSlbServers()) {
                Future<NginxResponse> future = threadPoolExecutor.submit(new PushCallable(buildRemoteUrl(slbserver.getIp()), data, slbserver.getIp()));
                futures.add(future);
            }
        }
        Map<String, Boolean> responses = new HashMap<>();
        int successCount = 0;
        int sum = futures.size();
        while (futures.size() > 0) {
            Iterator<Future<NginxResponse>> i = futures.iterator();
            while (i.hasNext()) {
                Future<NginxResponse> f = i.next();
                if (f.isDone()) {
                    i.remove();
                    NginxResponse res = f.get();
                    responses.put(res.getServerIp(), res.getSucceed());
                    if (res.getSucceed() != null && res.getSucceed()) {
                        successCount++;
                    }
                }
            }
        }

        if (futures.size() > 0 && (successCount < sum * persent.get() / 100 || successCount < 1)) {
            throw new Exception("Push Ip Black List Failed.Response:" + ObjectJsonWriter.write(responses));
        }
        logger.info("Push Ip Black List Result:" + ObjectJsonWriter.write(responses));
        return responses;
    }

    private static String buildRemoteUrl(String ip) {
        return "http://" + ip + ":" + adminServerPort.get();
    }

    private String buildData(String key, List<String> ips) throws Exception {
        if (key == null) {
            return "";
        }
        if (ips == null) {
            return "";
        }
        if (key.equalsIgnoreCase(GLOBAL_KEY)) {
            key = GLOBAL_KEY;
        } else if (key.toLowerCase().startsWith(GLOBAL_KEY)) {
            key = key;
        } else {
            String[] tmp = key.split(splitFlag);
            App app = appService.getAppByAppid(tmp[0]);
            if (app == null) {
                return "";
            }
        }
        return key + "=" + Joiner.on(",").join(ips);
    }


    class PushCallable implements Callable<NginxResponse> {
        private String url;
        private String data;
        private String ip;

        PushCallable(String url, String data, String ip) {
            this.url = url;
            this.data = data;
            this.ip = ip;
        }

        @Override
        public NginxResponse call() throws Exception {
            Boolean result = IpBlockClient.getClient(url).pushIpList(data);
            NginxResponse res = new NginxResponse();
            res.setServerIp(ip).setOutMsg(url).setSucceed(result);
            logger.info("Push Ip Black List Status:" + result + ";URL:" + url);
            return res;
        }
    }
}
