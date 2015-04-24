package com.ctrip.zeus.lock;

import com.ctrip.zeus.dal.core.DistLockDao;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by zhoumy on 2015/4/23.
 */
public class DbLockFactory implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DbLockFactory.applicationContext = applicationContext;
    }

    public static DistLockDao getDao() {
        return (DistLockDao) applicationContext.getBean("distLockDao");
    }
}
