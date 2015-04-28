package com.ctrip.zeus.lock;

import com.ctrip.zeus.dal.core.DistLockDao;
import com.ctrip.zeus.lock.impl.MysqlDistLock;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/4/23.
 */
@Component("dbLockFactory")
public class DbLockFactory {
    @Resource
    private DistLockDao distLockDao;

    public DistLock newLock(String name) {
        return new MysqlDistLock(name, distLockDao);
    }
}
