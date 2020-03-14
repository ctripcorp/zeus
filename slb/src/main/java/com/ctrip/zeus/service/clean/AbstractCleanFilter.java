package com.ctrip.zeus.service.clean;

import com.ctrip.zeus.clean.CleanDbManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by fanqq on 2016/5/5.
 */
@Service
public class AbstractCleanFilter implements CleanFilter {
    @Resource
    private CleanDbManager cleanDbManager;

    @PostConstruct
    public void init() {
        cleanDbManager.add(this);
    }

    @Override
    public void runFilter() throws Exception {

    }
}
