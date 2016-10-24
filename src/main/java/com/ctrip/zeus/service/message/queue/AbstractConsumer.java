package com.ctrip.zeus.service.message.queue;

import com.ctrip.zeus.queue.entity.Message;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by fanqq on 2016/9/6.
 */
@Service
public class AbstractConsumer implements Consumer {

    @Resource
    private MessageQueue messageQueue;

    @PostConstruct
    private void init() {
        messageQueue.addConsummer(this);
    }

    @Override
    public void onUpdateGroup(List<Message> messages) {

    }

    @Override
    public void onNewGroup(List<Message> messages) {

    }

    @Override
    public void onDeleteGroup(List<Message> messages) {

    }

    @Override
    public void onUpdateVs(List<Message> messages) {

    }

    @Override
    public void onNewVs(List<Message> messages) {

    }

    @Override
    public void onDeleteVs(List<Message> messages) {

    }

    @Override
    public void onNewSlb(List<Message> messages) {

    }

    @Override
    public void onUpdateSlb(List<Message> messages) {

    }

    @Override
    public void onDeleteSlb(List<Message> messages) {

    }

    @Override
    public void onOpsPull(List<Message> messages) {

    }

    @Override
    public void onOpsMember(List<Message> messages) {

    }

    @Override
    public void onOpsServer(List<Message> messages) {

    }

    @Override
    public void onOpsHealthy(List<Message> messages) {

    }

    @Override
    public void onActivateGroup(List<Message> messages) {

    }

    @Override
    public void onDeactivateGroup(List<Message> messages) {

    }

    @Override
    public void onActivateVs(List<Message> messages) {

    }

    @Override
    public void onDeactivateVs(List<Message> messages) {

    }

    @Override
    public void onActivateSlb(List<Message> messages) {

    }

    @Override
    public void onDeactivateSlb(List<Message> messages) {

    }
}
