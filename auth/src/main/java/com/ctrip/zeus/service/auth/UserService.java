package com.ctrip.zeus.service.auth;

import com.ctrip.zeus.auth.entity.DataResource;
import com.ctrip.zeus.auth.entity.User;

import java.util.List;
import java.util.Map;

/**
 * Created by fanqq on 2016/7/21.
 */
public interface UserService {
    /**
     * get all user info
     *
     * @return
     */
    List<User> getUsers() throws Exception;

    /**
     * get user info
     *
     * @param id
     * @return
     */
    User getUser(Long id) throws Exception;

    /**
     * get user info
     *
     * @param name
     * @return
     */
    User getUser(String name) throws Exception;

    /**
     * get user info Simple Info
     *
     * @param name
     * @return
     */
    User getUserSimpleInfo(String name) throws Exception;

    /**
     * get user info Simple Info
     *
     * @return
     */
    List<User> getUsersSimpleInfo() throws Exception;

    /**
     * is user exist
     *
     * @param name
     * @return
     */
    boolean isUserExist(String name) throws Exception;

    /**
     * new user
     *
     * @param user
     */
    User newUser(User user) throws Exception;

    /**
     * update user
     *
     * @param user
     * @return
     */
    User updateUser(User user) throws Exception;

    /**
     * delete user
     *
     * @param id
     */
    void deleteUser(Long id) throws Exception;

    /**
     * delete user
     *
     * @param name
     */
    void deleteUser(String name) throws Exception;

    /**
     * get merged auth resource by user name
     *
     * @param name
     * @return Map<type ,   Map < id , DataResource>>
     * @throws Exception
     */
    Map<String, Map<String, DataResource>> getAuthResourcesByUserName(String name) throws Exception;

}
