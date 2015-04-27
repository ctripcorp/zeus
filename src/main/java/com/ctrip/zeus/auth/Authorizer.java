package com.ctrip.zeus.auth;

import com.ctrip.zeus.auth.impl.AuthorizeException;

/**
 * User: mag
 * Date: 4/22/2015
 * Time: 1:41 PM
 */
public interface Authorizer {
    /**
     * Check whether the user has the authorization to access the resource
     * in the specified group or not. If not throw
     * @param userName
     * @param resourceName
     * @param resGroup
     * @throws com.ctrip.zeus.auth.impl.AuthorizeException
     */
    void authorize(String userName, String resourceName, String resGroup) throws AuthorizeException;
}
