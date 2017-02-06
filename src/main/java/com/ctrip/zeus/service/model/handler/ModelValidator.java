package com.ctrip.zeus.service.model.handler;

/**
 * Created by zhoumy on 2015/9/24.
 */
public interface ModelValidator<T> {

    boolean exists(Long targetId) throws Exception;

    void validate(T target) throws Exception;

    void checkVersionForUpdate(T target) throws Exception;

    void removable(Long targetId) throws Exception;
}
