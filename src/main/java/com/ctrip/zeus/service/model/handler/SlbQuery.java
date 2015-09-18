package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Slb;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbQuery {

    Slb getById(Long id) throws Exception;
}