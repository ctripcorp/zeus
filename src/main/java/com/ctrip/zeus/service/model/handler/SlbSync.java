package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.dal.core.SlbDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Slb;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbSync {

    void add(Slb slb) throws Exception;

    void update(Slb slb) throws Exception;

    int delete(Long slbId) throws Exception;
}
