package com.ctrip.zeus.service.auth.auto;

import com.ctrip.zeus.auth.entity.User;

/**
 * Created by fanqq on 2016/8/25.
 */
public interface AutoFillService {

    public void addFilter(AutoFillFilter fillFilter);

    public void autoFill(User name, String employee);

}
