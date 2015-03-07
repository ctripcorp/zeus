package com.ctrip.zeus.service;

import com.ctrip.zeus.model.entity.Slb;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbRepository extends Repository {
    List<Slb> list();

    Slb get(String slbName);

    void add(Slb sc);
}
