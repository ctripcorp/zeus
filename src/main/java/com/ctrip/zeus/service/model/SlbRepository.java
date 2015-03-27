package com.ctrip.zeus.service.model;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;
import com.ctrip.zeus.service.Repository;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {

    SlbList list() throws Exception;

    Slb get(String slbName) throws Exception;

    void add(Slb slb) throws Exception;

    void update(Slb slb) throws Exception;

    int delete(String slbName) throws Exception;
}
