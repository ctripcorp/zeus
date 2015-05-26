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

    SlbDo add(Slb slb) throws DalException, ValidationException;

    SlbDo update(Slb slb) throws DalException, ValidationException;

    int delete(long slbId) throws DalException, ValidationException;
}
