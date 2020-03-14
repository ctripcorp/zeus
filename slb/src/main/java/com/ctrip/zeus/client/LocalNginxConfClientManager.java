package com.ctrip.zeus.client;

import com.ctrip.zeus.model.nginx.SlbConfResponse;
import com.ctrip.zeus.model.nginx.VirtualServerConfResponse;
import com.ctrip.zeus.nginx.LocalSlbConfResponse;
import com.ctrip.zeus.service.model.handler.SlbQuery;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @Discription
 **/
@Service("localNginxConfClientManager")
public class LocalNginxConfClientManager {

    @Resource
    private SlbQuery slbQuery;

    private final Logger logger = LoggerFactory.getLogger(LocalNginxConfClientManager.class);

    private final static DynamicIntProperty REQ_TIMEOUT = DynamicPropertyFactory.getInstance().getIntProperty("local.nginx.client.req.timeout", 5000);
    private final DynamicIntProperty RETRY_TIMES = DynamicPropertyFactory.getInstance().getIntProperty("local.nginx.client.retry.times", 3);
    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    private static LoadingCache<String, LocalNginxConfClient> clientCache = CacheBuilder.newBuilder().maximumSize(10)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, LocalNginxConfClient>() {
                       @Override
                       public LocalNginxConfClient load(String url) throws Exception {
                           return new LocalNginxConfClient(url, REQ_TIMEOUT.get());
                       }
                   }
            );

    /*
     * @Description: Fetch nginx.conf from slb agent server.
     * @return: nginx.conf's content. In case of failure, return null
     **/
    public String getNginxConf(Long slbId) throws Exception {
        return randomFetchFromServer(ip -> getClient(ip).getNginxConf(), slbQuery.getSlbIps(slbId), "nginx conf");
    }

    public VirtualServerConfResponse getVsConf(Long slbId, Long vsId) throws Exception {
        return randomFetchFromServer(ip -> getClient(ip).getVsConf(vsId), slbQuery.getSlbIps(slbId), "vs conf");
    }

    public LocalSlbConfResponse getSlbConf(Long slbId) throws Exception {
        return randomFetchFromServer(ip -> getClient(ip).getSlbConf(), slbQuery.getSlbIps(slbId), "slb conf");
    }

    private <R> R randomFetchFromServer(Function<String, R> callable, List<String> ips, String target) {
        if (ips.size() > 0) {
            Collections.shuffle(ips);
            int retry = RETRY_TIMES.get();
            int pos = 0;
            while (retry-- > 0) {
                String ip = ips.get(pos);

                R result = callable.apply(ip);
                if (result != null) {
                    return result;
                }
                logger.warn("Fetching " + target + " from " + ip + " failed.");
                pos = (pos + 1) % ips.size();
            }
        }
        return null;
    }

    public Map<String, SlbConfResponse> getSlbConfFromAllServers(Long slbId) throws Exception {
        List<String> ips = slbQuery.getSlbIps(slbId);
        if (ips.size() > 0) {
            Map<String, SlbConfResponse> result = new HashMap<>();
            CompletionService<LocalSlbConfResponse> completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(10));
            for (String ip : ips) {
                completionService.submit(() -> this.getClient(ip).getSlbConf());
            }

            int fetchCount = 0;
            while (fetchCount < ips.size()) {
                Future<LocalSlbConfResponse> future = completionService.poll((long) REQ_TIMEOUT.get(), TimeUnit.MILLISECONDS);
                if (future == null) {
                    continue;
                }
                LocalSlbConfResponse response = future.get();
                if (response != null && response.getIp() != null) {
                    result.put(response.getIp(), response.getSlbConfResponse());
                }
                fetchCount++;
            }

            return result;
        }

        return new HashMap<>();
    }

    private LocalNginxConfClient getClient(String ip) {
        return clientCache.getUnchecked("http://" + ip + ":" + adminServerPort.get());
    }
}
