package com.ctrip.zeus.build;

import org.unidal.dal.jdbc.AbstractDao;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
public class GenerateDaoBeansConfig {

    public static void main(String[] args) {
        String daoPackage="com.ctrip.zeus.dal.core";

        List<Class<?>> list = ClassFinder.find(daoPackage);
        for (Class<?> clazz : list) {
            if(AbstractDao.class.isAssignableFrom(clazz))
            System.out.println(clazz.getName());
        }
    }
}
