package com.ctrip.zeus.service.message.queue;


/**
 * Created by fanqq on 2016/9/6.
 */
public interface MessageQueue {

    public void addConsummer(Consumer consumer);

    public void produceMessage(MessageType type, Long targetId, String targetData) throws Exception;

    public void produceMessage(String type, Long targetId, String targetData) throws Exception;
}
