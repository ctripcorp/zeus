package com.ctrip.zeus.service.auth;

import com.ctrip.zeus.dao.entity.AuthUserPassword;

import java.util.List;

/**
 * @Discription
 **/
public interface UserLoginService {
    List<AuthUserPassword> list();


    /*
     * @Description: Used for login api to find matched entry
     * @return: found entry or null
     **/
    AuthUserPassword login(String userName, String password);

    /*
     * used for registry api
     * @Param password: encoded and passed from client(server)
     * @return
     **/
    void signUp(String userName, String password) throws Exception;

    /*
     * @Description: add user by user management backend.
     * @return: not-encoded password
     **/
    String add(String userName) throws Exception;


    /*
     * @Description: whether a user exists
     * @return
     **/
    boolean userExists(String userName);

    /*
     * @Description: update user's password
     * @return
     **/
    void update(String userName, String password);

    /*
     * @Description: reset user's password
     * @return: random generated not-encoded password
     **/
    String resetPassword(String userName);

    void deleteUser(String userName);

    Long queryByUserName(String userName);
}
