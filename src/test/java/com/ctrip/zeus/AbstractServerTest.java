package com.ctrip.zeus;

import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import java.io.File;

/**
 * Created by zhoumy on 2015/11/5.
 */
public abstract class AbstractServerTest extends AbstractSpringTest {
    protected static MysqlDbServer mysqlDbServer;

    @BeforeClass
    public static void setUpDb() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        S.setPropertyDefaultValue("archaius.deployment.applicationId", "slb-admin");
        S.setPropertyDefaultValue("archaius.deployment.environment","unittest");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @AfterClass
    public static void tearDownDb() throws InterruptedException, ComponentLookupException, ComponentLifecycleException {
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }
}

