package com.ctrip.zeus.service.message.queue.consumers;


import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.status.StatusService;
import com.ctrip.zeus.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("groupStatusCleanConsumer")
public class GroupStatusCleanConsumer extends AbstractConsumer {
    @Resource
    private StatusService statusService;
    @Resource
    private DbLockFactory dbLockFactory;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int TIMEOUT = 1000;

    @Override
    public void onUpdateGroup(List<Message> messages) {
        cleanDisableGroupServerStatus(messages);
    }

    @Override
    public void onActivateGroup(List<Message> messages) {
        cleanDisableGroupServerStatus(messages);
    }

    private void cleanDisableGroupServerStatus(List<Message> messages) {
        for (Message msg : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(msg.getTargetData());
            if (data != null && data.getSuccess()) {
                try {
                    DistLock lock = dbLockFactory.newLock(msg.getTargetId() + "_updateGroup");
                    if (lock.tryLock()){
                        try {
                            statusService.cleanDisabledGroupServerStatus(msg.getTargetId());
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (Exception e) {
                    logger.error("Clean Disabled Group Server Status.", e);
                }
            }
        }
    }

    @Override
    public void onDeleteGroup(List<Message> messages) {
        for (Message msg : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(msg.getTargetData());
            if (data != null && data.getSuccess()) {
                try {
                    statusService.cleanGroupServerStatus(msg.getTargetId());
                } catch (Exception e) {
                    logger.error("Clean Delete All Group Server Status.", e);
                }
            }
        }
    }
}
