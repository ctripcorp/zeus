package com.ctrip.zeus.service.auth.auto.filter;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.service.auth.auto.AutoFillFilter;
import com.ctrip.zeus.service.auth.auto.AutoFillService;
import com.ctrip.zeus.service.build.ConfigHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/8/25.
 */
public abstract class AutoFillAbstractFilter implements AutoFillFilter {
    @Autowired
    private AutoFillService autoFillService;
    @Resource
    private ConfigHandler configHandler;

    @Override
    public int order() {
        return 0;
    }

    public abstract void runFilter(User user) throws Exception;

    public abstract boolean shouldFilter(User user);

    @Override
    public void filter(User user) throws Exception {
        if (!configHandler.getEnable("auth.auto.fill.filter." + this.getClass().getSimpleName(), true)) {
            return;
        }
        if (shouldFilter(user)) {
            runFilter(user);
        }
    }

    @PostConstruct
    public void init() {
        autoFillService.addFilter(this);
    }
}
