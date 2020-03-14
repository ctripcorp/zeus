package com.ctrip.zeus.service.tools.check.impl;

import com.ctrip.zeus.model.tools.Domain;
import com.ctrip.zeus.model.tools.VsPing;
import com.ctrip.zeus.model.tools.VsPingList;
import com.ctrip.zeus.restful.resource.tools.CheckerClient;
import com.ctrip.zeus.service.tools.check.PingVsVpnService;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
@Service("pingVsVpnService")
public class PingVsVpnServiceImpl implements PingVsVpnService {
    private final Logger logger = LoggerFactory.getLogger(CheckClientServiceImpl.class);
    private static DynamicIntProperty pingFutureTimeout = DynamicPropertyFactory.getInstance().getIntProperty("ping.client.future.timeout", 2000);
    private static DynamicIntProperty pingFutureCheckTimes = DynamicPropertyFactory.getInstance().getIntProperty("ping.client.future.check.times", 10);
    private static DynamicIntProperty pingFutureCheckQueueSize = DynamicPropertyFactory.getInstance().getIntProperty("ping.client.future.queue.size", 1000);

    private ThreadPoolExecutor threadPoolExecutor;

    PingVsVpnServiceImpl() {
        threadPoolExecutor = new ThreadPoolExecutor(1, 40, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public Map<String, VsPing> pingVses(VsPingList vses, int timeOut) throws ExecutionException, InterruptedException {
        HashMap<String, VsPing> responses = new HashMap<>();

        HashMap<String, FutureTask<String>> futureTasks = new HashMap<>();

        if (threadPoolExecutor.getQueue().size() > pingFutureCheckQueueSize.get()) {
            logger.error("Ping queue size is bigger than " + pingFutureCheckQueueSize.get());
            return responses;
        }

        List<VsPing> pingList = vses.getVses();
        // Get domains count
        int domainCount=0;
        for(VsPing ping: pingList){
            domainCount+=ping.getDomains().size();
        }

        for (final VsPing target : pingList) {
            for (Domain domain : target.getDomains()) {
                final String domainName = domain.getName();

                FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        logger.info("Start Ping:" + domainName);

                        String response = CheckerClient.CheckDomainVpn(domainName);
                        return response;
                    }
                });

                String key = target.getVsId()+"/"+domainName;
                threadPoolExecutor.execute(futureTask);
                futureTasks.put(key, futureTask);
            }
        }

        int sleepInterval = pingFutureTimeout.get() + timeOut / pingFutureCheckTimes.get();
        Set<String> finishedServer = new HashSet<>();

        for (int i = 0; i <= pingFutureCheckTimes.get(); i++) {
            for (String sip : futureTasks.keySet()) {
                FutureTask<String> futureTask = futureTasks.get(sip);
                if (futureTask.isDone() && !finishedServer.contains(sip)) {
                    finishedServer.add(sip);
                    String response = futureTask.get();

                    String[] pingArray = sip.split("/");

                    VsPing ping = new VsPing();

                    Domain domain = new Domain();
                    domain.setName(pingArray[1]);
                    domain.setIp(response);

                    ping.setVsId(Long.parseLong(pingArray[0]));
                    ping.addDomain(domain);
                    responses.put(sip, ping);
                }
            }

            if (finishedServer.size() == domainCount) {
                return responses;
            }
            try {
                Thread.sleep(sleepInterval);
            } catch (Exception e) {
                logger.warn("Ping domain sleep interrupted.");
            }
        }

        return responses;
    }
}
