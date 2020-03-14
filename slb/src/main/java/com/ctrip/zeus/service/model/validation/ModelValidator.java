package com.ctrip.zeus.service.model.validation;

/**
 * Created by zhoumy on 2015/9/24.
 */
public interface ModelValidator<T> {

    void checkRestrictionForUpdate(T target) throws Exception;

    void removable(Long targetId) throws Exception;
}
