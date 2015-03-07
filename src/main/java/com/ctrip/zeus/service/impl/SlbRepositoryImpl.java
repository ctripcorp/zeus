package com.ctrip.zeus.service.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.DbSync;
import com.ctrip.zeus.service.SlbRepository;
import com.ctrip.zeus.support.DefaultDoBuilder;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
@Repository("slbClusterRepository")
public class SlbRepositoryImpl implements SlbRepository {
    @Resource
    private SlbDao slbClusterDao;
    @Resource
    private SlbVipDao slbVipDao;
    @Resource
    private SlbServerDao slbServerDao;
    @Resource
    private SlbDomainDao slbDomainDao;
    @Resource
    private SlbVirtualServerDao slbVirtualServerDao;

    @Resource
    private DbSync dbSync;

    @Override
    public List<Slb> list() {
        return null;
    }

    @Override
    public void add(Slb s) {
        try {
            dbSync.sync(s);
        } catch (DalException e) {
            e.printStackTrace();
        }
    }

    public void add2(Slb sc) {
        Map<Class<?>, List> map = new DefaultDoBuilder().build(sc);
        SlbDo scd = (SlbDo) map.get(SlbDo.class).get(0);
        scd.setStatus("A");
        List<SlbVipDo> svdList = map.get(SlbVipDo.class);
        List<SlbServerDo> ssdList = map.get(SlbServerDo.class);
        List<SlbDomainDo> sddList = map.get(SlbDomainDo.class);
        List<SlbVirtualServerDo> svsdList = map.get(SlbVirtualServerDo.class);

        try {
            slbClusterDao.insert(scd);
            if (svdList != null) {
                for (SlbVipDo d : svdList) {
                    d.setSlbId(scd.getId());
                    slbVipDao.insert(d);
                }
            }
            if (ssdList != null) {
                for (SlbServerDo d : ssdList) {
                    d.setSlbId(scd.getId());
                    slbServerDao.insert(d);
                }
            }
        } catch (DalException e) {
            e.printStackTrace();
        }
    }
}
