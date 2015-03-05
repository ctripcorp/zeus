package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.model.entity.SlbCluster;
import com.ctrip.zeus.service.SlbClusterRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
@Repository("slbClusterRepository")
public class SlbClusterRepositoryImpl implements SlbClusterRepository {

    @Override
    public List<SlbCluster> list() {

        return null;
    }
}
