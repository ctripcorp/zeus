package com.ctrip.zeus.service.auth.auto;

import com.ctrip.zeus.auth.entity.User;

/**
 * Created by fanqq on 2016/8/25.
 */
public interface AutoFillFilter {

    /**
     * the smaller order run first.
     *
     * @return the order
     */
    public int order();

    /**
     * run filter
     *
     * @param user
     */
    public void filter(User user) throws Exception;

}
