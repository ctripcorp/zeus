package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.queue.entity.Message;
import com.ctrip.zeus.queue.entity.SlbMessageData;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.status.entity.GroupServerStatus;
import com.ctrip.zeus.status.entity.GroupStatus;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.MessageUtil;
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

    @Override
    public void onOpsPull(List<Message> messages) {
        addHealthProperty(messages);
    }

    @Override
    public void onOpsMember(List<Message> messages) {
        addHealthProperty(messages);
    }

    @Override
    public void onOpsServer(List<Message> messages) {
        addHealthProperty(messages);
    }

    @Override
    public void onOpsHealthy(List<Message> messages) {
        addHealthProperty(messages);
    }

    protected void addHealthProperty(List<Message> messages) {
        try {
            Set<Long> groupIds = new HashSet<>();
            for (Message msg : messages) {
                SlbMessageData data = MessageUtil.parserSlbMessageData(msg.getTargetData());
                if (data != null && data.getSuccess()) {
                    groupIds.add(msg.getTargetId());
                }
            }
            List<GroupStatus> gses = groupStatusService.getOfflineGroupsStatus(groupIds);

            for (GroupStatus gs : gses) {
                int upCount = 0;
                int healthCount = 0;
                int pullInCount = 0;
                int memberUpCount = 0;
                int serverUpCount = 0;
                int allServerCount = gs.getGroupServerStatuses().size();

                for (GroupServerStatus gss : gs.getGroupServerStatuses()) {
                    if (gss.getServer() && gss.getHealthy() && gss.getPull() && gss.getMember()) {
                        upCount += 1;
                        serverUpCount += 1;
                        healthCount += 1;
                        pullInCount += 1;
                        memberUpCount += 1;
                        continue;
                    }
                    if (gss.getServer()) {
                        serverUpCount += 1;
                    }
                    if (gss.getHealthy()) {
                        healthCount += 1;
                    }
                    if (gss.getPull()) {
                        pullInCount += 1;
                    }
                    if (gss.getMember()) {
                        memberUpCount += 1;
                    }
                }
                if (upCount == allServerCount) {
                    propertyBox.set("healthy", "healthy", "group", gs.getGroupId());
                } else if (upCount == 0) {
                    propertyBox.set("healthy", "broken", "group", gs.getGroupId());
                } else {
                    propertyBox.set("healthy", "unhealthy", "group", gs.getGroupId());
                }

                if (serverUpCount == allServerCount) {
                    propertyBox.set("serverHealthy", "healthy", "group", gs.getGroupId());
                } else if (serverUpCount == 0) {
                    propertyBox.set("serverHealthy", "broken", "group", gs.getGroupId());
                } else {
                    propertyBox.set("serverHealthy", "unhealthy", "group", gs.getGroupId());
                }
                if (memberUpCount == allServerCount) {
                    propertyBox.set("memberHealthy", "healthy", "group", gs.getGroupId());
                } else if (memberUpCount == 0) {
                    propertyBox.set("memberHealthy", "broken", "group", gs.getGroupId());
                } else {
                    propertyBox.set("memberHealthy", "unhealthy", "group", gs.getGroupId());
                }
                if (pullInCount == allServerCount) {
                    propertyBox.set("pullHealthy", "healthy", "group", gs.getGroupId());
                } else if (pullInCount == 0) {
                    propertyBox.set("pullHealthy", "broken", "group", gs.getGroupId());
                } else {
                    propertyBox.set("pullHealthy", "unhealthy", "group", gs.getGroupId());
                }
                if (healthCount == allServerCount) {
                    propertyBox.set("healthCheckHealthy", "healthy", "group", gs.getGroupId());
                } else if (healthCount == 0) {
                    propertyBox.set("healthCheckHealthy", "broken", "group", gs.getGroupId());
                } else {
                    propertyBox.set("healthCheckHealthy", "unhealthy", "group", gs.getGroupId());
                }
            }
        } catch (Exception e) {
            logger.error("Update Group Healthy Failed.", e);
        }
    }

}
