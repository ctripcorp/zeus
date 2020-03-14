package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.support.LanguageCheck;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

;

/**
 * Created by fanqq on 2016/9/12.
 */
@Service("appUpdateConsumer")
public class AppUpdateConsumer extends AbstractConsumer {
    @Resource
    private GroupQuery groupQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Autowired
    private AppService appService;
    @Resource
    private TagBox tagBox;

    @Resource
    private PropertyBox propertyBox;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onUpdateGroup(List<Message> messages) {
        Set<Long> vgroups = new HashSet<>();
        Map<Long, Message> messageMap = new HashMap<>();
        try {
            vgroups = groupCriteriaQuery.queryAllVGroups();
        } catch (Exception e) {
            logger.warn("[AppUpdateConsumer] Get Vgroup Failed.", e);
        }
        for (Message message : messages) {
            try {
                if (vgroups.contains(message.getTargetId())) {
                    continue;
                }
                SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
                if (slbMessageData == null || !slbMessageData.getSuccess()) {
                    continue;
                }
                messageMap.put(message.getTargetId(), message);
                String appId = groupQuery.getAppId(message.getTargetId());

                if (!appService.hasApp(appId)) {
                    appService.updateApp(appId);
                }
                appService.groupChange(message.getTargetId());
            } catch (Exception e) {
                logger.warn("[AppUpdateConsumer] Update App Relations Failed On update Group.GroupId:" + message.getTargetId(), e);
            }
        }
        addTags(messageMap);
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        Set<Long> vgroups = new HashSet<>();
        try {
            vgroups = groupCriteriaQuery.queryAllVGroups();
        } catch (Exception e) {
            logger.warn("[AppUpdateConsumer] Get Vgroup Failed.", e);
        }
        Map<Long, Message> messageMap = new HashMap<>();
        for (Message message : messages) {
            try {
                if (vgroups.contains(message.getTargetId())) {
                    continue;
                }
                SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
                if (slbMessageData == null || !slbMessageData.getSuccess()) {
                    continue;
                }
                messageMap.put(message.getTargetId(), message);
                String appId = groupQuery.getAppId(message.getTargetId());
                if (!appService.hasApp(appId)) {
                    appService.updateApp(appId);
                }
                appService.groupChange(message.getTargetId());
            } catch (Exception e) {
                logger.warn("[AppUpdateConsumer] Update App Relations Failed On New Group.GroupId:" + message.getTargetId(), e);
            }
        }
        addTags(messageMap);
    }

    @Override
    public void onDeleteGroup(List<Message> messages) {
        Set<Long> vgroups = new HashSet<>();
        try {
            vgroups = groupCriteriaQuery.queryAllVGroups();
        } catch (Exception e) {
            logger.warn("[AppUpdateConsumer] Get Vgroup Failed.", e);
        }

        for (Message message : messages) {
            try {
                if (vgroups.contains(message.getTargetId())) {
                    continue;
                }
                SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
                if (slbMessageData == null || !slbMessageData.getSuccess()) {
                    continue;
                }
                appService.groupDelete(message.getTargetId());
            } catch (Exception e) {
                logger.warn("[AppUpdateConsumer] Update App Relations Failed On Delete Group.GroupId:" + message.getTargetId(), e);
            }
        }
    }

    @Override
    public void onUpdateVs(List<Message> messages) {
        for (Message message : messages) {
            try {
                SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
                if (slbMessageData == null || !slbMessageData.getSuccess()) {
                    continue;
                }
                appService.vsChange(message.getTargetId());
            } catch (Exception e) {
                logger.warn("[AppUpdateConsumer] Update App Relations Failed On Update Vs.VsId:" + message.getTargetId(), e);
            }
        }
    }

    private void addTags(Map<Long, Message> messageMap) {
        for (Message message : messageMap.values()) {
            Set<Long> vgroups = new HashSet<>();
            try {
                vgroups = groupCriteriaQuery.queryAllVGroups();
                if (vgroups.contains(message.getTargetId())) continue;
            } catch (Exception e) {
                logger.warn("[AppUpdateConsumer] Get Vgroup Failed.", e);
            }
            String appId = null;
            try {
                appId = groupQuery.getAppId(message.getTargetId());
            } catch (Exception e) {
                logger.error("Get AppId By GroupId failed. GroupId:" + message.getTargetId(), e);
            }

            // add owner tag
            try {
                App app = appService.getAppByAppid(appId);
                if (app == null || app.getOwner() == null) {
                    tagBox.tagging("owner_unknown", "group", new Long[]{message.getTargetId()});
                } else {
                    tagBox.tagging("owner_" + app.getOwner(), "group", new Long[]{message.getTargetId()});
                }
            } catch (Exception e) {
                logger.error("Add Owner Tag For Group Failed. GroupId:" + message.getTargetId(), e);
            }
        }
    }

    private void setProperty(String pname, String pvalue, String type, Long id) {
        try {
            propertyBox.set(pname, pvalue, type, id);
        } catch (Exception e) {
            logger.error("Add " + pname + " property for " + type + " failed. Id:" + id);
        }
    }
}
