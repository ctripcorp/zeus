package com.ctrip.zeus.auth.impl;

import com.ctrip.zeus.auth.Authorizer;
import com.ctrip.zeus.dal.core.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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

    private Cache<String, List<AuthResourceRoleDo>> resourceRoleCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private Cache<String, List<AuthUserRoleDo>> userRoleCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    @Override
    public void authorize(String userName, String resourceName, String resGroup) throws AuthorizeException {
        try {
            List<AuthUserRoleDo> userRoles = getUserRoles(userName);
            List<AuthResourceRoleDo> resRoles = getResourceRoles(resourceName);

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

    private List<AuthResourceRoleDo> getResourceRoles(final String resourceName) throws Exception {
        return resourceRoleCache.get(resourceName, new Callable<List<AuthResourceRoleDo>>() {
            @Override
            public List<AuthResourceRoleDo> call() throws Exception {
                return resRoleDao.findByResourceName(resourceName, AuthResourceRoleEntity.READSET_FULL);
            }
        });
    }

    private boolean groupMatch(String userGroup, String resGroup) {
        return resGroup.matches(userGroup);
    }

    //todo: maybe it is better to store the user roles in session
    private List<AuthUserRoleDo> getUserRoles(final String userName) throws Exception {
        return userRoleCache.get(userName, new Callable<List<AuthUserRoleDo>>() {
            @Override
            public List<AuthUserRoleDo> call() throws Exception {
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
        });

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
