package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface GroupQuery {

    Group getById(Long id) throws Exception;
}