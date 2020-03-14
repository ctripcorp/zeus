package com.ctrip.zeus;

import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.auth.entity.Operation;
import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.dao.entity.AuthRoleE;
import com.ctrip.zeus.dao.entity.AuthRoleResourceR;
import com.ctrip.zeus.dao.entity.AuthUserE;
import com.ctrip.zeus.dao.entity.AuthUserResourceR;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class C {

    public static User toUser(AuthUserE user) {
        return new User()
                .setUserName(user.getName())
                .setBu(user.getBu())
                .setId(user.getId())
                .setChineseName(user.getChineseName())
                .setEmail(user.getEmail());
    }

    public static DataResource toAuthResource(AuthUserResourceR resourceR) {
        DataResource res = new DataResource()
                .setData(resourceR.getData())
                .setResourceType(resourceR.getType());
        if (resourceR.getOperation() != null) {
            for (String ops : resourceR.getOperation().split(";")) {
                res.addOperation(new Operation().setType(ops));
            }
        }
        return res;
    }

    public static DataResource toAuthResource(AuthRoleResourceR resourceDo) {
        DataResource res = new DataResource()
                .setData(resourceDo.getData())
                .setResourceType(resourceDo.getType());
        if (resourceDo.getOperation() != null) {
            for (String ops : resourceDo.getOperation().split(";")) {
                res.addOperation(new Operation().setType(ops));
            }
        }
        return res;
    }

    public static AuthUserE toAuthUserE(User user) {
        AuthUserE res = new AuthUserE();
        res.setId(user.getId() == null ? 0L : user.getId());
        res.setBu(user.getBu());
        res.setName(user.getUserName());
        res.setChineseName(user.getChineseName());
        res.setEmail(user.getEmail());
        return res;
    }

    public static AuthUserResourceR toUserResourceR(DataResource resource, Long userId) {
        AuthUserResourceR res = new AuthUserResourceR();
        res.setUserId(userId);
        res.setData(resource.getData());
        res.setType(resource.getResourceType());
        StringBuilder stringBuilder = new StringBuilder(128);
        if (resource.getOperations().size() > 0) {
            for (Operation o : resource.getOperations()) {
                stringBuilder.append(o.getType()).append(";");
            }
        }
        res.setOperation(stringBuilder.toString());
        return res;
    }

    public static AuthRoleResourceR toAuthRoleResourceR(DataResource resource, Long roleId) {
        AuthRoleResourceR res = new AuthRoleResourceR();
        res.setRoleId(roleId);
        res.setData(resource.getData());
        res.setType(resource.getResourceType());
        StringBuilder stringBuilder = new StringBuilder(128);
        if (resource.getOperations().size() > 0) {
            for (Operation o : resource.getOperations()) {
                stringBuilder.append(o.getType()).append(";");
            }
        }
        res.setOperation(stringBuilder.toString());
        return res;
    }

    public static Role toRole(AuthRoleE roleDo) {
        return new Role()
                .setId(roleDo.getId())
                .setRoleName(roleDo.getName())
                .setDiscription(roleDo.getDiscription());
    }
}


