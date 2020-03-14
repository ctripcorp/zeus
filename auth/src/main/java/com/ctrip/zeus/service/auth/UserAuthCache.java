package com.ctrip.zeus.service.auth;

import com.ctrip.zeus.auth.entity.DataResource;

import java.util.Map;

/**
 * Created by fanqq on 2016/7/22.
 */
public interface UserAuthCache {
    /**
     * get authResource by user name
     *
     * @param name
     * @return Map<String, AuthResource>
     * @throws Exception
     */
    Map<String, Map<String,DataResource>> getAuthResource(String name) throws Exception;

    /**
     * get authResource by user name
     *
     * @param name
     * @return AuthResource
     * @throws Exception
     */
    DataResource getAuthResource(String name, String type, Long id) throws Exception;
}
