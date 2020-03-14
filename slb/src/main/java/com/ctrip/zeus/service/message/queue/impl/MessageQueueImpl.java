package com.ctrip.zeus.service.message.queue.impl;

import com.ctrip.zeus.dao.entity.MessageQueueExample;
import com.ctrip.zeus.dao.mapper.MessageQueueMapper;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.Consumer;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.message.queue.MessageType;
import com.ctrip.zeus.service.message.queue.MessageTypeConsts;
import com.ctrip.zeus.support.C;
import com.ctrip.zeus.util.EnvHelper;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fanqq on 2016/9/6.
 */
@Service("messageQueue")
public class MessageQueueImpl implements MessageQueue {
    @Resource
    private MessageQueueMapper messageQueueMapper;
    @Resource
    private ConfigHandler configHandler;
    @Resource
    private DbLockFactory dbLockFactory;

    private final DynamicIntProperty poolSize = DynamicPropertyFactory.getInstance().getIntProperty("message.queue.service.thread.pool.size", 10);
    private final DynamicIntProperty fetchInterval = DynamicPropertyFactory.getInstance().getIntProperty("message.queue.service.fetch.interval", 1000);
    private List<Consumer> consumers = new ArrayList<>();
    private ExecutorService executorService;
    private Date startTime = new Date();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private FetchThread fetchThread = new FetchThread();

