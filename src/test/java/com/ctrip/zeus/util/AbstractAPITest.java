package com.ctrip.zeus.util;

import com.ctrip.zeus.server.SlbAdminServer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import java.io.File;

/**
 * Created by fanqq on 2015/3/30.
 */
public abstract class AbstractAPITest extends AbstractSpringTest {

    static SlbAdminServer server;
    static MysqlDbServer mysqlDbServer;

    @BeforeClass
    public static void setup() throws Exception {
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();

        S.setPropertyDefaultValue("archaius.deployment.applicationId", "slb-admin");
        S.setPropertyDefaultValue("archaius.deployment.environment", "local");
        S.setPropertyDefaultValue("server.www.base-dir", new File("").getAbsolutePath() + "/src/main/www");
        S.setPropertyDefaultValue("server.temp-dir", new File("").getAbsolutePath() + "/target/temp");
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");

        server = new SlbAdminServer();
        server.start();

        AopSpring.setup();
    }

    @AfterClass
    public static void teardown() throws InterruptedException, ComponentLifecycleException, ComponentLookupException {
        server.close();
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);

        AopSpring.teardown();

    }


}


