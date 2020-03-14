package com.ctrip.zeus.service.auth.impl;

import com.ctrip.zeus.C;
import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.auth.entity.Operation;
import com.ctrip.zeus.auth.entity.Role;
import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.AuthRoleEMapper;
import com.ctrip.zeus.dao.mapper.AuthRoleResourceRMapper;
import com.ctrip.zeus.dao.mapper.AuthUserRoleRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.auth.AuthDataValidate;
import com.ctrip.zeus.service.auth.RoleService;
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
@Service("roleService")
public class RoleServicceImpl implements RoleService {
    @Resource
    private AuthDataValidate authDataValidate;

    @Resource
    private AuthRoleEMapper authRoleEMapper;
    @Resource
    private AuthRoleResourceRMapper authRoleResourceRMapper;
    @Resource
    private AuthUserRoleRMapper authUserRoleRMapper;


    @Override
    public Role getRole(Long id) throws Exception {
        return getRoleMybatis(id);
    }

    @Override
    public Role getRoleMybatis(Long id) {
        Role result = null;
        AuthRoleE roleE = authRoleEMapper.selectByPrimaryKey(id);
        if (roleE == null) {
            return null;
        }
        result = C.toRole(roleE);
        List<AuthRoleResourceR> resources = authRoleResourceRMapper.selectByExample(new AuthRoleResourceRExample().createCriteria().
                andRoleIdEqualTo(id).example());
        if (resources != null) {
            for (AuthRoleResourceR r : resources) {
                result.addDataResource(C.toAuthResource(r));
            }
        }
        return result;
    }


    @Override
    public Role getRole(String name) throws Exception {
        return getRoleMybatis(name);
    }

    @Override
    public Role getRoleMybatis(String name) throws Exception {
        Role result = null;
        AuthRoleE roleE = authRoleEMapper.selectOneByExample(new AuthRoleEExample().createCriteria().andNameEqualTo(name).example());
        if (roleE == null) {
            return null;
        }
        result = C.toRole(roleE);
        List<AuthRoleResourceR> resources = authRoleResourceRMapper.selectByExample(new AuthRoleResourceRExample().createCriteria().
                andRoleIdEqualTo(roleE.getId()).example());
        if (resources != null) {
            for (AuthRoleResourceR r : resources) {
                result.addDataResource(C.toAuthResource(r));
            }
        }
        return result;
    }


    @Override
    public List<Role> getRoles() throws Exception {
        return getRolesMybatis();
    }


    private List<Role> getRolesMybatis() throws Exception {
        List<Role> result = new ArrayList<>();
        List<AuthRoleE> roleE = authRoleEMapper.selectByExample(new AuthRoleEExample().createCriteria().example());
        if (roleE != null) {
            for (AuthRoleE r : roleE) {
                result.add(getRoleMybatis(r.getId()));
            }
        }
        return result;
    }

    @Override
    public List<Role> getRolesByUserId(Long id) throws Exception {
        return getRolesByUserIdMybatis(id);
    }

    @Override
    public List<Role> getRolesByUserIdMybatis(Long id) throws Exception {
        List<Role> result = new ArrayList<>();
        List<AuthUserRoleR> roleResources = authUserRoleRMapper.selectByExample(new AuthUserRoleRExample().createCriteria().andUserIdEqualTo(id).example());
        if (roleResources != null) {
            for (AuthUserRoleR r : roleResources) {
                result.add(getRoleMybatis(r.getRoleId()));
            }
        }
        return result;
    }

    @Override
    public Role newRole(Role role) throws Exception {
        if (role == null) {
            throw new ValidationException("Invalidate Data. Role data is null.");
        }
        if (role.getDataResources() == null || role.getDataResources().size() == 0) {
            throw new ValidationException("Invalidate Data. Role data must have more than one AuthResources.");
        }
        role.setId(null);
        return newRoleMybatis(role);
    }

    private Role newRoleMybatis(Role role) throws Exception {
        AuthRoleE roleE = new AuthRoleE();
        roleE.setDiscription(role.getDiscription());
        roleE.setName(role.getRoleName());

        authRoleEMapper.insert(roleE);

        List<AuthRoleResourceR> data = new ArrayList<>();
        Map<String, DataResource> map = new HashMap<>();
        for (DataResource authResource : role.getDataResources()) {
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

        for (DataResource authResource : map.values()) {
            authDataValidate.validateDataResource(authResource);
            if (authResource.getData() != null && authResource.getResourceType() != null
                    && authResource.getOperations() != null && authResource.getOperations().size() > 0) {
                data.add(C.toAuthRoleResourceR(authResource, roleE.getId()));
            }
        }
        if (data.size() > 0) {
            authRoleResourceRMapper.batchInsert(data);
        }
        return getRoleMybatis(roleE.getId());
    }

    @Override
    public Role updateRole(Role role) throws Exception {
        if (role == null) {
            throw new ValidationException("Invalidate Data. Role data is null.");
        }
        if (role.getId() == null) {
            throw new ValidationException("Invalidate Data. Role id is null.");
        }
        if (role.getDataResources() == null || role.getDataResources().size() == 0) {
            throw new ValidationException("Invalidate Data. Role data must have more than one AuthResources.");
        }
        return updateRoleMybatis(role);
    }

    private Role updateRoleMybatis(Role role) throws Exception {
        AuthRoleE roleE = new AuthRoleE();
        roleE.setDiscription(role.getDiscription());
        roleE.setName(role.getRoleName());
        roleE.setId(role.getId());

        authRoleEMapper.updateByPrimaryKey(roleE);

        Role org = getRoleMybatis(role.getId());

        Map<String, DataResource> orgResource = new HashMap<>();
        Map<String, DataResource> newResource = new HashMap<>();

        for (DataResource a : org.getDataResources()) {
            orgResource.put(a.getResourceType() + a.getData(), a);
        }
        for (DataResource a : role.getDataResources()) {
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
                authRoleResourceRMapper.deleteByExample(new AuthRoleResourceRExample().createCriteria().andRoleIdEqualTo(role.getId())
                        .andDataEqualTo(orgResource.get(k).getData()).andTypeEqualTo(orgResource.get(k).getResourceType()).example());
            }
        }
        List<AuthRoleResourceR> data = new ArrayList<>();
        for (DataResource d : newResource.values()) {
            authDataValidate.validateDataResource(d);
            data.add(C.toAuthRoleResourceR(d, role.getId()));
        }
        if (data.size() > 0) {
            authRoleResourceRMapper.batchInsert(data);
        }
        return getRoleMybatis(role.getId());
    }

    @Override
    public void deleteRole(Long id) throws Exception {
        deleteRoleMybatis(id);
    }

    private void deleteRoleMybatis(Long id) {
        authRoleEMapper.deleteByPrimaryKey(id);
        authRoleResourceRMapper.deleteByExample(new AuthRoleResourceRExample().createCriteria().andRoleIdEqualTo(id).example());
        authUserRoleRMapper.deleteByExample(new AuthUserRoleRExample().createCriteria().andRoleIdEqualTo(id).example());
    }

    @Override
    public void deleteRole(String name) throws Exception {
        AssertUtils.assertNotNull(name, "Name Cannot Be Null.");
        AuthRoleE r = authRoleEMapper.selectOneByExample(new AuthRoleEExample().createCriteria().andNameEqualTo(name).example());
        AssertUtils.assertNotNull(r, "Not Found Role By Name:" + name);
        deleteRoleMybatis(r.getId());
    }


}
