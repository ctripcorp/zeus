package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.queue.entity.Message;
import com.ctrip.zeus.queue.entity.SlbMessageData;
import com.ctrip.zeus.service.aop.OperationLog.OperationLogType;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.operationLog.OperationLogService;
import com.ctrip.zeus.util.MessageUtil;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by fanqq on 2016/11/2.
 */
@Service("operationLogConsumer")
public class OperationLogConsumer extends AbstractConsumer {
    @Resource
    private OperationLogService operationLogService;

    private DynamicBooleanProperty operationLogNewVersion = DynamicPropertyFactory.getInstance().getBooleanProperty("new.operation.log", false);


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

    private void addOperationLog(List<Message> messages, OperationLogType type) {
        if (!operationLogNewVersion.get()) {
            return;
        }
        for (Message msg : messages) {
            SlbMessageData messageData = MessageUtil.parserSlbMessageData(msg.getTargetData());
            String operation;
            switch (messageData.getUri()) {
                case "/api/group/new":
                case "/api/vgroup/new":
                case "/api/vs/new":
                case "/api/slb/new":
                    operation = "new";
                    break;
                case "/api/group/update":
                case "/api/vgroup/update":
                case "/api/vs/update":
                case "/api/slb/update":
                    operation = "update";
                    break;
                case "/api/group/addMember":
                    operation = "addMember";
                    break;
                case "/api/group/updateMember":
                    operation = "updateMember";
                    break;
                case "/api/group/removeMember":
                    operation = "removeMember";
                    break;
                case "/api/group/updateCheckUri":
                    operation = "updateCheckUri";
                    break;
                case "/api/group/bindVs":
                    operation = "bindVs";
                    break;
                case "/api/group/unbindVs":
                    operation = "unbindVs";
                    break;
                case "/api/group/delete":
                case "/api/vgroup/delete":
                case "/api/vs/delete":
                case "/api/slb/delete":
                    operation = "delete";
                    break;
                case "/api/vs/addDomain":
                    operation = "addDomain";
                    break;
                case "/api/vs/removeDomain":
                    operation = "removeDomain";
                    break;
                case "/api/slb/addServer":
                    operation = "addServer";
                    break;
                case "/api/slb/removeServer":
                    operation = "removeServer";
                    break;
                case "/api/op/pullIn":
                    operation = "pullIn";
                    break;
                case "/api/op/pullOut":
                    operation = "pullOut";
                    break;
                case "/api/op/upMember":
                    operation = "upMember";
                    break;
                case "/api/op/downMember":
                    operation = "downMember";
                    break;
                case "/api/op/raise":
                    operation = "raise";
                    break;
                case "/api/op/fall":
                    operation = "fall";
                    break;
                case "/api/op/upServer":
                    operation = "upServer";
                    break;
                case "/api/op/downServer":
                    operation = "downServer";
                    break;
                case "/api/activate/group":
                case "/api/activate/vs":
                case "/api/activate/slb":
                    operation = "activate";
                    break;
                case "/api/deactivate/group":
                case "/api/deactivate/vs":
                case "/api/deactivate/slb":
                    operation = "deactivate";
                    break;
                default:
                    operation = "unknown";
            }
            operationLogService.insert(type.toString(), msg.getTargetId().toString(), operation, msg.getTargetData(), messageData.getUser()
                    , messageData.getClientIp(), messageData.getSuccess(), messageData.getErrorMessage(), new Date());
        }
    }
}
