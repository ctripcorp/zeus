package com.ctrip.zeus.service.model;

import com.ctrip.zeus.dal.core.SlbDo;
import com.ctrip.zeus.model.entity.Slb;
import org.unidal.dal.jdbc.DalException;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/7/2015.
 */
public interface SlbSync {
    SlbDo sync(Slb slb) throws DalException;
}
