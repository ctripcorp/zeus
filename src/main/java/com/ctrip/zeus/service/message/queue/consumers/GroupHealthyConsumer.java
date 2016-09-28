package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.queue.entity.Message;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.tag.PropertyBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2016/9/26.
 */
@Service("groupHealthyConsumer")
public class GroupHealthyConsumer extends AbstractConsumer {
    @Resource
    GroupStatusService groupStatusService;
    @Resource
    PropertyBox propertyBox;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onUpdateGroup(List<Message> messages) {
        addHealthProperty(messages);
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        addHealthProperty(messages);
    }

    protected void addHealthProperty(List<Message> messages) {
        try {
            Set<Long> groupIds = new HashSet<>();
            for (Message msg : messages) {
                groupIds.add(msg.getTargetId());
            }
            List<GroupStatus> gses = groupStatusService.getOfflineGroupsStatus(groupIds);
            for (GroupStatus gs : gses) {
                boolean health = true;
                boolean unhealth = true;
                for (GroupServerStatus gss : gs.getGroupServerStatuses()) {
                    if (gss.getServer() && gss.getHealthy() && gss.getPull() && gss.getMember()) {
                        unhealth = false;
                    } else {
                        health = false;
                    }
                }
                if (health) {
                    propertyBox.set("healthy", "healthy", "group", gs.getGroupId());
                } else if (unhealth) {
                    propertyBox.set("healthy", "broken", "group", gs.getGroupId());
                } else {
                    propertyBox.set("healthy", "unhealthy", "group", gs.getGroupId());
                }
            }
        } catch (Exception e) {
            logger.error("Update Group Healthy Failed.", e);
        }
    }

}
