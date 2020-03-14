package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by fanqq on 2016/9/27.
 */
// TODO: 2019/10/10 : Capability placeholder . Messaging for dependent message systems
@Service("groupHealthySendHermesConsumer")
public class GroupHealthySendHermesConsumer extends AbstractConsumer {

    @Override
    public void onUpdateGroup(List<com.ctrip.zeus.model.queue.Message> messages) {
    }


    @Override
    public void onNewGroup(List<com.ctrip.zeus.model.queue.Message> messages) {
    }

    @Override
    public void onOpsPull(List<com.ctrip.zeus.model.queue.Message> messages) {
    }

    @Override
    public void onOpsMember(List<com.ctrip.zeus.model.queue.Message> messages) {
    }

    @Override
    public void onOpsServer(List<com.ctrip.zeus.model.queue.Message> messages) {
    }

    @Override
    public void onOpsHealthy(List<com.ctrip.zeus.model.queue.Message> messages) {
    }
}
