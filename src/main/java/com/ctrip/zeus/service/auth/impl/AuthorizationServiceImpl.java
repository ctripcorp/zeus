package com.ctrip.zeus.service.auth.impl;

import com.ctrip.zeus.auth.entity.Resource;
import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.RoleGroup;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.auth.AuthorizationService;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mag
 * Date: 4/16/2015
 * Time: 7:21 PM
 */
@Component("authorizationService")
public class AuthorizationServiceImpl implements AuthorizationService {
    @javax.annotation.Resource
    private AuthUserDao userDao;

    @javax.annotation.Resource
    private AuthRoleDao roleDao;

    @javax.annotation.Resource
    private AuthUserRoleDao userRoleDao;

    @javax.annotation.Resource
    private AuthResourceDao resourceDao;

    @javax.annotation.Resource
    private AuthResourceRoleDao resourceRoleDao;

    @javax.annotation.Resource
    private AuthPrivateKeyDao authPrivateKeyDao;

    @Override
    public List<Role> getAllRoles() throws Exception {
        List<AuthRoleDo> authRoleDoList = roleDao.findAll(AuthRoleEntity.READSET_FULL);
        List<Role> result = new ArrayList<>();
        for (AuthRoleDo roleDo : authRoleDoList) {
            result.add(C.toRole(roleDo));
        }
        return result;
    }

    @Override
    public List<User> getAllUsers() throws Exception {
        List<User> result = new ArrayList<>();
        List<AuthUserDo> userDoList = userDao.findAll(AuthUserEntity.READSET_FULL);
        for (AuthUserDo authUserDo : userDoList) {
            User user = getUser(authUserDo.getUserName());
            result.add(user);
        }
        return result;
    }


    @Override
    public List<String> getUsersByRoleGroup(String roleName, String groupName) throws Exception {
        List<AuthUserRoleDo> userRoleDoList = userRoleDao.findByRoleGroup(roleName, groupName, AuthUserRoleEntity.READSET_FULL);
        List<String> result = new ArrayList<>();
        for (AuthUserRoleDo authUserRoleDo : userRoleDoList) {
            result.add(authUserRoleDo.getUserName());
        }
        return result;
    }

    @Override
    public User getUser(String userName) throws Exception {
        User user = new User().setUserName(userName);
        List<AuthUserRoleDo> userRoleDoList = userRoleDao.findByUserName(userName, AuthUserRoleEntity.READSET_FULL);
        for (AuthUserRoleDo authUserRoleDo : userRoleDoList) {
            user.addRoleGroup(new RoleGroup().setRole(new Role().setRoleName(authUserRoleDo.getRoleName()))
                    .setGroupName(authUserRoleDo.getGroup()));
        }
        return user;
    }

    @Override
    public void addUser(User user) throws Exception {
        userDao.insert(C.toUserDo(user));
        List<RoleGroup> roles = user.getRoleGroups();
        addUserRoles(user.getUserName(), roles);
    }

    private void addUserRoles(String userName, List<RoleGroup> roles) throws Exception {
        for (RoleGroup roleGroup : roles) {
            String roleName = roleGroup.getRole().getRoleName();
            checkRoleExit(roleName);
            userRoleDao.insert(new AuthUserRoleDo().setUserName(userName)
                    .setRoleName(roleName)
                    .setGroup(roleGroup.getGroupName()));
        }
    }

    private void checkRoleExit(String roleName) throws Exception {
        AuthRoleDo roleDo = roleDao.findByRoleName(roleName, AuthRoleEntity.READSET_FULL);
        if (roleDo == null) {
            throw new IllegalStateException("The role is not exist:" + roleName);
        }
    }

    @Override
    public void deleteUser(String userName) throws Exception {
        userDao.deleteByName(new AuthUserDo().setUserName(userName));
        userRoleDao.deleteByName(new AuthUserRoleDo().setUserName(userName));
    }

    @Override
    public void updateUser(User user) throws Exception {
        userDao.updateByName(C.toUserDo(user), AuthUserEntity.UPDATESET_FULL);
        userRoleDao.deleteByName(new AuthUserRoleDo().setUserName(user.getUserName()));
        addUserRoles(user.getUserName(), user.getRoleGroups());
    }

