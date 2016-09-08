package com.ctrip.zeus.service.message.queue;

import com.ctrip.zeus.queue.entity.Message;

import java.util.List;

/**
 * Created by fanqq on 2016/9/6.
 */
public interface Consumer {
    public void onUpdateGroup(List<Message> messages);

    public void onNewGroup(List<Message> messages);

    public void onDeleteGroup(List<Message> messages);

    public void onUpdateVs(List<Message> messages);

    public void onNewVs(List<Message> messages);

    public void onDeleteVs(List<Message> messages);

    public void onNewSlb(List<Message> messages);

    public void onUpdateSlb(List<Message> messages);

    public void onDeleteSlb(List<Message> messages);

    public void onOpsPull(List<Message> messages);

    public void onOpsMember(List<Message> messages);

    public void onOpsServer(List<Message> messages);

    public void onOpsHealthy(List<Message> messages);

    public void onActivateGroup(List<Message> messages);

    public void onDeactivateGroup(List<Message> messages);

    public void onActivateVs(List<Message> messages);

    public void onDeactivateVs(List<Message> messages);

    public void onActivateSlb(List<Message> messages);

    public void onDeactivateSlb(List<Message> messages);

}
