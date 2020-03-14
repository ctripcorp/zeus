package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.auth.entity.Operation;
import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.model.model.App;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.service.app.AppService;
import com.ctrip.zeus.service.auth.*;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.handler.GroupQuery;
import com.ctrip.zeus.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2016/10/21.
 */
@Service("authAutoFillConsumer")
public class OwnerAuthAutoFillConsumer extends AbstractConsumer {
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private GroupQuery groupQuery;
    @Autowired
    private AppService appService;
    @Resource
    private ConfigHandler configHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onUpdateGroup(List<Message> messages) {
        autoFillAuth(messages);
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        autoFillAuth(messages);
    }

    @Override
    public void onNewPolicy(List<Message> messages) {
        if (!configHandler.getEnable("AuthAutoFillConsumer", true)) {
            return;
        }
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            String user = slbMessageData.getUser();
            Long pid = message.getTargetId();
            if (user == null || pid == null || pid <= 0) {
                continue;
            }
            try {
                User userData = userService.getUser(user);
                List<Operation> ops = getDefaultDataResource();
                boolean flag = true;
                for (DataResource dr : userData.getDataResources()) {
                    if (dr.getResourceType().equalsIgnoreCase(ResourceDataType.Policy.getType()) && dr.getData().equalsIgnoreCase(pid.toString())) {
                        for (Operation op : ops) {
                            if (!dr.getOperations().contains(op)) {
                                dr.addOperation(op);
                            }
                        }
                        flag = false;
                    }
                }
                if (flag) {
                    DataResource dr = new DataResource().setData(pid.toString()).setResourceType(ResourceDataType.Policy.getType());
                    dr.getOperations().addAll(ops);
                    userData.addDataResource(dr);
                }
                userService.updateUser(userData);
            } catch (Exception e) {
                logger.error("Auth Auto Fill For Policy Failed.User:" + user + ";Policy:" + pid, e);
            }
        }
    }

    @Override
    public void onNewDr(List<Message> messages){
        if (!configHandler.getEnable("AuthAutoFillConsumer", true)) {
            return;
        }
        for (Message message : messages) {
            SlbMessageData slbMessageData = MessageUtil.parserSlbMessageData(message.getTargetData());
            if (slbMessageData == null || !slbMessageData.getSuccess()) {
                continue;
            }
            String user = slbMessageData.getUser();
            Long pid = message.getTargetId();
            if (user == null || pid == null || pid <= 0) {
                continue;
            }
            try {
                User userData = userService.getUser(user);
                List<Operation> ops = getDefaultDataResource();
                boolean flag = true;
                for (DataResource dr : userData.getDataResources()) {
                    if (dr.getResourceType().equalsIgnoreCase(ResourceDataType.Dr.getType()) && dr.getData().equalsIgnoreCase(pid.toString())) {
                        for (Operation op : ops) {
                            if (!dr.getOperations().contains(op)) {
                                dr.addOperation(op);
                            }
                        }
                        flag = false;
                    }
                }
                if (flag) {
                    DataResource dr = new DataResource().setData(pid.toString()).setResourceType(ResourceDataType.Dr.getType());
                    dr.getOperations().addAll(ops);
                    userData.addDataResource(dr);
                }
                userService.updateUser(userData);
            } catch (Exception e) {
                logger.error("Auth Auto Fill For Dr Failed.User:" + user + ";Dr:" + pid, e);
            }
        }
    }

    private List<Operation> getDefaultDataResource() {
        List<Operation> res = new ArrayList<>();
        res.add(new Operation().setType(ResourceOperationType.ACTIVATE.getType()));
        res.add(new Operation().setType(ResourceOperationType.DEACTIVATE.getType()));
        res.add(new Operation().setType(ResourceOperationType.READ.getType()));
        res.add(new Operation().setType(ResourceOperationType.UPDATE.getType()));
        return res;
    }

    private void autoFillAuth(List<Message> messages) {
        if (!configHandler.getEnable("auth.auto.fill.group", true)) {
            return;
        }
        Role visitor = null;
        for (Message msg : messages) {
            try {
                if (visitor == null) {
                    visitor = roleService.getRole(AuthDefaultValues.SLB_VISITOR_USER);
                }
                SlbMessageData data = MessageUtil.parserSlbMessageData(msg.getTargetData());
                if (data != null && data.getSuccess() != null && data.getSuccess()) {
                    String appId = groupQuery.getAppId(msg.getTargetId());
                    App app = appService.getAppByAppid(appId);
                    if (app == null) {
                        continue;
                    }

                    User user = userService.getUser(app.getOwner());
                    if (configHandler.getEnable("auth.auto.fill.visitor.role", true)) {
                        if (user == null) {
                            user = new User();
                            user.setUserName(app.getOwner());
                            user.setEmail(app.getOwnerEmail());
                            user.setBu(app.getSbu());
                            user.addRole(visitor);
                            userService.newUser(user);
                        } else {
                            if (!user.getRoles().contains(visitor)) {
                                user.addRole(visitor);
                                userService.updateUser(user);
                            }
                        }
                    }

                    if (configHandler.getEnable("auth.auto.fill.group.owner", true)) {
                        if (user == null) {
                            user = new User();
                            user.setUserName(app.getOwner());
                            user.setEmail(app.getOwnerEmail());
                            user.setBu(app.getSbu());
                            user.addDataResource(createDataResource(msg.getTargetId()));
                            user.addRole(visitor);
                            userService.newUser(user);
                        } else {
                            boolean alreadyAuth = false;
                            DataResource defaultData = createDataResource(msg.getTargetId());
                            for (DataResource dr : user.getDataResources()) {
                                if (dr.getResourceType().equals(ResourceDataType.Group.getType()) && dr.getData().equals(msg.getTargetId().toString())) {
                                    if (!dr.getOperations().containsAll(defaultData.getOperations())) {
                                        for (Operation op : defaultData.getOperations()) {
                                            if (!dr.getOperations().contains(op)) {
                                                dr.addOperation(op);
                                            }
                                        }
                                        if (!user.getRoles().contains(visitor)) {
                                            user.addRole(visitor);
                                        }
                                        userService.updateUser(user);
                                    }
                                    alreadyAuth = true;
                                    break;
                                }
                            }
                            if (!alreadyAuth) {
                                if (!user.getRoles().contains(visitor)) {
                                    user.addRole(visitor);
                                }
                                user.addDataResource(defaultData);
                                userService.updateUser(user);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                logger.warn("Auto Fill Auth Fail for Group. GroupID:" + msg.getTargetId());
            }
        }
    }

    private DataResource createDataResource(Long id) {
        DataResource dataResource = new DataResource();
        dataResource.setResourceType(ResourceDataType.Group.getType());
        dataResource.setData(id.toString());
        dataResource.addOperation(new Operation().setType(ResourceOperationType.ACTIVATE.getType()));
        dataResource.addOperation(new Operation().setType(ResourceOperationType.UPDATE.getType()));
        dataResource.addOperation(new Operation().setType(ResourceOperationType.READ.getType()));
        dataResource.addOperation(new Operation().setType(ResourceOperationType.OP_MEMBER.getType()));
        dataResource.addOperation(new Operation().setType(ResourceOperationType.PROPERTY.getType()));
        return dataResource;
    }
}
