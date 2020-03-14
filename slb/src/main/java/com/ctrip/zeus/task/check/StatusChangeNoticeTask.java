package com.ctrip.zeus.task.check;

import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.exceptions.SlbValidatorException;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.alert.AlertItem;
import com.ctrip.zeus.model.model.TrafficPolicy;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.change.StatusChangeService;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.service.mail.templet.StatusChangeMailTemplate;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.model.TrafficPolicyRepository;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.query.CriteriaQuery;
import com.ctrip.zeus.service.query.impl.DefaultGroupCriteriaQuery;
import com.ctrip.zeus.service.query.impl.DefaultTrafficPolicyCriteriaQuery;
import com.ctrip.zeus.service.task.TaskExecuteRecordService;
import com.ctrip.zeus.service.tools.local.LocalInfoService;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.EnvHelper;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component("groupStatusNoticeTask")
public class StatusChangeNoticeTask extends AbstractTask {
    @Resource
    private LocalInfoService localInfoService;

    @Resource
    private ConfigHandler configHandler;

    @Resource
    private TaskExecuteRecordService taskExecuteRecordService;

    @Resource
    private DbLockFactory dbLockFactory;

    @Resource
    protected TagService tagService;

    @Resource
    protected GroupQuery groupQuery;

    @Autowired
    private MailService mailService;

    @Resource
    private DefaultGroupCriteriaQuery groupCriteriaQuery;

    @Resource
    private DefaultTrafficPolicyCriteriaQuery trafficPolicyQuery;

    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;

    @Resource
    private StatusChangeService statusChangeService;

    @Autowired
    private ConfigValueService configValueService;

    private static DynamicBooleanProperty groupStatusChangeEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("group.status.change.enabled", true);
    private static DynamicBooleanProperty policyStatusChangeEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("policy.status.change.enabled", false);
    private static DynamicBooleanProperty sendMailEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("status.change.mail.enable", true);
    private static DynamicBooleanProperty localIpAppendEnabled = DynamicPropertyFactory.getInstance().getBooleanProperty("status.change.mail.append.ip", false);

