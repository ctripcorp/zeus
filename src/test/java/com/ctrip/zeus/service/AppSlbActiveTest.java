package com.ctrip.zeus.service;

import com.ctrip.zeus.ao.AbstractAPITest;
import com.ctrip.zeus.dal.core.ConfGroupSlbActiveDao;
import com.ctrip.zeus.dal.core.ConfGroupSlbActiveDo;
import com.ctrip.zeus.dal.core.ConfGroupSlbActiveEntity;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by fanqq on 2015/4/10.
 */
public class AppSlbActiveTest extends AbstractAPITest {

    @Resource
    ConfGroupSlbActiveDao confGroupSlbActiveDao;


    @Test
    public void test()
    {
        try{
            confGroupSlbActiveDao.insert(new ConfGroupSlbActiveDo().setSlbId(51).setGroupId(1).setSlbVirtualServerId(2));
            confGroupSlbActiveDao.insert(new ConfGroupSlbActiveDo().setSlbId(52).setGroupId(2).setSlbVirtualServerId(3));
            confGroupSlbActiveDao.insert(new ConfGroupSlbActiveDo().setSlbId(53).setGroupId(3).setSlbVirtualServerId(1));
            confGroupSlbActiveDao.insert(new ConfGroupSlbActiveDo().setSlbId(51).setGroupId(4).setSlbVirtualServerId(2));
            confGroupSlbActiveDao.insert(new ConfGroupSlbActiveDo().setSlbId(52).setGroupId(5).setSlbVirtualServerId(3));

            List<ConfGroupSlbActiveDo> res = confGroupSlbActiveDao.findBySlbId(52, ConfGroupSlbActiveEntity.READSET_FULL);

            confGroupSlbActiveDao.deleteByGroupId(new ConfGroupSlbActiveDo().setGroupId(2));

        }catch (Exception e)
        {

        }
    }

}
