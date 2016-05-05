package com.ctrip.zeus.service.clean;

import com.ctrip.zeus.clean.CleanDbManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/5/5.
 */
public class AbstractCleanFilter implements CleanFilter {
    @Resource
    private CleanDbManager cleanDbManager;

    @PostConstruct
    private void init() {
        cleanDbManager.add(this);
    }

    @Override
    public void runFilter() throws Exception {

    }
}
