package com.ctrip.zeus.transaction;

import com.ctrip.zeus.dal.core.AppDo;
import com.ctrip.zeus.service.DemoRepository;
import org.junit.Assert;
import org.junit.Test;
import support.AbstractSpringTest;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
public class TransactionTest extends AbstractSpringTest {

    @Resource
    private DemoRepository demoRepository;

    @Test
    public void  test(){
        String name = "app" + UUID.randomUUID();
        demoRepository.addApp(name);
        AppDo appDo = demoRepository.getApp(name);
        Assert.assertEquals(name, appDo.getName());
        demoRepository.deleteApp(appDo);

        name = "app" + UUID.randomUUID();
        try {
            demoRepository.addAppError(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        appDo = demoRepository.getApp(name);
        Assert.assertNull(appDo.getName());


    }
}
