package com.ctrip.zeus.transaction;

import com.ctrip.zeus.dal.core.GroupDo;
import com.ctrip.zeus.service.DemoRepository;
import com.ctrip.zeus.util.S;
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
import java.io.File;
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
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
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
        String name = "group" + UUID.randomUUID();
        demoRepository.addGroup(name);
        GroupDo groupDo = demoRepository.getGroup(name);
        Assert.assertEquals(name, groupDo.getName());
        demoRepository.deleteGroup(groupDo);

        name = "group" + UUID.randomUUID();
        try {
            demoRepository.addGroupError(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        groupDo = demoRepository.getGroup(name);
        Assert.assertNull(groupDo);
    }
}
