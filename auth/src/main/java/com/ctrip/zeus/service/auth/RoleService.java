package com.ctrip.zeus.service.auth;

import com.ctrip.zeus.auth.entity.Role;

import java.util.List;

/**
 * Created by fanqq on 2016/7/21.
 */
public interface RoleService {
    Role getRole(Long id) throws Exception;

    Role getRoleMybatis(Long id) throws Exception;

    Role getRole(String name) throws Exception;

    Role getRoleMybatis(String name) throws Exception;

    List<Role> getRoles() throws Exception;

    List<Role> getRolesByUserId(Long id) throws Exception;

    List<Role> getRolesByUserIdMybatis(Long id) throws Exception;

    Role newRole(Role role) throws Exception;

    Role updateRole(Role role) throws Exception;

    void deleteRole(Long id) throws Exception;

    void deleteRole(String name) throws Exception;
}
