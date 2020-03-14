package com.ctrip.zeus.service.auth;

import com.ctrip.zeus.auth.entity.DataResource;

/**
 * Created by fanqq on 2016/8/5.
 */
public interface AuthDataValidate {
    public void validateDataResource(DataResource dataResource) throws Exception;
}
