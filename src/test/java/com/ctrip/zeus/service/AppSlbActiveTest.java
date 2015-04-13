package com.ctrip.zeus.service;

import com.ctrip.zeus.ao.AbstractAPITest;
import com.ctrip.zeus.dal.core.ConfAppSlbActiveDao;
import com.ctrip.zeus.dal.core.ConfAppSlbActiveDo;
import com.ctrip.zeus.dal.core.ConfAppSlbActiveEntity;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by fanqq on 2015/4/10.
 */
public class AppSlbActiveTest extends AbstractAPITest {

    @Resource
    ConfAppSlbActiveDao confAppSlbActiveDao;


    @Test
    public void test()
    {
        try{
            confAppSlbActiveDao.insert(new ConfAppSlbActiveDo().setSlbName("slb").setAppName("app1").setSlbVirtualServerName("vs1"));
            confAppSlbActiveDao.insert(new ConfAppSlbActiveDo().setSlbName("slb").setAppName("app2").setSlbVirtualServerName("vs2"));
            confAppSlbActiveDao.insert(new ConfAppSlbActiveDo().setSlbName("slb").setAppName("app1").setSlbVirtualServerName("vs1"));
            confAppSlbActiveDao.insert(new ConfAppSlbActiveDo().setSlbName("slb2").setAppName("app2").setSlbVirtualServerName("vs1"));
            confAppSlbActiveDao.insert(new ConfAppSlbActiveDo().setSlbName("slb2").setAppName("app4").setSlbVirtualServerName("vs3"));

            List<ConfAppSlbActiveDo> res = confAppSlbActiveDao.findBySlbName("slb", ConfAppSlbActiveEntity.READSET_FULL);

            confAppSlbActiveDao.deleteByAppName(new ConfAppSlbActiveDo().setAppName("app2"));

        }catch (Exception e)
        {

        }
    }

}