    @PostConstruct
    protected void init() {
        fetchThread.start();
        executorService = new ThreadPoolExecutor(poolSize.get() / 2, poolSize.get(), 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void addConsummer(Consumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void produceMessage(MessageType type, Long targetId, String targetData) throws Exception {
        messageQueueMapper.insert(com.ctrip.zeus.dao.entity.MessageQueue.builder()
                .type(type.toString())
                .createTime(new Date())
                .status("TODO")
                .targetId(targetId == null ? 0L : targetId)
                .targetData(targetData == null ? "" : targetData)
                .build());

        logger.info("[[messageType=" + type.toString() + ",messageStatus=produce]][MessageQueueService] Produce Message Success. type:"
                + type.toString() + ";targetId:" + targetId + ";targetData:" + targetData);
    }

    @Override
    public void produceMessage(String type, Long targetId, String targetData) throws Exception {
        if (type == null) return;
        type = type.replaceAll("[/]+$", "");
        messageQueueMapper.insert(com.ctrip.zeus.dao.entity.MessageQueue.builder()
                .type(type)
                .createTime(new Date())
                .status("TODO")
                .targetId(targetId == null ? 0L : targetId)
                .targetData(targetData == null ? "" : targetData)
                .build());
        logger.info("[[messageType=" + type + ",messageStatus=produce]][MessageQueueService] Produce Message Success. type:" + type
                + ";targetId:" + targetId + ";targetData:" + targetData);
    }

    public void fetchMessage() throws Exception {
        Map<String, List<Message>> res = new HashMap<>();
        boolean isLocked = false;
        DistLock lock = dbLockFactory.newLock("MessageQueue");
        try {
            if (isLocked = lock.tryLock()) {
                List<com.ctrip.zeus.dao.entity.MessageQueue> list = messageQueueMapper.selectByExample(new MessageQueueExample().
                        createCriteria().
                        andStatusEqualTo("TODO").
                        andCreateTimeGreaterThanOrEqualTo(startTime).
                        example());

                logger.info("[MessageQueueService] Fetch Messages success. StartTime:" + startTime.toString() + ";count:" + (list == null ? 0 : list.size()));

                if (list != null && list.size() > 0) {
                    for (com.ctrip.zeus.dao.entity.MessageQueue messageQueue : list) {
                        if (!res.containsKey(messageQueue.getType())) {
                            res.put(messageQueue.getType(), new ArrayList<Message>());
                        }
                        res.get(messageQueue.getType()).add(C.toMessage(messageQueue));
                        messageQueue.setStatus("DONE");
                        messageQueue.setPerformer(LocalInfoPack.INSTANCE.getIp());
                    }
                    messageQueueMapper.updateById(list);
                    startTime = list.get(list.size() - 1).getCreateTime();
                    logger.info("[MessageQueueService] Finish Fetch Messages. message count:" + list.size());
                }
            }
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }

        logger.info("[MessageQueueService] Start process consumers.");
        for (Consumer consumer : consumers) {
            logger.info("[MessageQueueService] Invoke Consumer : " + consumer.getClass().getSimpleName());
            if (configHandler.getEnable("message.consumer." + consumer.getClass().getSimpleName() + ".enable", true)) {
                executorService.execute(new ConsumerExecutor(consumer, res));
            }
        }
    }

    @PreDestroy
    protected void preDestroy() {
        fetchThread.shutdown();
        executorService.shutdown();
    }

    class ConsumerExecutor implements Runnable {

        Consumer consumer;
        Map<String, List<Message>> messageMap;

        ConsumerExecutor(Consumer consumer, Map<String, List<Message>> messageMap) {
            this.consumer = consumer;
            this.messageMap = messageMap;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            logger.info("[[consumer=" + consumer.getClass().getSimpleName() + ",messageStatus=processing]][ConsumerExecutor] Consumer Started. Consumer:" + consumer.getClass().getSimpleName());
            try {
                for (String type : messageMap.keySet()) {
                    executeConsumer(type);
                }
            } catch (Exception e) {
                logger.error("[[consumerStatus=failed]][ConsumerExecutor] Consumer Execute Failed.Consumer:" + consumer.getClass().getSimpleName(), e);
            }
            logger.info("[[consumer=" + consumer.getClass().getSimpleName() + ",messageStatus=processing]][ConsumerExecutor] Consumer Finished. cost:" + (System.currentTimeMillis() - start));
        }

        private void executeConsumer(String type) {
            switch (type) {
                case "/api/group/new":
                case "/api/group/related/new":
                case "/api/vgroup/new":
                    consumer.onNewGroup(messageMap.get(type));
                    break;

                case "/api/group/delegate/new":
                    consumer.onNewDelegateGroup(messageMap.get(type));
                    break;
                case "/api/group/update":
                case "/api/group/addMember":
                case "/api/group/updateMember":
                case "/api/group/removeMember":
                case "/api/vgroup/update":
                case "/api/group/updateCheckUri":
                case "/api/group/bindVs":
                case "/api/group/unbindVs":
                    consumer.onUpdateGroup(messageMap.get(type));
                    break;
                case "/api/group/delete":
                case "/api/vgroup/delete":
                    consumer.onDeleteGroup(messageMap.get(type));
                    break;
                case "/api/policy/new":
                    consumer.onNewPolicy(messageMap.get(type));
                    break;
                case "/api/policy/update":
                    consumer.onUpdatePolicy(messageMap.get(type));
                    break;
                case "/api/policy/delete":
                    consumer.onDeletePolicy(messageMap.get(type));
                    break;
                case "/api/dr/new":
                    consumer.onNewDr(messageMap.get(type));
                    break;
                case "/api/dr/update":
                    consumer.onUpdateDr(messageMap.get(type));
                    break;
                case "/api/dr/delete":
                    consumer.onDeleteDr(messageMap.get(type));
                    break;
                case "/api/vs/new":
                case "/api/route/vs/new":
                    consumer.onNewVs(messageMap.get(type));
                    break;
                case "/api/vs/update":
                case "/api/vs/addDomain":
                case "/api/vs/removeDomain":
                    consumer.onUpdateVs(messageMap.get(type));
                    break;
                case "/api/vs/delete":
                    consumer.onDeleteVs(messageMap.get(type));
                    break;
                case "/api/slb/new":
                    consumer.onNewSlb(messageMap.get(type));
                    break;
                case "/api/slb/update":
                case "/api/slb/addServer":
                case "/api/slb/removeServer":
                    consumer.onUpdateSlb(messageMap.get(type));
                    break;
                case "/api/slb/delete":
                    consumer.onDeleteSlb(messageMap.get(type));
                    break;
                case "/api/op/pullIn":
                case "/api/op/pullOut":
                    consumer.onOpsPull(messageMap.get(type));
                    break;
                case "/api/op/upMember":
                case "/api/op/downMember":
                    consumer.onOpsMember(messageMap.get(type));
                    break;
                case "/api/op/raise":
                case "/api/op/fall":
                    consumer.onOpsHealthy(messageMap.get(type));
                    break;
                case "/api/op/upServer":
                case "/api/op/downServer":
                    consumer.onOpsServer(messageMap.get(type));
                    break;
                case "/api/activate/group":
                    consumer.onActivateGroup(messageMap.get(type));
                    break;
                case "/api/activate/policy":
                    consumer.onActivatePolicy(messageMap.get(type));
                    break;
                case "/api/activate/dr":
                    consumer.onActivateDr(messageMap.get(type));
                    break;
                case "/api/activate/vs":
                    consumer.onActivateVs(messageMap.get(type));
                    break;
                case "/api/activate/slb":
                    consumer.onActivateSlb(messageMap.get(type));
                    break;
                case "/api/deactivate/group":
                    consumer.onDeactivateGroup(messageMap.get(type));
                    break;
                case "/api/deactivate/policy":
                    consumer.onDeactivatePolicy(messageMap.get(type));
                    break;
                case "/api/deactivate/dr":
                    consumer.onDeactivateDr(messageMap.get(type));
                    break;
                case "/api/deactivate/vs":
                    consumer.onDeactivateVs(messageMap.get(type));
                    break;
                case "/api/deactivate/slb":
                    consumer.onDeactivateSlb(messageMap.get(type));
                    break;
                case "/api/auth/apply":
                    consumer.onAuthApply(messageMap.get(type));
                    break;
                case "/api/cert/sandbox/install":
                case "/api/cert/sandbox/uninstall":
                    consumer.onSandBox(messageMap.get(type));
                    break;
                case MessageTypeConsts.RELOAD:
                    consumer.onReload(messageMap.get(type));
                    break;
                default:
                    if (type.startsWith("/api/flow/slb/creating")) {
                        consumer.onSlbCreatingFlow(messageMap.get(type));
                        break;
                    }
                    if (type.startsWith("/api/flow/slb/sharding")) {
                        consumer.onSlbShardingFlow(messageMap.get(type));
                        break;
                    }
                    if (type.startsWith("/api/cert/")) {
                        consumer.onCertOperation(messageMap.get(type));
                        break;
                    }
                    if (type.startsWith("/api/flow/slb/destroy")) {
                        consumer.onSlbShardingFlow(messageMap.get(type));
                        break;
                    }
                    if (type.startsWith("/api/flow/vs/merge")) {
                        consumer.onSlbShardingFlow(messageMap.get(type));
                        break;
                    }
                    if (type.startsWith("/api/flow/vs/split")) {
                        consumer.onSlbShardingFlow(messageMap.get(type));
                        break;
                    }
            }
        }
    }

    class FetchThread extends Thread {
        AtomicBoolean isRunning = new AtomicBoolean(true);

        public void shutdown() {
            isRunning.set(false);
        }

        public void run() {
            while (true) {
                if (!isRunning.get()) {
                    return;
                }
                try {
                    if (EnvHelper.portal() && configHandler.getEnable("message.queue", null, null, null, true)) {
                        fetchMessage();
                    }

                } catch (Throwable e) {
                    logger.error("Message Queue Execute Failed.", e);
                }
                try {
                    Thread.sleep(fetchInterval.get());
                } catch (Exception e) {
                    logger.error("Sleep interrupted.", e);
                }
            }
        }
    }

}

