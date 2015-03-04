package com.ctrip.zeus.service;

import com.ctrip.zeus.model.entity.SlbCluster;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
public interface SlbClusterRepository extends Repository {
    List<SlbCluster> list();
}
