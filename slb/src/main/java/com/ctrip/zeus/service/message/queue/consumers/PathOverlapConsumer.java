package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.queue.GroupData;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.service.mail.templet.OperationMailTemple;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.ErrorType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.StringUtils;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("PathOverlapConsumer")
public class PathOverlapConsumer extends AbstractConsumer {
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    private ValidationFacade validationFacade;
    @Resource
    private EntityFactory entityFactory;
    @Resource
    private ConfigHandler configHandler;
    @Autowired
    private AppService appService;
    @Autowired
    private MailService mailService;
    @Autowired
    private ConfigValueService configValueService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onActivateGroup(List<Message> messages) {
        noticeEffectOwners(messages);
    }

    private void noticeEffectOwners(List<Message> messages) {
        for (Message msg : messages) {
            Long gid = msg.getTargetId();
            SlbMessageData messageData = MessageUtil.parserSlbMessageData(msg.getTargetData());
            if (messageData == null || messageData.getSuccess() == null || !messageData.getSuccess()) {
                continue;
            }

            int onlineVersion = 0;
            int offlineVersion = 0;
            for (GroupData gd : messageData.getGroupDatas()) {
                if (gd.getId().equals(gid)) {
                    if (offlineVersion == 0) {
                        offlineVersion = gd.getVersion();
                    } else {
                        if (gd.getVersion() > offlineVersion) {
                            onlineVersion = offlineVersion;
                            offlineVersion = gd.getVersion();
                        } else {
                            onlineVersion = gd.getVersion();
                        }
                    }
                }
            }
            if (onlineVersion == offlineVersion || onlineVersion == 0) continue;
            try {
                Group online = groupRepository.getByKey(new IdVersion(gid, onlineVersion));
                Group offline = groupRepository.getByKey(new IdVersion(gid, offlineVersion));
                Map<Long, GroupVirtualServer> onlineGvs = new HashMap<>();
                Map<Long, GroupVirtualServer> offlineGvs = new HashMap<>();
                for (GroupVirtualServer gvs : online.getGroupVirtualServers()) {
                    onlineGvs.put(gvs.getVirtualServer().getId(), gvs);
                }
                for (GroupVirtualServer gvs : offline.getGroupVirtualServers()) {
                    offlineGvs.put(gvs.getVirtualServer().getId(), gvs);
                }
                List<Long> needValidateVsId = new ArrayList<>();
                for (Long id : offlineGvs.keySet()) {
                    // Continue In Case Of Activate New Gvs Or Gvs's Path or Priority Changed.
                    if (!onlineGvs.containsKey(id) ||
                            (StringUtils.equalsIgnoreCase(onlineGvs.get(id).getPath(), offlineGvs.get(id).getPath()) &&
                                    onlineGvs.get(id).getPriority().equals(offlineGvs.get(id).getPriority()))) {
                        continue;
                    }
                    needValidateVsId.add(id);
                }
                ModelStatusMapping<Group> groupMap = entityFactory.getGroupsByVsIds(needValidateVsId.toArray(new Long[needValidateVsId.size()]));
                Set<Long> noticeGroup = new HashSet<>();
                for (Long vsId : needValidateVsId) {
                    ValidationContext context = new ValidationContext();
                    validationFacade.validateEntriesOnVs(vsId, new ArrayList<>(groupMap.getOnlineMapping().values()), new ArrayList<TrafficPolicy>(), context);
                    validationFacade.validateSkipErrorsOfRelatedGroup(offline, context);
                    if (!context.shouldProceed() &&
                            (context.getGroupErrorType(gid).contains(ErrorType.PATH_OVERLAP)
                                    || context.getGroupErrorType(gid).contains(ErrorType.ROOT_PATH_OVERLAP))) {
                        Set<Long> ids = context.getGroupRelatedIdsByErrorTypeAndId(gid, ErrorType.PATH_OVERLAP);
                        Set<Long> rootOverlapIds = context.getGroupRelatedIdsByErrorTypeAndId(gid, ErrorType.ROOT_PATH_OVERLAP);
                        if (ids.size() > 0) {
                            noticeGroup.addAll(ids);
                        }
                        if (rootOverlapIds.size() > 0) {
                            noticeGroup.addAll(rootOverlapIds);
                        }
                    }
                }
                if (noticeGroup.size() > 0) {
                    for (Long id : noticeGroup) {
                        int version = groupMap.getOnlineMapping().get(id).getVersion();
                        sendMail(groupRepository.getByKey(new IdVersion(id, version)), offline, offlineGvs, needValidateVsId);
                    }
                }

            } catch (Exception e) {
                logger.warn("Group Effect Notice Failed.Group:" + gid, e);
            }

        }
    }

