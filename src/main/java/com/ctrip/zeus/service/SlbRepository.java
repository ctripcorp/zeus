package com.ctrip.zeus.service;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {

    SlbList list();

    Slb get(String slbName);

    void addOrUpdate(Slb sc);
}
