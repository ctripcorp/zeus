package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.model.status.GroupServerStatus;
import com.ctrip.zeus.model.status.GroupStatus;
import com.ctrip.zeus.service.auth.UserService;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.service.mail.templet.GroupHealthyMailTemplet;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.MessageUtil;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2016/9/26.
 */
@Service("groupHealthyConsumer")
public class GroupHealthyConsumer extends AbstractConsumer {
    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    protected TagService tagService;
    @Resource
    protected GroupQuery groupQuery;
    @Autowired
    MailService mailService;
    @Resource
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private DynamicBooleanProperty sendMailEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("slb.op.mail.enable", false);

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
        sendNotifyMail(messages, "member");
    }

    @Override
    public void onOpsServer(List<Message> messages) {
        addHealthProperty(messages);
        sendNotifyMail(messages, "server");
    }

    @Override
    public void onOpsHealthy(List<Message> messages) {
        addHealthProperty(messages);
        sendNotifyMail(messages, "healthy");
    }

    private void addHealthProperty(List<Message> messages) {
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

    private void sendNotifyMail(List<Message> messages, String type) {
        try {
            for (Message msg : messages) {
                SlbMessageData data = MessageUtil.parserSlbMessageData(msg.getTargetData());
                if (data == null || !data.getSuccess()) continue;
                if (type.equals("healthy")) {
                    GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(msg.getTargetId());
                    if (groupStatus == null || groupStatus.getGroupServerStatuses() == null) {
                        logger.warn("[GroupHealthyConsumer] Failed to get GroupStatus. Current Group Id:" + msg.getTargetId());
                        continue;
                    }
                    boolean skip = true;
                    for (GroupServerStatus gss : groupStatus.getGroupServerStatuses()) {
                        if (!data.getIps().contains(gss.getIp())) continue;
                        if (!gss.getPull() || !gss.getMember() || !gss.getServer()) continue;
                        skip = false;
                    }
                    if (skip) continue;
                }
                List<String> users = new ArrayList<>();
                List<String> tags = tagService.getTags("group", msg.getTargetId());
                for (String tag : tags) {
                    if (tag.startsWith("owner_") || tag.startsWith("user_")) {
                        String[] s = tag.split("_");
                        if (s.length > 1) {
                            users.add(s[1]);
                        }
                    }
                }
                String appId = groupQuery.getAppId(msg.getTargetId());
                String groupName = groupQuery.getGroupName(msg.getTargetId());
                String op = null;
                switch (msg.getType()) {
                    case "/api/op/upMember":
                        op = "Ops操作：Member拉入";
                        break;
                    case "/api/op/downMember":
                        op = "Ops操作：Member拉出";
                        break;
                    case "/api/op/upServer":
                        op = "Ops操作：Server拉入";
                        break;
                    case "/api/op/downServer":
                        op = "Ops操作：Server拉出";
                        break;
                    case "/api/op/raise":
                        op = "健康监测操作：健康监测拉入";
                        break;
                    case "/api/op/fall":
                        op = "健康监测操作：健康监测拉出";
                        break;
                }
                List<String> ips = data.getIps();
                String des = data.getDescription();
                if (sendMailEnable.get()) {
                    SlbMail mail = new SlbMail();
                    mail.setHtml(true);
                    mail.setSubject("集群可用性通知");
                    List<String> res = new ArrayList<>();
                    for (String user : users) {
                        User u = userService.getUserSimpleInfo(user);
                        if (u.getEmail() != null) {
                            res.add(u.getEmail());
                        }
                    }
                    mail.setRecipients(res);
                    mail.setBody(GroupHealthyMailTemplet.getInstance().build(op, groupName, msg.getTargetId(), appId, ips, des));
                    mailService.sendEmail(mail);
                }
            }
        } catch (Exception e) {
            logger.error("Send Notify Mail For Group Healthy Failed.", e);
        }
    }

}
