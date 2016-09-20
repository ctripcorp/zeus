package com.ctrip.zeus.service.message.queue.impl;

import com.ctrip.zeus.dal.core.DefaultPageActiveDao;
import com.ctrip.zeus.dal.core.MessageQueueDao;
import com.ctrip.zeus.dal.core.MessageQueueDo;
import com.ctrip.zeus.dal.core.MessageQueueEntity;
import com.ctrip.zeus.queue.entity.Message;
import com.ctrip.zeus.server.LocalInfoPack;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.Consumer;
import com.ctrip.zeus.service.message.queue.MessageQueueService;
import com.ctrip.zeus.service.message.queue.MessageType;
import com.ctrip.zeus.support.C;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fanqq on 2016/9/6.
 */
@Service("messageQueueService")
public class MessageQueueServiceImpl implements MessageQueueService {
    @Resource
    private MessageQueueDao messageQueueDao;
    @Resource
    private ConfigHandler configHandler;

    private final DynamicIntProperty poolSize = DynamicPropertyFactory.getInstance().getIntProperty("message.queue.service.thread.pool.size", 10);
    private final DynamicIntProperty fetchInterval = DynamicPropertyFactory.getInstance().getIntProperty("message.queue.service.fetch.interval", 1000);
    private List<Consumer> consumers = new ArrayList<>();
    private ExecutorService executorService;
    private Date startTime = new Date();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private FetchThread fetchThread = new FetchThread();

    @PostConstruct
    private void init() {
        fetchThread.start();
        executorService = new ThreadPoolExecutor(poolSize.get() / 2, poolSize.get(), 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void addConsummer(Consumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void produceMessage(MessageType type, Long targetId, String targetData) throws Exception {
        MessageQueueDo messageQueueDo = new MessageQueueDo();
        messageQueueDo.setType(type.toString())
                .setCreateTime(new Date())
                .setStatus("TODO")
                .setTargetId(targetId == null ? 0L : targetId)
                .setTargetData(targetData == null ? "" : targetData);
        messageQueueDao.insert(messageQueueDo);
        logger.info("[[messageType=" + type.toString() + ",messageStatus=produce]][MessageQueueService] Produce Message Success. type:" + type.toString() + ";targetId:" + targetId + ";targetData:" + targetData);
    }

    public void fetchMessage() throws Exception {
        Map<String, List<Message>> res = new HashMap<>();
        List<MessageQueueDo> list = messageQueueDao.findByStatusAndAfterCreateTime("TODO", startTime, MessageQueueEntity.READSET_FULL);
        if (list != null && list.size() > 0) {
            logger.info("[MessageQueueService] Fetch Messages success. StartTime:" + startTime.toString() + ";count:" + list.size());
            for (MessageQueueDo messageQueueDo : list) {
                if (!res.containsKey(messageQueueDo.getType())) {
                    res.put(messageQueueDo.getType(), new ArrayList<Message>());
                }
                res.get(messageQueueDo.getType()).add(C.toMessage(messageQueueDo));
                messageQueueDo.setStatus("DONE");
                messageQueueDo.setPerformer(LocalInfoPack.INSTANCE.getIp());
            }
            messageQueueDao.updateById(list.toArray(new MessageQueueDo[list.size()]), MessageQueueEntity.UPDATESET_FULL);
            startTime = list.get(list.size() - 1).getCreateTime();

            logger.info("[MessageQueueService] Start process consumers.");
            for (Consumer consumer : consumers) {
                logger.info("[MessageQueueService] Invoke Consumer : " + consumer.getClass().getSimpleName());
                executorService.execute(new ConsumerExecutor(consumer, res));
            }

            logger.info("[MessageQueueService] Finish Fetch Messages. message count:" + list.size());
        }
    }

    @PreDestroy
    private void preDestroy() {
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
                    switch (MessageType.valueOf(type)) {
                        case NewGroup:
                            consumer.onNewGroup(messageMap.get(type));
                            break;
                        case UpdateGroup:
                            consumer.onUpdateGroup(messageMap.get(type));
                            break;
                        case DeleteGroup:
                            consumer.onDeleteGroup(messageMap.get(type));
                            break;
                        case NewVs:
                            consumer.onNewVs(messageMap.get(type));
                            break;
                        case UpdateVs:
                            consumer.onUpdateVs(messageMap.get(type));
                            break;
                        case DeleteVs:
                            consumer.onDeleteVs(messageMap.get(type));
                            break;
                        case NewSlb:
                            consumer.onNewSlb(messageMap.get(type));
                            break;
                        case UpdateSlb:
                            consumer.onUpdateSlb(messageMap.get(type));
                            break;
                        case DeleteSlb:
                            consumer.onDeleteSlb(messageMap.get(type));
                            break;
                        case OpsPull:
                            consumer.onOpsPull(messageMap.get(type));
                            break;
                        case OpsMember:
                            consumer.onOpsMember(messageMap.get(type));
                            break;
                        case OpsHealthy:
                            consumer.onOpsHealthy(messageMap.get(type));
                            break;
                        case OpsServer:
                            consumer.onOpsServer(messageMap.get(type));
                            break;
                        case ActivateGroup:
                            consumer.onActivateGroup(messageMap.get(type));
                            break;
                        case ActivateVs:
                            consumer.onActivateVs(messageMap.get(type));
                            break;
                        case ActivateSlb:
                            consumer.onActivateSlb(messageMap.get(type));
                            break;
                        case DeactivateGroup:
                            consumer.onDeactivateGroup(messageMap.get(type));
                            break;
                        case DeactivateVs:
                            consumer.onDeactivateVs(messageMap.get(type));
                            break;
                        case DeactivateSlb:
                            consumer.onDeactivateSlb(messageMap.get(type));
                            break;
                    }
                }
            } catch (Exception e) {
                logger.error("[[consumerStatus=failed]][ConsumerExecutor] Consumer Execute Failed.Consumer:" + consumer.getClass().getSimpleName(), e);
            }
            logger.info("[[consumer=" + consumer.getClass().getSimpleName() + ",messageStatus=processing]][ConsumerExecutor] Consumer Finished. cost:" + (System.currentTimeMillis() - start));
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
                    if (configHandler.getEnable("message.queue", null, null, null, false)) {
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

