package com.ctrip.zeus.service.auth;

import com.ctrip.zeus.auth.entity.*;

import java.util.List;

/**
 * User: mag
 * Date: 4/16/2015
 * Time: 4:08 PM
 */
public interface AuthorizationService {
    /**
     * get all roles in system
     * @return
     */
    List<Role> getAllRoles() throws Exception;

    /**
     * get all users in system
     * @return
     */
    List<User> getAllUsers() throws Exception;

    List<String> getUsersByRoleGroup(String roleName, String groupName) throws Exception;

    User getUser(String userName) throws Exception;

    void addUser(User user) throws Exception;

    void deleteUser(String userName) throws Exception;

    void updateUser(User user) throws Exception;

    void addRole(Role role) throws Exception;

    /**
     * get all resources in system
     * @return
     */
    List<Resource> getAllResources() throws Exception;

    /**
     *
     * @param resourceName
     * @return Role name list that has authorization to access this resource
     */
    List<String> getRolesForResource(String resourceName) throws Exception;

    /**
     *
     * @param roleName
     * @return all authorized resource for specific role
     */
    List<Resource> getResourcesByRole(String roleName) throws Exception;

    /**
     * add resource in the system
     * @param resource
     */
    void addResource(Resource resource) throws Exception;

    /**
     * delete resource and related resource roles
     * @param resourceName
     */
    void deleteResource(String resourceName) throws Exception;


    void updateResource(Resource resource) throws Exception;

    void updateResourceRoles(String resourceName, List<String> roles) throws Exception;
}
