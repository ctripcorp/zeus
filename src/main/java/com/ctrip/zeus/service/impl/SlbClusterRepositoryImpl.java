package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dal.core.Demo;
import com.ctrip.zeus.dal.core.DemoDao;
import com.ctrip.zeus.dal.core.DemoEntity;
import com.ctrip.zeus.model.entity.SlbCluster;
import com.ctrip.zeus.service.SlbClusterRepository;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
@Repository("slbClusterRepository")
public class SlbClusterRepositoryImpl implements SlbClusterRepository {
    @Resource
    private DemoDao demoDao;

    @Override
    public List<SlbCluster> list() {

        try {
            Demo demo = demoDao.findByPK(1, DemoEntity.READSET_FULL);

            System.out.println(demo.getName());

        } catch (DalException e) {
            e.printStackTrace();
        }

        return null;
    }
}
