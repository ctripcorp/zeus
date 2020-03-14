package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.exceptions.SlbValidatorException;
import com.ctrip.zeus.model.alert.AlertItem;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.service.change.StatusChangeService;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.CriteriaQuery;
import com.ctrip.zeus.service.query.impl.DefaultGroupCriteriaQuery;
import com.ctrip.zeus.service.query.impl.DefaultTrafficPolicyCriteriaQuery;
import com.ctrip.zeus.util.MessageUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


/**
 * Created by ygshen on 2018/2/26.
 */
@Service("statusChangeConsumer")
public class StatusChangeConsumer extends AbstractConsumer {

    @Resource
    private DefaultGroupCriteriaQuery groupCriteriaQuery;

    @Resource
    private DefaultTrafficPolicyCriteriaQuery trafficPolicyQuery;

    @Resource
    private StatusChangeService statusChangeService;


    @Override
    public void onUpdateGroup(List<Message> messages) {
        try {
            saveChanges(messages, "group");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivateGroup(List<Message> messages) {
        try {
            saveChanges(messages, "group");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeactivateGroup(List<Message> messages) {
        try {
            saveChanges(messages, "group");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onUpdatePolicy(List<Message> messages) {
        try {
            saveChanges(messages, "policy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivatePolicy(List<Message> messages) {
        try {
            saveChanges(messages, "policy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeactivatePolicy(List<Message> messages) {
        try {
            saveChanges(messages, "policy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveChanges(List<Message> messages, String type) throws Exception {
        Map<Long, String> idUsers = new HashMap<>();
        Set<Long> ids;

        for (Message msg : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(msg.getTargetData());
            if (data != null && data.getSuccess()) {
                String user = data.getUser();
                if (!idUsers.containsKey(msg.getTargetId())) {
                    idUsers.put(msg.getTargetId(), user);
                }
            }
        }

        ids = idUsers.keySet();
        Long[] groupIds = ids.toArray(new Long[ids.size()]);

        if (groupIds == null || groupIds.length == 0) return;

        CriteriaQuery query;
        switch (type) {
            case "group":
                query = groupCriteriaQuery;
                break;

            case "policy":
                query = trafficPolicyQuery;
                break;

            default:
                throw new SlbValidatorException("Invalid type: " + type);
        }


        Set<IdVersion> onlines = query.queryByIdsAndMode(groupIds, SelectionMode.ONLINE_EXCLUSIVE);
        Set<IdVersion> offlines = query.queryByIdsAndMode(groupIds, SelectionMode.OFFLINE_EXCLUSIVE);


        HashMap<Long, Integer> offlinesMapping = new HashMap<>();
        for (IdVersion offline : offlines) {
            offlinesMapping.put(offline.getId(), offline.getVersion());
        }

        Map<Long, String> tIds = new HashMap<>();
        Set<Long> dIds = new HashSet<>();
        Set<Long> aIds = new HashSet<>();
        if (onlines == null || onlines.size() == 0) {
            // deactivate operation
            dIds = ids;
        }

        for (IdVersion online : onlines) {
            Long id = online.getId();

            int onlineVersion = online.getVersion();

            if (!offlinesMapping.containsKey(id)) {
                aIds.add(id);
                continue;
            }

            int offlineVersion = offlinesMapping.get(id);

            int gap = offlineVersion - onlineVersion;
            if (gap > 0) {
                tIds.put(id, onlineVersion + ";" + offlineVersion);
            } else {
                aIds.add(id);
            }
        }

        // New issues
        if (tIds.size() > 0) {
            List<AlertItem> opened = statusChangeService.getStatusChangesByTypeIdsAndStatus(tIds.keySet().toArray(new Long[tIds.size()]), type, false);
            AlertItem setted = new AlertItem().
                    setAppearTime(new Date()).
                    setType(type).
                    setStatus(false);

            List<AlertItem> items1 = new ArrayList<>();
            for (AlertItem c : opened) {
                Long target = c.getTarget();
                c.setVersions(tIds.get(target));
                c.setPerformer(idUsers.get(target));
                tIds.remove(target);
                items1.add(c);
            }
            statusChangeService.batchAddStatusChange(items1);

            // tIds left not existed and closed records, create new records
            List<AlertItem> items2 = new ArrayList<>();
            for (Long id : tIds.keySet()) {
                setted.setVersions(tIds.get(id));
                setted.setPerformer(idUsers.get(id));
                setted.setTarget(id);
                items2.add(setted);
            }
            statusChangeService.batchAddStatusChange(items2);
        }

        // Close issues
        if (aIds.size() > 0 || dIds.size() > 0) {
            aIds.addAll(dIds);
            List<AlertItem> existingList = statusChangeService.getStatusChangesByTypeAndIds(aIds.toArray(new Long[aIds.size()]), type);
            List<AlertItem> items3 = new ArrayList<>();
            for (AlertItem c : existingList) {
                if (!c.isStatus()) {
                    c.setStatus(true);
                    c.setSolvedTime(new Date());
                    items3.add(c);
                }
            }
            statusChangeService.batchUpdateStatusChanges(items3);
        }
    }
}
