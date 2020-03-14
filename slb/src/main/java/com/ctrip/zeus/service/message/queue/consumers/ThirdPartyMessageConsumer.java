package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by zhoumy on 2016/12/26.
 */
// TODO: 2019/10/10 : Capability placeholder . Messaging for dependent message systems
@Service("hermesMessageConsumer")
public class ThirdPartyMessageConsumer extends AbstractConsumer {
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

}
