package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.query.*;
import com.ctrip.zeus.service.tagging.EntityTaggingService;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.util.MessageUtil;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

;

/**
 * Created by fanqq on 2017/1/9.
 */
@Service("addPropertiesConsumer")
public class AddPropertiesConsumer extends AbstractConsumer {

    @Autowired
    protected GroupRepository groupRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private PropertyService propertyService;
    @Resource
    protected PropertyBox propertyBox;
    @Autowired
    protected AppService appService;
    @Resource
    protected GroupQuery groupQuery;

    @Resource
    private ConfigHandler configHandler;
    @Resource
    protected TagBox tagBox;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private EntityTaggingService entityTaggingService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onUpdateGroup(List<Message> messages) {
        addPropertiesForGroup(messages);
        for (Message message : messages) {
            try {
                IdVersion[] online = groupCriteriaQuery.queryByIdAndMode(message.getTargetId(), SelectionMode.ONLINE_EXCLUSIVE);
                IdVersion[] offline = groupCriteriaQuery.queryByIdAndMode(message.getTargetId(), SelectionMode.OFFLINE_FIRST);

                Integer onlineVersion = online.length == 1 ? online[0].getVersion() : null;
                Integer offlineVersion = offline.length == 1 ? offline[0].getVersion() : null;

                if (onlineVersion == null) {
                    setProperty("status", "deactivated", "group", message.getTargetId());
                } else if (!onlineVersion.equals(offlineVersion)) {
                    setProperty("status", "toBeActivated", "group", message.getTargetId());
                } else if (onlineVersion.equals(offlineVersion)) {
                    setProperty("status", "activated", "group", message.getTargetId());
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        addPropertiesForGroup(messages);
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "group", message.getTargetId());
        }
    }

    @Override
    public void onDeleteGroup(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            try {
                propertyBox.clear("group", message.getTargetId());
            } catch (Exception ex) {
                logger.error("[[addProperty=false]]Add Group Property Failed.GroupId:" + message.getTargetId());
            }
            try {
                tagBox.clear("group", message.getTargetId());
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onUpdatePolicy(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            try {
                if (trafficPolicyQuery.queryByIdAndMode(message.getTargetId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                    setProperty("status", "toBeActivated", "policy", message.getTargetId());
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onNewPolicy(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "policy", message.getTargetId());
        }
    }

    @Override
    public void onDeletePolicy(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            try {
                propertyBox.clear("policy", message.getTargetId());
            } catch (Exception ex) {
                logger.error("[[addProperty=false]]Clear policy Property Failed.GroupId:" + message.getTargetId());
            }
            try {
                tagBox.clear("policy", message.getTargetId());
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onActivatePolicy(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "activated", "policy", message.getTargetId());
        }
    }

    @Override
    public void onDeactivatePolicy(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "policy", message.getTargetId());
        }
    }

    @Override
    public void onUpdateDr(List<Message> messages) {

    }

    @Override
    public void onNewDr(List<Message> messages) {

    }

    @Override
    public void onDeleteDr(List<Message> messages) {

    }

    @Override
    public void onActivateDr(List<Message> messages) {

    }

    @Override
    public void onDeactivateDr(List<Message> messages) {

    }


    @Override
    public void onActivateGroup(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "activated", "group", message.getTargetId());
        }
    }

    @Override
    public void onDeactivateGroup(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "group", message.getTargetId());
        }
    }

    @Override
    public void onActivateVs(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "activated", "vs", message.getTargetId());
        }
    }

    @Override
    public void onDeactivateVs(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "vs", message.getTargetId());
        }
    }

    @Override
    public void onActivateSlb(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "activated", "slb", message.getTargetId());
        }
    }

    @Override
    public void onDeactivateSlb(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "slb", message.getTargetId());
        }
    }

    @Override
    public void onDeleteVs(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            try {
                propertyBox.clear("vs", message.getTargetId());
            } catch (Exception ex) {
                logger.error("[[addProperty=false]]Add VS Property Failed.VS:" + message.getTargetId());
            }
            try {
                tagBox.clear("vs", message.getTargetId());
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onNewSlb(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "slb", message.getTargetId());
        }
    }

    @Override
    public void onUpdateSlb(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            try {
                IdVersion[] versions = slbCriteriaQuery.queryByIdAndMode(message.getTargetId(), SelectionMode.REDUNDANT);
                if (versions.length == 1) {
                    setProperty("status", "deactivated", "slb", message.getTargetId());
                } else if (versions[0].getVersion().equals(versions[1].getVersion())) {
                    setProperty("status", "activated", "slb", message.getTargetId());
                } else {
                    setProperty("status", "toBeActivated", "slb", message.getTargetId());
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onDeleteSlb(List<Message> messages) {
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            try {
                propertyBox.clear("vs", message.getTargetId());
            } catch (Exception ex) {
                logger.error("[[addProperty=false]]Add SLB Property Failed.SlbId:" + message.getTargetId());
            }
            try {
                tagBox.clear("vs", message.getTargetId());
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public void onNewVs(List<Message> messages) {
        addZonePropertiesForVs(messages);
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            setProperty("status", "deactivated", "vs", message.getTargetId());
        }
    }

    @Override
    public void onUpdateVs(List<Message> messages) {
        addZonePropertiesForVs(messages);
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            try {
                if (virtualServerCriteriaQuery.queryByIdAndMode(message.getTargetId(), SelectionMode.ONLINE_EXCLUSIVE).length == 1) {
                    setProperty("status", "toBeActivated", "vs", message.getTargetId());
                }
            } catch (Exception ex) {
            }

        }
    }


    private void addZonePropertiesForVs(List<Message> messages) {
        if (!configHandler.getEnable("add.zone.properties.consumer", true)) {
            return;
        }
        for (Message msg : messages) {
            try {
                if (msg.getTargetId() <= 0) continue;
                VirtualServer virtualServer = virtualServerRepository.getById(msg.getTargetId());
                if (virtualServer == null || virtualServer.getSlbIds() == null || virtualServer.getSlbIds().size() == 0)
                    continue;

                Set<String> zone = new HashSet<>();
                Set<String> idc = new HashSet<>();
                for (Long slbId : virtualServer.getSlbIds()) {
                    Property zoneProperty = propertyService.getProperty("zone", slbId, "slb");
                    Property idcProperty = propertyService.getProperty("idc", slbId, "slb");
                    if (zoneProperty != null) {
                        zone.add(zoneProperty.getValue());
                    }
                    if (idcProperty != null) {
                        idc.add(idcProperty.getValue());
                    }
                }
                if (zone.size() > 0) {
                    String zoneStr = Joiner.on(',').join(zone);
                    propertyBox.set("zone", zoneStr, "vs", msg.getTargetId());
                    logger.info("add vs zone property success.VsId:" + msg.getTargetId() + " pValue:" + zoneStr);
                }
                if (idc.size() > 0) {
                    String idcStr = Joiner.on(',').join(idc);
                    propertyBox.set("idc", idcStr, "vs", msg.getTargetId());
                    logger.info("add vs idc property success.VsId:" + msg.getTargetId() + " pValue:" + idcStr);
                }
            } catch (Exception e) {
                logger.error("Add zone property for vs failed. VirtualServerId:" + msg.getTargetId());
            }
        }
    }

    private void addPropertiesForGroup(List<Message> messages) {
        for (Message msg : messages) {
            if (msg.getTargetId() <= 0) continue;

            Group group;
            try {
                group = groupRepository.getById(msg.getTargetId());
            } catch (Exception e) {
                logger.error("Error occurs when loading group #" + msg.getTargetId(), e);
                continue;
            }

            if (group == null) {
                logger.error("Failed to get Group with groupId:" + msg.getTargetId());
                return;
            }

            addZonePropertiesForGroup(group);

            try {
                App app = appService.getAppByAppid(group.getAppId());
                if (app != null && app.getSbu() != null) {
                    setProperty("SBU", app.getSbu(), "group", group.getId());
                    if (app.getSbuCode() != null) {
                        setProperty("sbuCode", app.getSbuCode(), "group", group.getId());
                    }
                }
            } catch (Exception e) {
                logger.error("add Group Sbu Property Failed.GroupId:" + group.getId(), e);
            }

            try {
                entityTaggingService.tagGroupAdvancedFeatures(group);
            } catch (Exception e) {

                logger.error("Error occurs when updating advanced feature tags for group #" + group.getId(), e);
            }
        }
    }

    private void addZonePropertiesForGroup(Group group) {
        if (!configHandler.getEnable("add.zone.properties.consumer", true)) {
            return;
        }
        try {
            if (group.getGroupVirtualServers() == null || group.getGroupVirtualServers().size() == 0)
                return;

            Set<Long> vsIds = new HashSet<>();
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }

            List<VirtualServer> vses = virtualServerRepository.listAll(vsIds.toArray(new Long[vsIds.size()]));
            Set<Long> slbIds = new HashSet<>();
            for (VirtualServer vs : vses) {
                slbIds.addAll(vs.getSlbIds());
            }
            Set<String> zone = new HashSet<>();
            Set<String> idc = new HashSet<>();
            for (Long slbId : slbIds) {
                Property zoneProperty = propertyService.getProperty("zone", slbId, "slb");
                Property idcProperty = propertyService.getProperty("idc", slbId, "slb");
                if (zoneProperty != null) {
                    zone.add(zoneProperty.getValue());
                }
                if (idcProperty != null) {
                    idc.add(idcProperty.getValue());
                }
            }
            if (zone.size() > 0) {
                String zoneStr = Joiner.on(',').join(zone);
                propertyBox.set("zone", zoneStr, "group", group.getId());
                logger.info("add group zone property success.GroupId:" + group.getId() + " pValue:" + zoneStr);
            }
            if (idc.size() > 0) {
                String idcStr = Joiner.on(',').join(idc);
                setProperty("idc", idcStr, "group", group.getId());
                logger.info("add group idc property success.GroupId:" + group.getId() + " pValue:" + idcStr);
            }
        } catch (Exception e) {
            logger.error("Add zone property for group failed. GroupId:" + group.getId());
        }
    }

    protected void setProperty(String pname, String pvalue, String type, Long id) {
        try {
            propertyBox.set(pname, pvalue, type, id);
        } catch (Exception e) {
            logger.error("Add " + pname + " property for " + type + " failed. Id:" + id);
        }
    }

    protected void setTag(String tagName, String type, Long[] itemIds) {
        try {
            tagBox.tagging(tagName, type, itemIds);
        } catch (Exception e) {
            logger.error("Add tag: " + tagName + " for " + type + " failed. Ids:" + Arrays.toString(itemIds));
        }
    }
}