    private void sendMail(Group group, Group changedGroup, Map<Long, GroupVirtualServer> offlineGvs, List<Long> needValidateVsId) throws Exception {
        if (!configHandler.getEnable("path.overlap.notice", true)) {
            return;
        }
        String baseAppId = group.getAppId();
        String changedAppId = changedGroup.getAppId();
        App baseApp = appService.getAppByAppid(baseAppId);
        App changedApp = appService.getAppByAppid(changedAppId);

        Map<Long, GroupVirtualServer> baseGvs = new HashMap<>();
        for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
            baseGvs.put(gvs.getVirtualServer().getId(), gvs);
        }

        String title = "[SLB Path Overlap通知]应用[" +
                (changedApp == null ? "" : changedApp.getChineseName()) + changedAppId + "]变更访问入口，导致拦截应用[" +
                (baseApp == null ? "" : baseApp.getChineseName()) + baseAppId + "]的部分流量";
        String ptitle = "[SLB Path Overlap通知]应用访问入口变更导致" + toRedSpan("拦截其他应用流量，请确认对业务无影响。");
        LinkedHashMap<String, String> des = new LinkedHashMap<>();
        des.put("变更的应用", (changedApp == null ? changedAppId : changedApp.getChineseName() + changedAppId + " Owner:" + changedApp.getOwner())
                + buildLink("查看应用详情", getGroupLink(changedGroup.getId())));
        des.put("被拦截应用", toRedSpan(baseApp == null ? baseAppId : baseApp.getChineseName() + baseAppId + " Owner:" + baseApp.getOwner())
                + buildLink("查看应用详情", getGroupLink(group.getId())));
        StringBuilder sb = new StringBuilder(128);
        StringBuilder baseSb = new StringBuilder(128);
        for (Long id : needValidateVsId) {
            sb.append("Domains：");
            sb.append(toRedSpan(domainsToString(offlineGvs.get(id).getVirtualServer().getDomains())));
            sb.append("<br/>");
            sb.append("Path：");
            sb.append(toRedSpan(offlineGvs.get(id).getPath()));
            sb.append("<br/>");
            baseSb.append("Domains：");
            baseSb.append(toRedSpan(domainsToString(baseGvs.get(id).getVirtualServer().getDomains())));
            baseSb.append("<br/>");
            baseSb.append("Path：");
            baseSb.append(toRedSpan(baseGvs.get(id).getPath()));
            baseSb.append("<br/>");
        }
        des.put("变更应用拦截流量的访问入口", sb.toString());
        des.put("被拦截应用的访问入口", baseSb.toString());

        String end = "<b>备注：</b>" +
                "<br>【SLB Path Overlap】是对访问入口Path的覆盖校验。" +
                "<br>举例：假设已存在应用A，访问入口Path为/a，现有另一应用B上线访问入口Path为/a/b，/a/b的流量（可能本来就并不存在/a/b的流量）" +
                "原本应该转发给应用A，由于B应用新的访问入口需求拦截/a/b的流量，因此判定为流量拦截。" +
                "<br>" + toRedSpan("假如/a/b的流量确实为A应用业务流量，或者该路径不允许其他应用接管，请紧急联系B应用Owner将变更回退。");
        String body = OperationMailTemple.getInstance().build(ptitle, des, end);
        mailService.sendEmail(new SlbMail().setSubject(title).setBody(body).setRecipients(getRecipients(baseApp, changedApp)));
    }

    private String domainsToString(List<Domain> domains) {
        StringBuilder sb = new StringBuilder(128);
        for (Domain d : domains) {
            sb.append(d.getName()).append(";<br/>");
        }
        return sb.toString();
    }

    private String getGroupLink(Long targetId) {
        return getLinkUrl(targetId, "/portal/group#?env=%s&groupId=%s");
    }

    private String buildLink(String title, String link) {
        return "<a href=\"" + link + "\" style=\"font-size: 16px;color: red;\" target=\"_blank\">" + title + "</a>";
    }

    private List<String> getRecipients(App app, App orgApp) {
        List<String> result = new ArrayList<>();
        if (configHandler.getEnable("path.overlap.notice.slb.team", true)) {
            result.add(configValueService.getTeamMail());
        }
        if (configHandler.getEnable("path.overlap.notice.owner", true)) {
            result.add(app.getOwnerEmail());
            result.add(orgApp.getOwnerEmail());
        }
        if (configHandler.getEnable("path.overlap.notice.owner.backup", false)) {
            result.add(app.getBackupEmail());
            result.add(orgApp.getBackupEmail());
        }
        return result;
    }

    private String toRedSpan(String data) {
        return "<span style=\"color:red;\">" + data + "</span>";
    }

    private String getLinkUrl(Long targetId, String suffix) {
        String portalUrl = configValueService.getSlbPortalUrl();
        if (Strings.isNullOrEmpty(portalUrl) || targetId == null) return null;

        String infoPage = portalUrl + suffix;
        String env = configHandler.getStringValue("slb.portal.url.env", "pro");
        return String.format(infoPage, env, targetId);
    }
}
