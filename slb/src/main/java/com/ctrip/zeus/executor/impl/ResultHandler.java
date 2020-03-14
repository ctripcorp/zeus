package com.ctrip.zeus.executor.impl;

import java.util.Set;

/**
 * Created by zhoumy on 2016/1/6.
 */
public interface ResultHandler<T, W> {

    W[] handle(Set<T> result) throws Exception;
}