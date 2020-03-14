package com.ctrip.zeus.service.auth.impl;

import com.ctrip.zeus.auth.entity.Operation;
import com.ctrip.zeus.auth.impl.AuthorizeException;
import com.ctrip.zeus.service.auth.*;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/7/21.
 */
@Service("authService")
public class AuthServiceImpl implements AuthService {
    @Resource
    private UserAuthCache userAuthCache;

    private DynamicBooleanProperty enable = DynamicPropertyFactory.getInstance().getBooleanProperty("server.authorization.enable", false);
    private DynamicBooleanProperty systemUserSkipp = DynamicPropertyFactory.getInstance().getBooleanProperty("server.authorization.system.user.skip.validate", false);

    private Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, String id) throws AuthorizeException {
        if (!enable.get()) {
            return;
        }
        if (userName == null) {
            throw new AuthorizeException("User is not authorized.Ops:" + ops + " type:" + dataType + " ID:" + id);
        }
        if (userName.equals(AuthDefaultValues.SLB_SERVER_USER)) {
            return;
        }
        if (dataType != ResourceDataType.Auth && systemUserSkipp.get()) {
            if (userName.equals(AuthDefaultValues.SLB_HealthCheck_USER) ||
                    userName.equals(AuthDefaultValues.SLB_OPS_USER) ||
                    userName.equals(AuthDefaultValues.SLB_RELEASE_USER)) {
                return;
            }
        }
        if (id == null) {
            throw new AuthorizeException("Invalidate Auth data resource. User:" + userName + " Ops:" + ops + " type:" + dataType);
        }
        try {
            boolean auth = false;
            switch (dataType) {
                case Ip:
                    if (userAuthCache.getAuthResource(userName).get(dataType.getType()).containsKey(AuthDefaultValues.ALL)) {
                        auth = userAuthCache.getAuthResource(userName).get(dataType.getType()).get(AuthDefaultValues.ALL).getOperations().contains(new Operation().setType(ops.getType()));
                    }
                    if (!auth) {
                        SubnetUtils.SubnetInfo subnetInfo;
                        for (String subnet : userAuthCache.getAuthResource(userName).get(dataType.getType()).keySet()) {
                            try {
                                subnetInfo = new SubnetUtils(subnet).getInfo();
                                if (subnetInfo.isInRange(id)) {
                                    auth = true;
                                    break;
                                }
                            } catch (Exception e) {
                                continue;
                            }
                        }
                    }
                    if (!auth) {
                        throw new AuthorizeException("User is not authorized. User:" + userName + " Ops:" + ops + " Type:" + dataType + " Data:" + id);
                    }
                    break;
                case Group:
                case Slb:
                case Vs:
                case Dr:
                case Policy:
                case Rule:
                case Auth:
                    if (userAuthCache.getAuthResource(userName).get(dataType.getType()).containsKey(AuthDefaultValues.ALL)) {
                        auth = userAuthCache.getAuthResource(userName).get(dataType.getType()).get(AuthDefaultValues.ALL).getOperations().contains(new Operation().setType(ops.getType()));
                    }
                    if (!auth) {
                        auth = userAuthCache.getAuthResource(userName).get(dataType.getType()).get(id).getOperations().contains(new Operation().setType(ops.getType()));
                    }
                    if (!auth) {
                        throw new AuthorizeException("User is not authorized. User:" + userName + " Ops:" + ops + " Type:" + dataType + " Data:" + id);
                    }
                    break;
                default:
                    if (userAuthCache.getAuthResource(userName).get(dataType.getType()).containsKey(AuthDefaultValues.ALL)) {
                        auth = userAuthCache.getAuthResource(userName).get(dataType.getType()).get(AuthDefaultValues.ALL).getOperations().contains(new Operation().setType(ops.getType()));
                    }
                    if (!auth) {
                        auth = userAuthCache.getAuthResource(userName).get(dataType.getType()).get(id).getOperations().contains(new Operation().setType(ops.getType()));
                    }
                    if (!auth) {
                        throw new AuthorizeException("User is not authorized. User:" + userName + " Ops:" + ops + " Type:" + dataType + " Data:" + id);
                    }
                    break;
            }
        } catch (Exception e) {
            if (e instanceof AuthorizeException) {
                throw (AuthorizeException) e;
            }
            logger.warn("Get Auth Info Failed. User:" + userName + " Ops:" + ops + " type:" + dataType + " ID:" + id, e);
            throw new AuthorizeException("User is not authorized. User:" + userName + " Ops:" + ops + " type:" + dataType + " ID:" + id);
        }
    }

    @Override
    public void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, String[] ids) throws AuthorizeException {
        for (String id : ids) {
            authValidate(userName, ops, dataType, id);
        }
    }

    @Override
    public void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, Long id) throws AuthorizeException {
        if (id == null) {
            throw new AuthorizeException("Invalidate Auth data resource. User:" + userName + " Ops:" + ops + " type:" + dataType + " ID: null.");
        }
        authValidate(userName, ops, dataType, id.toString());
    }

    @Override
    public void authValidate(String userName, ResourceOperationType ops, ResourceDataType dataType, Long[] ids) throws AuthorizeException {
        for (Long id : ids) {
            authValidate(userName, ops, dataType, id.toString());
        }
    }

    @Override
    public void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, String id) throws AuthorizeException {
        authValidate(userName, ops, dataType, id);
        authValidate(userName, ResourceOperationType.FORCE, dataType, id);
    }

    @Override
    public void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, String[] id) throws AuthorizeException {
        authValidate(userName, ops, dataType, id);
        authValidate(userName, ResourceOperationType.FORCE, dataType, id);
    }

    @Override
    public void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, Long[] id) throws AuthorizeException {
        authValidate(userName, ops, dataType, id);
        authValidate(userName, ResourceOperationType.FORCE, dataType, id);
    }

    @Override
    public void authValidateWithForce(String userName, ResourceOperationType ops, ResourceDataType dataType, Long id) throws AuthorizeException {
        authValidate(userName, ops, dataType, id);
        authValidate(userName, ResourceOperationType.FORCE, dataType, id);
    }
}
