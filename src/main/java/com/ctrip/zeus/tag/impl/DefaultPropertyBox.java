package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.PropertyDao;
import com.ctrip.zeus.dal.core.PropertyItemDao;
import com.ctrip.zeus.dal.core.PropertyKeyDao;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhoumy on 2015/7/16.
 */
@Component("propertyBox")
public class DefaultPropertyBox {
    @Resource
    private PropertyDao propertyDao;
    @Resource
    private PropertyItemDao propertyItemDao;
    @Resource
    private PropertyKeyDao propertyKeyDao;
}
