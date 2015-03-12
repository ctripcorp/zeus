package com.ctrip.zeus.transaction;

import com.ctrip.zeus.dal.core.AppDo;
import com.ctrip.zeus.service.DemoRepository;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author:xingchaowang
 * @date: 3/10/2015.
 */
public class TransactionTest extends AbstractSpringTest {

    @Resource
    private DemoRepository demoRepository;


    static MysqlDbServer mysqlDbServer;
    @BeforeClass
    public static void setup() throws ComponentLookupException, ComponentLifecycleException {

        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }
    @AfterClass
    public static void teardown() throws InterruptedException, ComponentLookupException, ComponentLifecycleException {
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }

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
