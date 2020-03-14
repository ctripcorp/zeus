package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.config.ConfigValueService;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.HealthCheck;
import com.ctrip.zeus.model.queue.GroupData;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.model.status.GroupServerStatus;
import com.ctrip.zeus.model.status.GroupStatus;
import com.ctrip.zeus.service.aop.OperationLog.OperationLogType;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.auth.UserService;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.service.mail.templet.OperationMailTemple;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.service.operationLog.OperationLogService;
import com.ctrip.zeus.service.operationLog.OperationType;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.tag.PropertyNames;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import com.ctrip.zeus.util.MessageUtil;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fanqq on 2016/11/2.
 */
@Service("operationLogConsumer")
public class OperationLogConsumer extends AbstractConsumer {
    @Resource
    protected OperationLogService operationLogService;
    @Autowired
    private GroupRepository groupRepository;
    @Resource
    protected GroupQuery groupQuery;
    @Resource
    protected PropertyService propertyService;
    @Resource
    protected UserService userService;
    @Autowired
    protected AppService appService;
    @Resource
    protected MailService mailService;
    @Resource
    protected ConfigHandler configHandler;
    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private TagService tagService;
    @Autowired
    protected ConfigValueService configValueService;

    private static final String AUTO_MANAGED_TAG = "autoManaged";
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onUpdateGroup(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onDeleteGroup(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onUpdatePolicy(List<Message> messages) {
        addOperationLog(messages, OperationLogType.POLICY);
    }

    @Override
    public void onNewPolicy(List<Message> messages) {
        addOperationLog(messages, OperationLogType.POLICY);
    }

    @Override
    public void onDeletePolicy(List<Message> messages) {
        addOperationLog(messages, OperationLogType.POLICY);
    }


    @Override
    public void onUpdateVs(List<Message> messages) {
        addOperationLog(messages, OperationLogType.VS);
    }

    @Override
    public void onNewVs(List<Message> messages) {
        addOperationLog(messages, OperationLogType.VS);
    }

    @Override
    public void onDeleteVs(List<Message> messages) {
        addOperationLog(messages, OperationLogType.VS);
    }

    @Override
    public void onNewSlb(List<Message> messages) {
        addOperationLog(messages, OperationLogType.SLB);
    }

    @Override
    public void onUpdateSlb(List<Message> messages) {
        addOperationLog(messages, OperationLogType.SLB);
    }

    @Override
    public void onDeleteSlb(List<Message> messages) {
        addOperationLog(messages, OperationLogType.SLB);
    }

    @Override
    public void onOpsPull(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onOpsMember(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onOpsServer(List<Message> messages) {
        addOperationLog(messages, OperationLogType.SERVER);
    }

    @Override
    public void onOpsHealthy(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onActivateGroup(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onDeactivateGroup(List<Message> messages) {
        addOperationLog(messages, OperationLogType.GROUP);
    }

    @Override
    public void onActivateVs(List<Message> messages) {
        addOperationLog(messages, OperationLogType.VS);
    }

    @Override
    public void onDeactivateVs(List<Message> messages) {
        addOperationLog(messages, OperationLogType.VS);
    }

    @Override
    public void onActivateSlb(List<Message> messages) {
        addOperationLog(messages, OperationLogType.SLB);
    }

    @Override
    public void onDeactivateSlb(List<Message> messages) {
        addOperationLog(messages, OperationLogType.SLB);
    }

    @Override
    public void onActivatePolicy(List<Message> messages) {
        addOperationLog(messages, OperationLogType.POLICY);
    }

    @Override
    public void onDeactivatePolicy(List<Message> messages) {
        addOperationLog(messages, OperationLogType.POLICY);
    }

    @Override
    public void onAuthApply(List<Message> messages) {
        addOperationLog(messages, OperationLogType.AUTH);
    }

    @Override
    public void onSandBox(List<Message> messages) {
        addOperationLog(messages, OperationLogType.SANDBOX);
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
    public void onSlbCreatingFlow(List<Message> messages) {
        addOperationLog(messages, OperationLogType.FLOW_SLB_CREATING);
    }

    @Override
    public void onSlbShardingFlow(List<Message> messages) {
        addOperationLog(messages, OperationLogType.FLOW_SLB_SHARDING);
    }

    @Override
    public void onCertOperation(List<Message> messages) {
        addOperationLog(messages, OperationLogType.VS);
    }

    @Override
    public void onSlbDestoryFlow(List<Message> messages) {
        addOperationLog(messages, OperationLogType.FLOW_SLB_DESTROY);
    }


    @Override
    public void onVSSplitFlow(List<Message> messages) {
        addOperationLog(messages, OperationLogType.FLOW_VS_SPLIT);
    }

    @Override
    public void onVSMergeFlow(List<Message> messages) {
        addOperationLog(messages, OperationLogType.FLOW_VS_MERGE);
    }


    protected void addOperationLog(List<Message> messages, OperationLogType type) {
        for (Message msg : messages) {
            SlbMessageData messageData = MessageUtil.parserSlbMessageData(msg.getTargetData());
            String operation = getOperation(messageData);
            operationLogService.insert(type.toString(), msg.getTargetId().toString(), operation, msg.getTargetData(), messageData.getUser()
                    , messageData.getClientIp(), messageData.getSuccess(), messageData.getErrorMessage(), new Date());
            operationNotice(msg, messageData, type);
        }
    }

    protected void operationNotice(Message msg, SlbMessageData messageData, OperationLogType type) {
        if (!messageData.getSuccess()) return;
        if (!configHandler.getEnable("operation.mail.notice", true)) return;

        switch (type) {
            case SERVER:
                noticeServerOp(msg, messageData);
                break;
            case GROUP:
                noticeGroupOp(msg, messageData);
                break;
        }
    }

    private void noticeGroupOp(Message msg, SlbMessageData messageData) {
        //case: Group Operations
        try {
            String appId = groupQuery.getAppId(msg.getTargetId());
            App app = appService.getAppByAppid(appId);
            List<String> receivers = new ArrayList<>();
            if (app != null && configHandler.getEnable("operation.mail.notice.user", true)) {
                receivers.add(app.getOwnerEmail());
                String backup = app.getBackupEmail();
                if (backup != null) {
                    String[] tmp = backup.split(";");
                    Collections.addAll(receivers, tmp);
                }
            }
            if (!messageData.getUri().contains("fall") && !messageData.getUri().contains("raise") &&
                    configHandler.getEnable("operation.mail.notice.slbTeam", true)) {
                receivers.add(configValueService.getTeamMail());
            }

            List<String> tags = tagService.getTags("group", msg.getTargetId());
            if (tags.contains(AUTO_MANAGED_TAG) && configHandler.getEnable("skip.auto.managed.tag", true)) {
                return;
            }

            LinkedHashMap<String, String> des = new LinkedHashMap<>();
            String appName = app == null ? "-" : app.getChineseName() + "(" + appId + ")";
            des.put("应用名称(app)", appName);
            des.put("link_应用名称(app)", getGroupLink(msg.getTargetId()));
            des.put("Env", configHandler.getStringValue("slb.portal.url.env", "pro").toUpperCase());
            String title = "";
            String ptitle = "";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            switch (messageData.getUri()) {
                case "/api/group/addMember":
                    des.put("IP", messageData.getIps().toString());
                    des.put("扩容时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
                    title = "[SLB操作通知]应用[" + appName + "]扩容" + messageData.getIps().size() + "台服务器";
                    ptitle = "[SLB操作通知]应用<span style=\"color:red;\">扩容</span>" + messageData.getIps().size() + "台服务器";
                    break;
                case "/api/group/removeMember":
                    des.put("IP", messageData.getIps().toString());
                    des.put("缩容时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
                    title = "[SLB操作通知]应用[" + appName + "]缩容" + messageData.getIps().size() + "台服务器";
                    ptitle = "[SLB操作通知]应用<span style=\"color:red;\">缩容</span>" + messageData.getIps().size() + "台服务器";
                    break;
                case "/api/group/updateCheckUri":
                    Group g = groupRepository.getById(msg.getTargetId());
                    Group old = groupRepository.getByKey(new IdVersion(msg.getTargetId(), g.getVersion() - 1));
                    String oldUri = old.getHealthCheck() == null ? "未配置" : old.getHealthCheck().getUri();
                    String uri = g.getHealthCheck() == null ? "未配置" : g.getHealthCheck().getUri();
                    des.put("健康监测URL", uri.equalsIgnoreCase(oldUri) ? uri : oldUri + "->" + uri);
                    if (g.getHealthCheck() != null) {
                        HealthCheck h = g.getHealthCheck();
                        des.put("健康监测配置", String.format("每%ds一次，%ds超时，连续%d次失败拉出，连续%d成功拉入", h.getIntervals() / 1000,
                                h.getTimeout() / 1000, h.getFails(), h.getPasses()));
                    }
                    des.put("变更时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
                    title = "[SLB操作通知]应用[" + appName + "]健康监测配置变更";
                    ptitle = "[SLB操作通知]<span style=\"color:red;\">健康监测配置变更</span>";
                    break;
                case "/api/op/upMember":
                    des.put("IP", messageData.getIps().toString());
                    des.put("集群可用率", getWorkingPercent(msg.getTargetId()) + "%");
                    des.put("拉入时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
                    des.put("拉入原因", messageData.getDescription());
                    des.put("link_拉入原因", getLogLink(msg.getTargetId()));
                    title = "[SLB操作通知]应用[" + appName + "]服务器拉入" + messageData.getIps().size() + "台服务器";
                    ptitle = "[SLB操作通知]<span style=\"color:red;\">服务器拉入</span>" + messageData.getIps().size() + "台服务器";
                    break;
                case "/api/op/downMember":
                    des.put("IP", messageData.getIps().toString());
                    des.put("集群可用率", getWorkingPercent(msg.getTargetId()) + "%");
                    des.put("拉出时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
                    des.put("拉出原因", messageData.getDescription());
                    des.put("link_拉出原因", getLogLink(msg.getTargetId()));
                    title = "[SLB操作通知]应用[" + appName + "]服务器拉出" + messageData.getIps().size() + "台服务器";
                    ptitle = "[SLB操作通知]<span style=\"color:red;\">服务器拉出</span>" + messageData.getIps().size() + "台服务器";
                    break;
                case "/api/op/raise":
                    if (!needSendMailForFallRaise(msg.getTargetId()) && !configHandler.getEnable("operation.mail.notice.healthy", false)) {
                        return;
                    }
                    des.put("IP", messageData.getIps().toString());
                    des.put("集群可用率", getWorkingPercent(msg.getTargetId()) + "%");
                    des.put("拉出时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
                    des.put("拉出原因", messageData.getDescription());
                    des.put("link_拉出原因", getLogLink(msg.getTargetId()));
                    title = "[SLB操作通知]应用[" + appName + "]健康监测拉入" + messageData.getIps().size() + "台服务器";
                    ptitle = "[SLB操作通知]<span style=\"color:red;\">健康监测拉入</span>" + messageData.getIps().size() + "台服务器";
                    break;
                case "/api/op/fall":
                    if (!needSendMailForFallRaise(msg.getTargetId()) && !configHandler.getEnable("operation.mail.notice.healthy", false)) {
                        return;
                    }
                    des.put("IP", messageData.getIps().toString());
                    des.put("集群可用率", getWorkingPercent(msg.getTargetId()) + "%");
                    des.put("拉出时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
                    des.put("拉出原因", messageData.getDescription());
                    des.put("link_拉出原因", getLogLink(msg.getTargetId()));
                    title = "[SLB操作通知]应用[" + appName + "]健康监测拉出" + messageData.getIps().size() + "台服务器";
                    ptitle = "[SLB操作通知]<span style=\"color:red;\">健康监测拉出</span>" + messageData.getIps().size() + "台服务器";
                    break;
            }

            String body = OperationMailTemple.getInstance().build(ptitle, des);
            mailService.sendEmail(new SlbMail().setSubject(title).setBody(body).setRecipients(receivers));
        } catch (Exception e) {
            logger.error("Send Mail For Operation Failed.MSG:" + msg.toString(), e);
        }
    }

    private boolean needSendMailForFallRaise(Long targetId) throws Exception {
        if (targetId == null) {
            return false;
        }
        Property p = propertyService.getProperty(PropertyNames.NOTICE_FOR_HEALTH_CHECK, targetId, "group");
        return p != null && "true".equalsIgnoreCase(p.getValue().trim());
    }

    private int getWorkingPercent(Long groupId) {
        try {
            GroupStatus groupStatus = groupStatusService.getOfflineGroupStatus(groupId);
            int count = 0;
            for (GroupServerStatus gss : groupStatus.getGroupServerStatuses()) {
                if (gss.getMember() && gss.getServer() && gss.getHealthy() && gss.getPull()) {
                    count++;
                }
            }
            return count * 100 / groupStatus.getGroupServerStatuses().size();
        } catch (Exception e) {
            logger.error("Get group status failed.");
            return -1;
        }
    }

    private void noticeServerOp(Message msg, SlbMessageData messageData) {
        try {
            List<GroupData> groupDatas = messageData.getGroupDatas();
            Set<String> appIds = new HashSet<>();
            for (GroupData gd : groupDatas) {
                appIds.add(groupQuery.getAppId(gd.getId()));
            }
            List<App> apps = appService.getAllAppsByAppIds(appIds);
            StringBuilder appNames = new StringBuilder(128);
            List<String> receivers = new ArrayList<>();
            for (App app : apps) {
                appNames.append(app.getChineseName()).append("(").append(app.getAppId()).append(") ");
                if (configHandler.getEnable("operation.mail.notice.user", true)) {
                    receivers.add(app.getOwnerEmail());
                    String backup = app.getBackupEmail();
                    if (backup != null) {
                        String[] tmp = backup.split(";");
                        Collections.addAll(receivers, tmp);
                    }
                }
            }
            if (configHandler.getEnable("operation.mail.notice.slbTeam", true)) {
                receivers.add(configValueService.getTeamMail());
            }
            LinkedHashMap<String, String> des = new LinkedHashMap<>();
            String title = "[SLB操作通知]应用服务器被手动拉出";
            des.put("拉出机器", messageData.getIps().toString());
            des.put("受影响应用", appNames.toString());
            des.put("Env", configHandler.getStringValue("slb.portal.url.env", "pro").toUpperCase());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            des.put("拉出时间", messageData.getUser() + "(" + format.format(new Date()) + ")");
            des.put("拉出原因", messageData.getDescription() == null ? "未记录原因" : messageData.getDescription());
            String body = OperationMailTemple.getInstance().build(title, des);
            mailService.sendEmail(new SlbMail().setSubject(title).setBody(body).setRecipients(receivers));
        } catch (Exception e) {
            logger.error("Send Mail For Operation Failed." + msg.toString(), e);
        }
    }

    private String getGroupLink(Long targetId) {
        if (targetId == null) return null;
        return getLinkUrl(targetId.toString(), "/portal/group#?env=%s&groupId=%s");
    }

    private String getLogLink(Long targetId) {
        if (targetId == null) return null;
        return getLinkUrl(targetId.toString(), "/portal/group/log#?env=%s&groupId=%d&groupOperations=downServer&groupOperations=downMember");
    }

    public String getOperation(SlbMessageData messageData) {
        String type = messageData.getType();
        if (type == null) {
            type = messageData.getUri();
        }
        switch (type) {
            case "/api/group/new":
            case "/group/related/new":
            case "/api/policy/new":
            case "/api/dr/new":
            case "/api/vgroup/new":
            case "/api/vs/new":
            case "/api/route/vs/new":
            case "/api/slb/new":
                return OperationType.NEW;
            case "/api/group/update":
            case "/api/vgroup/update":
            case "/api/vs/update":
            case "/api/slb/update":
            case "/api/policy/update":
            case "/api/dr/update":
                return OperationType.UPDATE;
            case "/api/group/addMember":
                return OperationType.ADD_MEMBER;
            case "/api/group/updateMember":
                return OperationType.UPDATE_MEMBER;
            case "/api/group/removeMember":
                return OperationType.REMOVE_MEMBER;
            case "/api/group/updateCheckUri":
                return OperationType.UPDATE_CHECK_URI;
            case "/api/group/bindVs":
                return OperationType.BIND_VS;
            case "/api/group/unbindVs":
                return OperationType.UNBIND_VS;
            case "/api/group/delete":
            case "/api/vgroup/delete":
            case "/api/vs/delete":
            case "/api/policy/delete":
            case "/api/slb/delete":
            case "/api/dr/delete":
                return OperationType.DELETE;
            case "/api/vs/addDomain":
                return OperationType.ADD_Domain;
            case "/api/vs/removeDomain":
                return OperationType.REMOVE_Domain;
            case "/api/slb/addServer":
                return OperationType.ADD_Server;
            case "/api/slb/removeServer":
                return OperationType.REMOVE_Server;
            case "/api/op/pullIn":
                return OperationType.PULL_IN;
            case "/api/op/pullOut":
                return OperationType.PULL_OUT;
            case "/api/op/upMember":
                return OperationType.UP_MEMBER;
            case "/api/op/downMember":
                return OperationType.DOWN_MEMBER;
            case "/api/op/raise":
                return OperationType.RAISE;
            case "/api/op/fall":
                return OperationType.FALL;
            case "/api/op/upServer":
                return OperationType.UP_SERVER;
            case "/api/op/downServer":
                return OperationType.DOWN_SERVER;
            case "/api/activate/group":
            case "/api/activate/policy":
            case "/api/activate/vs":
            case "/api/activate/slb":
            case "/api/activate/dr":
                return OperationType.ACTIVATE;
            case "/api/deactivate/group":
            case "/api/deactivate/policy":
            case "/api/deactivate/vs":
            case "/api/deactivate/slb":
            case "/api/deactivate/dr":
                return OperationType.DEACTIVATE;
            case "/api/auth/apply":
                return OperationType.AUTH_APPLY;
            case "/api/cert/sandbox/install":
                return OperationType.SANDBOX_INSTALL;
            case "/api/cert/sandbox/uninstall":
                return OperationType.SANDBOX_UNINSTALL;
            case "/api/cert/upload":
                return OperationType.CERT_UPLOAD;
            case "/api/cert/load":
                return OperationType.CERT_LOAD;
            case "/api/cert/activate":
                return OperationType.CERT_ACTIVATE;
            case "/api/cert/canary":
                return OperationType.CERT_CANARY;
            case "/api/cert/rollback":
                return OperationType.CERT_ROLLBACK;
            case "/api/flow/slb/creating/new":
                return OperationType.FLOW_SLB_CREATING_NEW;
            case "/api/flow/slb/creating/update":
                return OperationType.FLOW_SLB_CREATING_UPDATE;
            case "/api/flow/slb/creating/createGroup":
                return OperationType.FLOW_SLB_CREATING_CREATE_GROUP;
            case "/api/flow/slb/creating/expand":
                return OperationType.FLOW_SLB_CREATING_EXPAND;
            case "/api/flow/slb/creating/createRoute":
                return OperationType.FLOW_SLB_CREATING_CREATE_ROUTE;
            case "/api/flow/slb/creating/config":
                return OperationType.FLOW_SLB_CREATING_CONFIG;
            case "/api/flow/slb/sharding/new":
                return OperationType.FLOW_SLB_SHARDING_NEW;
            case "/api/flow/slb/sharding/update":
                return OperationType.FLOW_SLB_SHARDING_UPDATE;
            case "/api/flow/slb/sharding/vs/migration":
                return OperationType.FLOW_SLB_SHARDING_START_VS_MIGRATION;
            case "/api/flow/slb/sharding/create/slb":
                return OperationType.FLOW_SLB_SHARDING_START_CREATE_SLB;
            case "/api/flow/slb/destroy/new":
                return OperationType.NEW;
            case "/api/flow/slb/destroy/update":
                return OperationType.UPDATE;
            case "/api/flow/slb/destroy/delete":
                return OperationType.DELETE;
            case "/api/flow/slb/destroy/delete/config":
                return OperationType.FLOW_SLB_DESTROY_DELETE_CONFIG;
            case "/api/flow/slb/destroy/delete/resources":
                return OperationType.FLOW_SLB_DESTROY_DELETE_RESOURCES;
            case "/api/flow/vs/split/new":
                return OperationType.NEW;
            case "/api/flow/vs/split/update":
                return OperationType.UPDATE;
            case "/api/flow/vs/split/delete":
                return OperationType.DELETE;
            case "/api/flow/vs/split/bind/new/vs":
                return OperationType.FLOW_VS_SPLIT_BIND_NEW_VS;
            case "/api/flow/vs/split/split":
                return OperationType.FLOW_VS_SPLIT_SPLIT;
            case "/api/flow/vs/merge/new":
                return OperationType.NEW;
            case "/api/flow/vs/merge/update":
                return OperationType.UPDATE;
            case "/api/flow/vs/merge/delete":
                return OperationType.DELETE;
            case "/api/flow/vs/merge/bind/new/vs":
                return OperationType.FLOW_VS_MERGE_BIND_NEW_VS;
            case "/api/flow/vs/merge/merge":
                return OperationType.FLOW_VS_MERGE_MERGE;
            case "/api/rule/set":
            case "/api/rule/new":
            case "/api/rule/update":
                return OperationType.RULE_SET;
            case "/api/rule/clear":
            case "/api/rule/delete":
                return OperationType.RULE_DELETE;
            default:
                return "unknown";
        }
    }

    protected String getLinkUrl(String targetId, String suffix) {
        String portalUrl = configValueService.getSlbPortalUrl();
        if (Strings.isNullOrEmpty(portalUrl) || targetId == null) return null;

        String infoPage = portalUrl + suffix;
        String env = configHandler.getStringValue("slb.portal.url.env", "pro");
        return String.format(infoPage, env, targetId);
    }
}