    @Override
    public void addRole(Role role) throws Exception {
        AuthRoleDo tmp = roleDao.findByRoleName(role.getRoleName(),AuthRoleEntity.READSET_FULL);
        if (tmp!=null)
        {
            throw new ValidationException("Role Name is already exist!");
        }
        roleDao.insert(C.toRoleDo(role));
    }
    @Override
    public void updateRole(Role role) throws Exception {
        roleDao.updateByName(C.toRoleDo(role),AuthRoleEntity.UPDATESET_FULL);
    }

    @Override
    public void deleteRole(String role) throws Exception {
        roleDao.deleteByName(new AuthRoleDo().setRoleName(role));
    }

    @Override
    public AuthPrivateKeyDo addPrivateKey(String key) throws Exception {
        authPrivateKeyDao.insert(new AuthPrivateKeyDo().setPrivateKey(key));
        return authPrivateKeyDao.findFirst(AuthPrivateKeyEntity.READSET_FULL);
    }

    @Override
    public List<Resource> getAllResources() throws Exception {
        List<Resource> result = new ArrayList<>();
        List<AuthResourceDo> resourceDoList = resourceDao.findAll(AuthResourceEntity.READSET_FULL);
        for (AuthResourceDo resourceDo : resourceDoList) {
            List<AuthResourceRoleDo> authResourceRoleDos = resourceRoleDao.findByResourceName(resourceDo.getResourceName(),AuthResourceRoleEntity.READSET_FULL);
            Resource r = C.toResource(resourceDo);
            if (authResourceRoleDos!=null && authResourceRoleDos.size()>0)
            {
                r.setRoleName(authResourceRoleDos.get(0).getRoleName());
            }
            result.add(r);
        }
        return result;
    }

    @Override
    public List<String> getRolesForResource(String resourceName) throws Exception {
        List<AuthResourceRoleDo> resourceRoleDoList = resourceRoleDao.findByResourceName(resourceName, AuthResourceRoleEntity.READSET_FULL);
        List<String> result = new ArrayList<>();
        for (AuthResourceRoleDo resourceRoleDo : resourceRoleDoList) {
            result.add(resourceRoleDo.getRoleName());
        }
        return result;
    }

    @Override
    public List<Resource> getResourcesByRole(String roleName) throws Exception {
        List<AuthResourceRoleDo> resourceRoleDoList = resourceRoleDao.findByRoleName(roleName, AuthResourceRoleEntity.READSET_FULL);
        List<Resource> result = new ArrayList<>();
        for (AuthResourceRoleDo resourceRoleDo : resourceRoleDoList) {
            AuthResourceDo resDo = resourceDao.findByResourceName(resourceRoleDo.getResourceName(), AuthResourceEntity.READSET_FULL);
            result.add(C.toResource(resDo));
        }
        return result;
    }

    @Override
    public void addResource(Resource resource) throws Exception {
        AuthResourceDo tmp = resourceDao.findByResourceName(resource.getResourceName(),AuthResourceEntity.READSET_FULL);
        if (null != tmp)
        {
            throw new ValidationException("Resource is already exist!");
        }
        resourceDao.insert(C.toResourceDo(resource));
        resourceRoleDao.insert(new AuthResourceRoleDo()
                .setResourceName(resource.getResourceName()).setRoleName(resource.getRoleName()));
    }

    @Override
    public void deleteResource(String resourceName) throws Exception {
        resourceDao.deleteByResourceName(new AuthResourceDo().setResourceName(resourceName));
        resourceRoleDao.deleteByResourceName(new AuthResourceRoleDo().setResourceName(resourceName));
    }

    @Override
    public void updateResource(Resource resource) throws Exception {
        resourceDao.updateByName(C.toResourceDo(resource), AuthResourceEntity.UPDATESET_FULL);
        resourceRoleDao.updateByResourceName(new AuthResourceRoleDo().setRoleName(resource.getRoleName())
                        .setResourceName(resource.getResourceName()),AuthResourceRoleEntity.UPDATESET_FULL);
    }

    @Override
    public void updateResourceRoles(String resourceName, List<String> roles) throws Exception {
        resourceRoleDao.deleteByResourceName(new AuthResourceRoleDo().setResourceName(resourceName));
        for (String role : roles) {
            resourceRoleDao.insert(new AuthResourceRoleDo()
                    .setResourceName(resourceName)
                    .setRoleName(role));
        }
    }
}
