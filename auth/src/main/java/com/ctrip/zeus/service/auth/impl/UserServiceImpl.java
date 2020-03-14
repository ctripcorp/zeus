package com.ctrip.zeus.service.auth.impl;

import com.ctrip.zeus.C;
import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.auth.entity.Operation;
import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.AuthUserEMapper;
import com.ctrip.zeus.dao.mapper.AuthUserResourceRMapper;
import com.ctrip.zeus.dao.mapper.AuthUserRoleRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.auth.AuthDataValidate;
import com.ctrip.zeus.service.auth.RoleService;
import com.ctrip.zeus.service.auth.UserService;
import com.ctrip.zeus.util.AssertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2016/7/21.
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Resource
    private RoleService roleService;
    @Resource
    private AuthDataValidate authDataValidate;

    @Resource
    private AuthUserEMapper authUserEMapper;
    @Resource
    private AuthUserResourceRMapper authUserResourceRMapper;
    @Resource
    private AuthUserRoleRMapper authUserRoleRMapper;


    @Override
    public List<User> getUsers() throws Exception {
        return getUsersMybatis();

    }

    private List<User> getUsersMybatis() throws Exception {
        List<User> res = new ArrayList<>();
        List<AuthUserE> users = authUserEMapper.selectByExample(new AuthUserEExample().createCriteria().example());
        for (AuthUserE u : users) {
            res.add(getUserMybatis(u.getId()));
        }
        return res;
    }


    @Override
    public User getUser(Long id) throws Exception {
        return getUserMybatis(id);
    }


    private User getUserMybatis(Long id) throws Exception {
        User user = null;
        //Get user info
        AuthUserE userE = authUserEMapper.selectByPrimaryKey(id);
        if (userE == null) {
            return null;
        }
        user = C.toUser(userE);
        //Get user resources
        List<AuthUserResourceR> resourceRS = authUserResourceRMapper.selectByExample(new AuthUserResourceRExample()
                .createCriteria().andUserIdEqualTo(id).example());
        for (AuthUserResourceR r : resourceRS) {
            user.addDataResource(C.toAuthResource(r));
        }
        //Get user roles
        user.getRoles().addAll(roleService.getRolesByUserIdMybatis(id));
        return user;
    }

    @Override
    public User getUser(String name) throws Exception {
        return getUserMybatis(name);
    }


    private User getUserMybatis(String name) throws Exception {
        User user = null;
        //Get user info
        AuthUserE userE = authUserEMapper.selectOneByExample(new AuthUserEExample().createCriteria().andNameEqualTo(name).example());
        if (userE == null) {
            return null;
        }
        user = C.toUser(userE);
        //Get user resources
        List<AuthUserResourceR> resourceRS = authUserResourceRMapper.selectByExample(new AuthUserResourceRExample()
                .createCriteria().andUserIdEqualTo(user.getId()).example());
        for (AuthUserResourceR r : resourceRS) {
            user.addDataResource(C.toAuthResource(r));
        }
        //Get user roles
        user.getRoles().addAll(roleService.getRolesByUserIdMybatis(user.getId()));
        return user;
    }

    @Override
    public User getUserSimpleInfo(String name) throws Exception {
        AuthUserE userE = authUserEMapper.selectOneByExample(new AuthUserEExample().createCriteria().andNameEqualTo(name).example());
        if (userE == null) {
            return null;
        }
        return C.toUser(userE);
    }

    @Override
    public List<User> getUsersSimpleInfo() throws Exception {
        List<User> res = new ArrayList<>();
        List<AuthUserE> users = authUserEMapper.selectByExample(new AuthUserEExample().createCriteria().example());
        for (AuthUserE u : users) {
            res.add(C.toUser(u));
        }

        return res;
    }


    @Override
    public boolean isUserExist(String name) throws Exception {
        return getUserSimpleInfo(name) != null;
    }

    @Override
    public User newUser(User user) throws Exception {
        return newUserMybatis(user);
    }


    private User newUserMybatis(User user) throws Exception {
        AuthUserE authUserE = C.toAuthUserE(user);
        AssertUtils.assertNotNull(authUserE.getName(), "User name can not be null.User Name is needed.");
        authUserEMapper.insert(authUserE);
        user.setId(authUserE.getId());

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                Long rid = null;
                rid = checkAndGetRoleIdMybatis(role);
                if (rid != null) {
                    AuthUserRoleR authUserRoleR = new AuthUserRoleR();
                    authUserRoleR.setRoleId(rid);
                    authUserRoleR.setUserId(user.getId());
                    authUserRoleRMapper.insert(authUserRoleR);
                }
            }
        }

        if (user.getDataResources() != null) {
            Map<String, DataResource> map = new HashMap<>();
            for (DataResource authResource : user.getDataResources()) {
                authDataValidate.validateDataResource(authResource);
                if (map.containsKey(authResource.getResourceType() + authResource.getData())) {
                    DataResource pre = map.get(authResource.getResourceType() + authResource.getData());
                    for (Operation o : authResource.getOperations()) {
                        if (!pre.getOperations().contains(o)) {
                            pre.addOperation(o);
                        }
                    }
                } else {
                    map.put(authResource.getResourceType() + authResource.getData(), authResource);
                }
            }

            List<AuthUserResourceR> resourceRS = new ArrayList<>();

            for (DataResource d : map.values()) {
                if (d.getOperations() != null && d.getOperations().size() > 0
                        && d.getData() != null && d.getResourceType() != null) {
                    resourceRS.add(C.toUserResourceR(d, user.getId()));
                }
            }
            if (resourceRS.size() > 0) {
                authUserResourceRMapper.batchInsert(resourceRS);
            }
        }
        return getUserMybatis(user.getId());
    }

    @Override
    public User updateUser(User user) throws Exception {
        if (user.getId() == null || user.getUserName() == null) {
            throw new ValidationException("User id is needed.");
        }
        return updateUserMybatis(user);
    }


    private User updateUserMybatis(User user) throws Exception {
        User org = getUserMybatis(user.getUserName());
        if (org == null) {
            throw new ValidationException("Not Found User By User Id.");
        }
        user.setId(org.getId());
        authUserEMapper.updateByPrimaryKey(C.toAuthUserE(user));

        List<String> orgRoleNames = new ArrayList<>();
        List<String> newRoleNames = new ArrayList<>();

        for (Role role : org.getRoles()) {
            orgRoleNames.add(role.getRoleName());
        }
        for (Role role : user.getRoles()) {
            if (role.getId() == null || role.getRoleName() == null) {
                throw new ValidationException("Invalid data.Role id is null.");
            }
            newRoleNames.add(role.getRoleName());
        }

        for (String name : orgRoleNames) {
            if (!newRoleNames.contains(name)) {
                Long tmpId = checkAndGetRoleIdMybatis(new Role().setRoleName(name));
                authUserRoleRMapper.deleteByExample(new AuthUserRoleRExample().createCriteria().andRoleIdEqualTo(tmpId)
                        .andUserIdEqualTo(user.getId()).example());
            }
        }
        for (String name : newRoleNames) {
            if (!orgRoleNames.contains(name)) {
                Long rid = null;
                rid = checkAndGetRoleIdMybatis(new Role().setRoleName(name));
                if (rid != null) {
                    AuthUserRoleR userRoleR = new AuthUserRoleR();
                    userRoleR.setUserId(user.getId());
                    userRoleR.setRoleId(rid);
                    authUserRoleRMapper.insert(userRoleR);
                }
            }
        }

        Map<String, DataResource> orgResource = new HashMap<>();
        Map<String, DataResource> newResource = new HashMap<>();

        for (DataResource a : org.getDataResources()) {
            orgResource.put(a.getResourceType() + a.getData(), a);
        }
        for (DataResource a : user.getDataResources()) {
            if (newResource.containsKey(a.getResourceType() + a.getData())) {
                DataResource pre = newResource.get(a.getResourceType() + a.getData());
                for (Operation o : a.getOperations()) {
                    if (!pre.getOperations().contains(o)) {
                        pre.addOperation(o);
                    }
                }
            } else {
                newResource.put(a.getResourceType() + a.getData(), a);
            }
        }

        for (String k : orgResource.keySet()) {
            if (!newResource.containsKey(k)) {
                authUserResourceRMapper.deleteByExample(new AuthUserResourceRExample().createCriteria().andUserIdEqualTo(user.getId())
                        .andDataEqualTo(orgResource.get(k).getData()).andTypeEqualTo(orgResource.get(k).getResourceType()).example());
            }
        }

        List<AuthUserResourceR> resourceRS = new ArrayList<>();

        for (DataResource d : newResource.values()) {
            authDataValidate.validateDataResource(d);
            resourceRS.add(C.toUserResourceR(d, user.getId()));
        }
        if (resourceRS.size() > 0) {
            authUserResourceRMapper.batchInsert(resourceRS);
        }
        return getUserMybatis(user.getId());
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        authUserEMapper.deleteByPrimaryKey(id);
        authUserResourceRMapper.deleteByExample(new AuthUserResourceRExample().createCriteria().andUserIdEqualTo(id).example());
        authUserRoleRMapper.deleteByExample(new AuthUserRoleRExample().createCriteria().andUserIdEqualTo(id).example());
    }

    @Override
    public void deleteUser(String name) throws Exception {
        User user = getUserSimpleInfo(name);
        if (user == null) return;
        deleteUser(user.getId());
    }

    @Override
    public Map<String, Map<String, DataResource>> getAuthResourcesByUserName(String name) throws Exception {
        Map<String, Map<String, DataResource>> result = new HashMap<>();
        User user = getUser(name);
        if (user.getDataResources() != null && user.getDataResources().size() > 0) {
            for (DataResource a : user.getDataResources()) {
                if (!result.containsKey(a.getResourceType())) {
                    result.put(a.getResourceType(), new HashMap<String, DataResource>());
                }
                result.get(a.getResourceType()).put(a.getData(), a);
            }
        }
        if (user.getRoles() != null && user.getRoles().size() > 0) {
            for (Role role : user.getRoles()) {
                for (DataResource a : role.getDataResources()) {
                    if (!result.containsKey(a.getResourceType())) {
                        result.put(a.getResourceType(), new HashMap<String, DataResource>());
                    }
                    if (result.get(a.getResourceType()).containsKey(a.getData())) {
                        for (Operation ops : a.getOperations()) {
                            if (!result.get(a.getResourceType()).get(a.getData()).getOperations().contains(ops)) {
                                result.get(a.getResourceType()).get(a.getData()).addOperation(ops);
                            }
                        }
                    } else {
                        result.get(a.getResourceType()).put(a.getData(), a);
                    }
                }
            }
        }
        return result;
    }

    private Long checkAndGetRoleIdMybatis(Role role) throws Exception {
        Role r = null;
        if (role.getRoleName() != null) {
            r = roleService.getRoleMybatis(role.getRoleName());
        }
        if (r == null && role.getId() != null) {
            r = roleService.getRoleMybatis(role.getId());
            if (r == null) {
                throw new ValidationException("Not found Role by id and Name, roleId: " + role.getId() + " Name:" + role.getRoleName());
            }
        }
        return r != null ? r.getId() : null;
    }
}
