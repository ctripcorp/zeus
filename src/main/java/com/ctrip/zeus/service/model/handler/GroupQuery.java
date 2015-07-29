package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface GroupQuery {

    Group get(String name) throws Exception;

    Group getById(Long id) throws Exception;

    Group getByAppId(String appId) throws Exception;

    List<Group> batchGet(Long[] ids) throws Exception;

    List<Group> getAll() throws Exception;

    List<Group> getBySlb(Long slbId) throws Exception;
}