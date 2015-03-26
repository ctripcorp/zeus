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

    SlbList list();

    Slb get(String slbName);

    void add(Slb slb);

    void update(Slb slb);

    int delete(String slbName);
}
