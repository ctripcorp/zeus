package com.ctrip.zeus.service.message.queue;

import com.ctrip.zeus.model.queue.Message;

import java.util.List;

/**
 * Created by fanqq on 2016/9/6.
 */
public interface Consumer {
    void onUpdateGroup(List<Message> messages);

    void onNewGroup(List<Message> messages);

    void onDeleteGroup(List<Message> messages);
    
    void onNewDelegateGroup(List<Message> messages);


    void onUpdateVs(List<Message> messages);

    void onNewVs(List<Message> messages);

    void onDeleteVs(List<Message> messages);

    void onNewSlb(List<Message> messages);

    void onUpdateSlb(List<Message> messages);

    void onDeleteSlb(List<Message> messages);

    void onOpsPull(List<Message> messages);

    void onOpsMember(List<Message> messages);

    void onOpsServer(List<Message> messages);

    void onOpsHealthy(List<Message> messages);

    void onActivateGroup(List<Message> messages);

    void onDeactivateGroup(List<Message> messages);

    void onActivateVs(List<Message> messages);

    void onDeactivateVs(List<Message> messages);

    void onActivateSlb(List<Message> messages);

    void onDeactivateSlb(List<Message> messages);

    void onUpdatePolicy(List<Message> messages);

    void onNewPolicy(List<Message> messages);

    void onDeletePolicy(List<Message> messages);

    void onActivatePolicy(List<Message> messages);

    void onDeactivatePolicy(List<Message> messages);

    void onAuthApply(List<Message> messages);

    void onSandBox(List<Message> messages);

    void onUpdateDr(List<Message> messages);

    void onNewDr(List<Message> messages);

    void onDeleteDr(List<Message> messages);

    void onActivateDr(List<Message> messages);

    void onDeactivateDr(List<Message> messages);

    void onSlbCreatingFlow(List<Message> messages);

    void onSlbShardingFlow(List<Message> messages);

    void onCertOperation(List<Message> messages);

    void onSlbDestoryFlow(List<Message> messages);

    void onVSSplitFlow(List<Message> messages);

    void onVSMergeFlow(List<Message> messages);

    void onReload(List<Message> messages);
}