    private static DynamicIntProperty interval = DynamicPropertyFactory.getInstance().getIntProperty("status.change.notice.task.send.interval", 10);
    private static DynamicIntProperty timespan = DynamicPropertyFactory.getInstance().getIntProperty("status.change.notice.task.send.timespan", 20);
    private static DynamicIntProperty nextNoticedTime = DynamicPropertyFactory.getInstance().getIntProperty("status.change.next.notice.hour.time", 23);

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String TASKNAME = "StatusChangeNoticeTask";

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        if (EnvHelper.portal() && configHandler.getEnable("status.change.task", null, null, null, true)) {
            logger.info(TASKNAME + " Started.");

            DistLock lock = dbLockFactory.newLock(TASKNAME);
            try {
                if (lock.tryLock()) {
                    int i = 0;
                    Long[] needNoticedGroups = new Long[]{};
                    Long[] needNoticedPolicies = new Long[]{};

                    // Check if there are to be noticed records
                    if (groupStatusChangeEnabled.get()) {
                        needNoticedGroups = getNeedNoticedItems("group");
                    }
                    if (policyStatusChangeEnabled.get()) {
                        needNoticedPolicies = getNeedNoticedItems("policy");
                    }
                    if (needNoticedGroups != null && needNoticedGroups.length > 0) {
                        // Notice
                        noticeStatusChanges(needNoticedGroups, "group");
                        // Update
                        updateStatusChangeItems(needNoticedGroups, "group");
                    }
                    if (needNoticedPolicies != null && needNoticedPolicies.length > 0) {
                        // Notice
                        noticeStatusChanges(needNoticedPolicies, "policy");
                        // Update
                        updateStatusChangeItems(needNoticedPolicies, "policy");
                    }
                    i++;
                    if (i % interval.get() == 0) {
                        Thread.sleep(1000);
                    }
                    taskExecuteRecordService.markExecution(TASKNAME);
                }
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 2 * 60000;
    }

    /****
     * Get need to noticed item ids
     * **/
    private Long[] getNeedNoticedItems(String type) throws Exception {
        List<AlertItem> statusChanges = statusChangeService.getStatusChangesByTypeAndStatus(type, false);
        Set<Long> idsList = new HashSet<>();
        for (AlertItem change : statusChanges) {
            // if current status change is not appeared bigger than 20 minutes
            if (System.currentTimeMillis() - change.getAppearTime().getTime() > timespan.get() * 60000) {
                // if it was never alerted
                Date lastNoticedTime = change.getLastNoticeTime();
                if (lastNoticedTime == null) {
                    idsList.add(change.getTarget());
                } else {
                    if (System.currentTimeMillis() - lastNoticedTime.getTime() > nextNoticedTime.get() * 60 * 1000 * 60) {
                        idsList.add(change.getTarget());
                    }
                }
            }
        }

        // no changes
        if (idsList.size() == 0) return null;

        Long[] groupIds = idsList.toArray(new Long[idsList.size()]);

        //Status changes' related items are really closed
        CriteriaQuery query;
        switch (type) {
            case "group":
                query = groupCriteriaQuery;
                break;

            case "policy":
                query = trafficPolicyQuery;
                break;

            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }

        Set<IdVersion> onlines = query.queryByIdsAndMode(groupIds, SelectionMode.ONLINE_EXCLUSIVE);
        Set<IdVersion> offlines = query.queryByIdsAndMode(groupIds, SelectionMode.OFFLINE_EXCLUSIVE);

        // Convert set to map for searching purpose
        Map<Long, Integer> onlinesMapping = convertVersions(onlines);
        Map<Long, Integer> offlinesMapping = convertVersions(offlines);

        for (Long id : groupIds) {
            // If current id does not has online version. This group has been deactivated. Needless to report
            if (!onlinesMapping.containsKey(id)) {
                idsList.remove(id);
                continue;
            }
            // If current id does not has offline version. This group has been activated. needless to report
            if (!offlinesMapping.containsKey(id)) {
                idsList.remove(id);
                continue;
            }
            // If current id has online and offline version. Compare them. if offline>online. This need to report
            Integer onlineVersion = onlinesMapping.get(id);
            Integer offlineVersion = offlinesMapping.get(id);

            if (offlineVersion <= onlineVersion) {
                idsList.remove(id);
            }
        }

        return idsList.toArray(new Long[idsList.size()]);
    }

    /**
     * Update the status changes with last noticed time
     **/
    private void updateStatusChangeItems(Long[] targets, String type) throws Exception {
        List<AlertItem> changes = statusChangeService.getStatusChangesByTypeIdsAndStatus(targets, type, false);
        for (AlertItem change : changes) {
            change.setLastNoticeTime(new Date());
        }
        statusChangeService.batchUpdateStatusChanges(changes);
    }

    private void noticeStatusChanges(Long[] targets, String type) throws Exception {

        for (Long id : targets) {
            noticeStatusChange(id, type);
        }
    }

    protected void noticeStatusChange(Long targetId, String type) throws Exception {
        Long[] targets = new Long[1];
        targets[0] = targetId;
        Map<Long, List<String>> idTags = tagService.getTags(type, targets);

        String[] versions;
        String appearTime;
        String performer;
        String appId = null;
        String groupName = null;
        String mailTitle = null;

        List<String> receiptions = new ArrayList<>();
        receiptions.add(configValueService.getTeamMail());

        if ("group".equalsIgnoreCase(type)) {
            appId = groupQuery.getAppId(targetId);
            groupName = groupQuery.getGroupName(targetId);
            mailTitle = "[问题告警]SLB Group变更后未激活生效: 超过" + timespan.getValue() + "分钟";
        }
        if ("policy".equalsIgnoreCase(type)) {
            TrafficPolicy policy = trafficPolicyRepository.getById(targetId);
            if (policy != null) {
                groupName = policy.getName();
            }
            mailTitle = "[问题告警]SLB 策略变更后未激活生效: 超过" + timespan.getValue() + "分钟";
        }

        // Get the notice by target ids
        List<AlertItem> changes = statusChangeService.getStatusChangesByTypeIdsAndStatus(new Long[]{targetId}, type, false);
        if (changes == null)
            throw new SlbValidatorException("Could not get the status change record for " + type + ", Id=" + targetId);

        String serverIp = localInfoService.getLocalIp();

        AlertItem change = changes.get(0);
        versions = change.getVersions().split(";");
        appearTime = change.getAppearTime() != null ? change.getAppearTime().toString() : "未知时间";
        performer = change.getPerformer() != null ? change.getPerformer() : "未知用户";

        if (localIpAppendEnabled.get()) {
            if (serverIp != null) {
                performer += ("/" + serverIp);
            } else {
                logger.error("[StatusChangeNoticeTask::] Could not get local ip");
            }
        }

        // Send mail
        if (sendMailEnable.get()) {
            SlbMail mail = new SlbMail();
            mail.setHtml(true);
            mail.setSubject(mailTitle);
            mail.setRecipients(receiptions);
            mail.setBody(StatusChangeMailTemplate.getInstance().build(groupName, targetId, appId, versions[0], versions[1], appearTime, performer, type, mailTitle));
            mailService.sendEmail(mail);
        }
    }

    /**
     * Utils methods. Convert an IdVersion set to Id version mapping
     **/
    private Map<Long, Integer> convertVersions(Set<IdVersion> idVersionSet) {
        Map<Long, Integer> result = new HashMap<>();
        for (IdVersion idv : idVersionSet) {
            if (!result.containsKey(idv.getId())) {
                result.put(idv.getId(), idv.getVersion());
            }
        }
        return result;
    }
}
