package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.executor.scheduler.SlbTaskStarter;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SlbTaskStarterConsumer extends AbstractConsumer {
    @Resource
    private SlbTaskStarter slbTaskStarter;

    @Override
    public void onNewSlb(List<Message> messages) {
        slbTaskStarter.startUp();
    }
}
