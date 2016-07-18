package com.ctrip.zeus.tag;

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
public class TagTest extends AbstractSpringTest {
    private static MysqlDbServer mysqlDbServer;

    @Resource
    private TagBox tagBox;
    @Resource
    private TagService tagService;

    @BeforeClass
    public static void setUpDb() throws ComponentLookupException, ComponentLifecycleException {
        S.setPropertyDefaultValue("CONF_DIR", new File("").getAbsolutePath() + "/conf/test");
        mysqlDbServer = new MysqlDbServer();
        mysqlDbServer.start();
    }

    @Test
    public void testTaggingAndUntagging() throws Exception {
        tagBox.tagging("testTaggingGroup", "group", new Long[]{1L});
        tagBox.tagging("testTaggingGroup", "group", new Long[]{2L});
        tagBox.tagging("testTaggingGroup", "group", new Long[]{3L});
        tagBox.tagging("testTaggingSlb", "slb", new Long[]{1L});
        tagBox.tagging("testTaggingSlb", "slb", new Long[]{2L});
        tagBox.tagging("testTaggingSlb", "slb", new Long[]{3L});
        tagBox.tagging("testTaggingSlb", "slb", new Long[]{4L});
        tagBox.tagging("testTaggingSlb", "slb", new Long[]{5L});

        List<Long> glist = tagService.query("testTaggingGroup", "group");
        List<Long> slist = tagService.query("testTaggingSlb", "slb");
        Assert.assertEquals(3, glist.size());
        Assert.assertEquals(5, slist.size());

        tagBox.untagging("testTaggingGroup", "group", new Long[]{2L});
        tagBox.untagging("testTaggingGroup", "group", new Long[]{3L});
        tagBox.untagging("testTaggingSlb", "slb", new Long[]{1L});
        tagBox.untagging("testTaggingSlb", "slb", new Long[]{2L});
        tagBox.untagging("testTaggingSlb", "slb", new Long[]{3L});

        glist = tagService.query("testTaggingGroup", "group");
        slist = tagService.query("testTaggingSlb", "slb");
        Assert.assertEquals(1, glist.size());
        Assert.assertEquals(1L, glist.get(0).longValue());
        Assert.assertEquals(2, slist.size());

        tagBox.untagging("testTaggingGroup", "group", null);
        tagBox.untagging("testTaggingSlb", "slb", null);

        glist = tagService.query("testTaggingGroup", "group");
        slist = tagService.query("testTaggingSlb", "slb");
        Assert.assertEquals(0, glist.size());
        Assert.assertEquals(0, slist.size());

        tagBox.removeTag("testTaggingGroup", true);
        tagBox.removeTag("testTaggingSlb", true);
    }

    @Test
    public void testRemoveTag() throws Exception {
        tagBox.tagging("testRemoveTag", "group", new Long[]{1L});
        tagBox.tagging("testRemoveTag", "group", new Long[]{2L});
        tagBox.tagging("testRemoveTag", "group", new Long[]{3L});

        try {
            tagBox.removeTag("testRemoveTag", false);
            Assert.assertTrue(false);
        } catch (Exception ex) {
        }

        tagBox.untagging("testRemoveTag", "group", new Long[]{1L});
        try {
            tagBox.removeTag("testRemoveTag", false);
            Assert.assertTrue(false);
        } catch (Exception ex1) {

        }
        tagBox.untagging("testRemoveTag", "group", new Long[]{2L, 3L});
        tagBox.removeTag("testRemoveTag", false);
    }

    @Test
    public void testRenameTag() throws Exception {
        tagBox.tagging("testTaggingGroup", "group", new Long[]{1L});
        tagBox.tagging("testTaggingGroup", "group", new Long[]{2L});
        tagBox.tagging("testTaggingGroup", "group", new Long[]{3L});

        List<Long> l = tagService.query("testTaggingGroup", "group");
        Assert.assertEquals(3, l.size());

        tagBox.renameTag("testTaggingGroup", "ttg");
        l = tagService.query("testTaggingGroup", "group");
        Assert.assertEquals(0, l.size());
        l = tagService.query("ttg", "group");
        Assert.assertEquals(3, l.size());

        tagBox.removeTag("ttg", true);
    }

    @Test
    public void testGetItemTags() throws Exception {
        tagBox.tagging("testTaggingGroup1", "group", new Long[]{1L});
        tagBox.tagging("testTaggingGroup2", "group", new Long[]{1L});
        tagBox.tagging("testTaggingGroup3", "group", new Long[]{2L});

        List<String> l = tagService.getTags("group", 1L);
        Assert.assertEquals(2, l.size());

        tagBox.removeTag("testTaggingGroup1", true);
        tagBox.removeTag("testTaggingGroup2", true);
        tagBox.removeTag("testTaggingGroup3", true);
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
