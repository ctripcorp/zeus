package com.ctrip.zeus.tag;

import com.ctrip.zeus.tag.entity.Property;
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
import java.util.List;

/**
 * Created by zhoumy on 2015/7/21.
 */
public class PropertyTest extends AbstractSpringTest {
    private static MysqlDbServer mysqlDbServer;

    @Resource
    private PropertyBox propertyBox;
    @Resource
    private PropertyService propertyService;

    @BeforeClass
    public static void setUpDb() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @Test
    public void testAddAndRemoveItem() throws Exception {
        propertyBox.add("department", "qiche", "client", 1L);
        propertyBox.add("department", "gonglue", "client", 1L);
        propertyBox.add("department", "qiche", "client", 2L);
        propertyBox.add("department", "jiudian", "client", 5L);
        propertyBox.add("department", "qiche", "client", 3L);
        propertyBox.add("department", "gonglue", "client", 6L);

        List<Long> plist = propertyService.query("department", "client");
        Assert.assertEquals(6, plist.size());
        plist = propertyService.query("department", "qiche", "client");
        Assert.assertEquals(3, plist.size());

        propertyBox.delete("department", "qiche", "client", 3L);
        propertyBox.delete("department", "qiche", "client", 1L);
        plist = propertyService.query("department", "qiche", "client");
        Assert.assertEquals(1, plist.size());

        propertyBox.delete("department", "gonglue", "client", null);
        plist = propertyService.query("department", "gonglue", "client");
        Assert.assertEquals(0, plist.size());

        propertyBox.removeProperty("department");
    }

    @Test
    public void testRenameProperty() throws Exception {
        propertyBox.add("bumen", "car", "client", 1L);
        propertyBox.renameProperty("bumen", "department", "car", "qiche");
        List<Long> plist = propertyService.query("bumen", "qiche", "client");
        Assert.assertEquals(0, plist.size());
        plist = propertyService.query("department", "qiche", "client");
        Assert.assertEquals(1, plist.size());

        propertyBox.add("department", "qiche", "client", 3L);
        propertyBox.add("department", "qiche", "client", 2L);
        propertyBox.renameProperty("department", "bumen");
        plist = propertyService.query("bumen", "qiche", "client");
        Assert.assertEquals(3, plist.size());

        propertyBox.removeProperty("bumen");
    }

    @Test
    public void testGetItemProperties() throws Exception {
        propertyBox.add("department", "qiche", "client", 1L);
        propertyBox.add("department", "gonglue", "client", 1L);
        propertyBox.add("department", "qiche", "client", 2L);
        propertyBox.add("department", "jiudian", "client", 1L);
        propertyBox.add("department", "qiche", "client", 3L);
        propertyBox.add("department", "gonglue", "client", 6L);

        List<Property> l = propertyService.getProperties("client", 1L);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(3, l.get(0).getValues().size());

        propertyBox.removeProperty("department");
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
