package com.ctrip.zeus.service.message.queue;

import com.ctrip.zeus.model.queue.Message;
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
    protected void init() {
        messageQueue.addConsummer(this);
    }

    @Override
    public void onSlbCreatingFlow(List<Message> messages) {

    }

    @Override
    public void onSlbShardingFlow(List<Message> messages) {

    }

    @Override
    public void onCertOperation(List<Message> messages) {

    }

    @Override
    public void onSlbDestoryFlow(List<Message> messages) {

    }

    @Override
    public void onVSSplitFlow(List<Message> messages) {

    }

    @Override
    public void onVSMergeFlow(List<Message> messages) {

    }

    @Override
    public void onUpdateGroup(List<Message> messages) {

    }

    @Override
    public void onNewGroup(List<Message> messages) {

    }

    @Override
    public void onNewDelegateGroup(List<Message> messages) {

    }

    @Override
    public void onDeleteGroup(List<Message> messages) {

    }

    @Override
    public void onUpdatePolicy(List<Message> messages) {

    }

    @Override
    public void onNewPolicy(List<Message> messages) {

    }

    @Override
    public void onDeletePolicy(List<Message> messages) {

    }

    @Override
    public void onActivatePolicy(List<Message> messages) {

    }

    @Override
    public void onDeactivatePolicy(List<Message> messages) {

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
    @Override
    public void onAuthApply(List<Message> messages) {

    }

    @Override
    public void onSandBox(List<Message> messages) {

    }

    @Override
    public void onUpdateDr(List<Message> messages) {

    }

    @Override
    public void onNewDr(List<Message> messages) {

    }

    @Override
    public void onDeleteDr(List<Message> messages) {

    }

    @Override
    public void onActivateDr(List<Message> messages) {

    }

    @Override
    public void onDeactivateDr(List<Message> messages) {

    }
    @Override
    public void onReload(List<Message> messages) {

    }
}
