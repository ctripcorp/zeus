package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.Authorizer;
import com.ctrip.zeus.dal.core.*;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * User: mag
 * Date: 4/22/2015
 * Time: 1:44 PM
 */
@Component("authorizer")
public class DefaultAuthorizer implements Authorizer {
    private static final String SUPER_ADMIN = "SuperAdmin";
    private static final String SLB_USER = "SlbUser";

    @Resource
    private AuthUserRoleDao userRoleDao;

    @Resource
    private AuthResourceRoleDao resRoleDao;

    @Override
    public void authorize(String userName, String resourceName, String resGroup) throws AuthorizeException {
        try {
            //TODO add some cache
            List<AuthUserRoleDo> userRoles = getUserRoles(userName);
            List<AuthResourceRoleDo> resRoles = resRoleDao.findByResourceName(resourceName,AuthResourceRoleEntity.READSET_FULL);

            //Super Admin has all authorities.
            //If resource role is not config, any role can access it.
            if (isSuperAdmin(userRoles) || resRoles.isEmpty()){
                return;
            }

            List<AuthUserRoleDo> rolesInCommon = findCommonRoles(userRoles, resRoles);
            // if resource group is empty and there is some common roles, then check success.
            if ((resGroup == null || resGroup.isEmpty()) && rolesInCommon.size() > 0){
                return;
            }
            for (AuthUserRoleDo userRoleDo : rolesInCommon) {
                if (groupMatch(userRoleDo.getGroup(), resGroup)){
                    return;
                }
            }
        } catch (Exception e) {
            throw new AuthorizeException(e);
        }
        throw new AuthorizeException("The user:" + userName + " is not authorized.");
    }

    private boolean groupMatch(String userGroup, String resGroup) {
        return resGroup.matches(userGroup);
    }

    private List<AuthUserRoleDo> getUserRoles(String userName) throws DalException {
        List<AuthUserRoleDo> result = userRoleDao.findByUserName(userName, AuthUserRoleEntity.READSET_FULL);
        if (result == null){
            result = new ArrayList<>();
        }
        // add slb user role
        result.add(new AuthUserRoleDo().setUserName(userName)
            .setRoleName(SLB_USER)
            .setGroup(".*"));
        return result;

    }

    private boolean isSuperAdmin(List<AuthUserRoleDo> userRoles) {
        for (AuthUserRoleDo userRole : userRoles) {
            if (SUPER_ADMIN.equals(userRole.getRoleName())){
                return true;
            }
        }
        return false;
    }

    private List<AuthUserRoleDo> findCommonRoles(List<AuthUserRoleDo> userRoles, List<AuthResourceRoleDo> resRoles) {
        List<AuthUserRoleDo> result = new ArrayList<>();

        for (AuthUserRoleDo userRole : userRoles) {
            for (AuthResourceRoleDo resRole : resRoles) {
                if (userRole.getRoleName().equals(resRole.getRoleName())) {
                    result.add(userRole);
                }
            }
        }
        return result;
    }
}
