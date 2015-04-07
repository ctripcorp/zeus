package com.ctrip.zeus.restful;

import com.ctrip.zeus.client.AppClient;
import com.ctrip.zeus.client.SlbClient;
import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.restful.response.entity.ErrorMessage;
import com.ctrip.zeus.restful.response.transform.DefaultJsonParser;
import com.ctrip.zeus.server.SlbAdminServer;
import com.ctrip.zeus.util.IOUtils;
import com.ctrip.zeus.util.S;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.datasource.DataSourceManager;
import org.unidal.dal.jdbc.transaction.TransactionManager;
import org.unidal.lookup.ContainerLoader;
import support.AbstractSpringTest;
import support.MysqlDbServer;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.*;

/**
 * Created by zhoumy on 2015/4/2.
 */
public class ExceptionTest extends AbstractSpringTest {
    private static SlbAdminServer server;
    private static MysqlDbServer mysqlDbServer;

    @Resource
    private AppDao appDao;
    @Resource
    private SlbDao slbDao;

    @BeforeClass
    public static void setUpDb() throws Exception {
        S.setPropertyDefaultValue("archaius.deployment.applicationId", "slb-admin");
        S.setPropertyDefaultValue("archaius.deployment.environment", "local");
        S.setPropertyDefaultValue("server.www.base-dir", new File("").getAbsolutePath() + "/src/main/www");
        S.setPropertyDefaultValue("server.temp-dir", new File("").getAbsolutePath() + "/target/temp");
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");

        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();

        server = new SlbAdminServer();
        server.start();
    }

    @Test
    public void testDalNotFoundException() throws DalException {
        Assert.assertNull(appDao.findByName("notExistApp", AppEntity.READSET_FULL));
        Assert.assertNull(slbDao.findByName("notExistSlb", SlbEntity.READSET_FULL));
    }

    @Test
    public void testExceptionInterceptor() throws Exception {
        AppClient ac = new AppClient("http://127.0.0.1:8099");
        Response appResponse = ac.add(new App());
        Assert.assertEquals(500, appResponse.getStatus());

        String appString = IOUtils.inputStreamStringify((InputStream) appResponse.getEntity());
        ErrorMessage aem = DefaultJsonParser.parse(ErrorMessage.class, appString);
        printErrorMessage(aem);
        Assert.assertEquals(ValidationException.class.getSimpleName(), aem.getCode());

        SlbClient sc = new SlbClient("http://127.0.0.1:8099");
        Response slbResponse = sc.add(new Slb());
        Assert.assertEquals(500, appResponse.getStatus());

        String slbString = IOUtils.inputStreamStringify((InputStream) slbResponse.getEntity());
        ErrorMessage sem = DefaultJsonParser.parse(ErrorMessage.class, slbString);
        printErrorMessage(sem);
        Assert.assertEquals(ValidationException.class.getSimpleName(), sem.getCode());
    }

    private static void printErrorMessage(ErrorMessage em) {
        System.out.println("*************** Error message ***************");
        System.out.println("code:\n" + em.getCode());
        System.out.println("message:\n" + em.getMessage());
        System.out.println("stack trace:\n" + em.getCode());
        System.out.println("*********************************************");
    }

    @AfterClass
    public static void tearDownDb() throws InterruptedException, ComponentLookupException, ComponentLifecycleException {
        server.close();
        mysqlDbServer.stop();

        DataSourceManager ds = ContainerLoader.getDefaultContainer().lookup(DataSourceManager.class);
        ContainerLoader.getDefaultContainer().release(ds);
        TransactionManager ts = ContainerLoader.getDefaultContainer().lookup(TransactionManager.class);
        ContainerLoader.getDefaultContainer().release(ts);
    }
}
