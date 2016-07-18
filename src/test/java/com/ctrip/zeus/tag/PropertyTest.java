package com.ctrip.zeus.tag;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.tag.entity.Property;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/21.
 */
public class PropertyTest extends AbstractServerTest {

    @Resource
    private PropertyBox propertyBox;
    @Resource
    private PropertyService propertyService;

    @Test
    public void testSetAndRemoveItem() throws Exception {
        propertyBox.set("department", "qiche", "client", 1L);
        propertyBox.set("department", "qiche", "client", 2L);
        propertyBox.set("department", "jiudian", "client", 5L);
        propertyBox.set("department", "qiche", "client", 3L);
        propertyBox.set("department", "gonglue", "client", 6L);
        Assert.assertEquals(3, propertyService.queryTargets("department", "qiche", "client").size());

        propertyBox.set("department", "gonglue", "client", 1L);
        Assert.assertEquals(2, propertyService.queryTargets("department", "qiche", "client").size());

        Assert.assertTrue(propertyBox.clear("department", "qiche", "client", 3L));
        Assert.assertFalse(propertyBox.clear("department", "qiche", "client", 1L));
        Assert.assertEquals(1, propertyService.queryTargets("department", "qiche", "client").size());

        propertyBox.set("dc", "jq", "client", 2L);
        Assert.assertEquals(2, propertyService.getProperties("client", 2L).size());
        propertyBox.clear("client", 2L);
        Assert.assertEquals(0, propertyService.getProperties("client", 2L).size());

        propertyBox.removeProperty("department", true);
        propertyBox.removeProperty("dc", true);
    }

    @Test
    public void testBatchSetAndRemoveItem() throws Exception {
        propertyBox.set("department", "qiche", "client", 1L);
        propertyBox.set("department", "qiche", "client", 2L);
        propertyBox.set("department", "jiudian", "client", 3L);
        propertyBox.set("department", "gonglue", "client", 5L);
        propertyBox.set("dc", "jq", "client", 2L);
        propertyBox.set("dc", "jq", "client", 4L);

        Assert.assertEquals(2, propertyService.queryTargets("department", "qiche", "client").size());
        Assert.assertEquals(2, propertyService.queryTargets("dc", "jq", "client").size());

        propertyBox.set("department", "jiudian", "client", new Long[]{1L, 3L});
        propertyBox.set("department", "qiche", "client", new Long[]{2L, 5L});
        propertyBox.set("dc", "oy", "client", new Long[]{2L, 6L, 7L});
        propertyBox.set("dc", "jq", "client", 4L);

        Assert.assertArrayEquals(new Long[]{2L, 5L}, propertyService.queryTargets("department", "qiche", "client").toArray(new Long[0]));
        Assert.assertArrayEquals(new Long[]{1L, 3L}, propertyService.queryTargets("department", "jiudian", "client").toArray(new Long[0]));
        Assert.assertEquals(0, propertyService.queryTargets("department", "gonglue", "client").size());
        Assert.assertEquals(1, propertyService.queryTargets("dc", "jq", "client").size());
        Assert.assertEquals(3, propertyService.queryTargets("dc", "oy", "client").size());

        propertyBox.clear("department", "qiche", "client", new Long[]{1L, 2L, 5L});
        propertyBox.clear("dc", "oy", "client", new Long[]{2L, 6L});
        Assert.assertArrayEquals(new Long[]{1L, 3L}, propertyService.queryTargets("department", "jiudian", "client").toArray(new Long[0]));
        Assert.assertEquals(0, propertyService.queryTargets("department", "qiche", "client").size());
        Assert.assertEquals(1, propertyService.queryTargets("dc", "oy", "client").size());
        propertyBox.removeProperty("department", true);
        propertyBox.removeProperty("dc", true);
    }

    @Test
    public void testRemoveProperty() throws Exception {
        propertyBox.set("department", "qiche", "client", 1L);
        propertyBox.set("department", "qiche", "client", 2L);
        propertyBox.set("department", "jiudian", "client", 3L);

        try {
            propertyBox.removeProperty("department", false);
            Assert.assertTrue(false);
        } catch (Exception ex) {
        }

        propertyBox.clear("department", "jiudian", "client", 3L);
        try {
            propertyBox.removeProperty("department", false);
            Assert.assertTrue(false);
        } catch (Exception ex) {
        }

        propertyBox.clear("department", "qiche", "client", new Long[]{1L, 2L});
        propertyBox.removeProperty("department", false);
    }

    @Test
    public void testRenameProperty() throws Exception {
        propertyBox.set("bumen", "car", "client", 1L);
        List<Long> plist = propertyService.queryTargets("bumen", "qiche", "client");
        Assert.assertEquals(0, plist.size());

        propertyBox.renameProperty("bumen", "department");
        plist = propertyService.queryTargets("department", "car", "client");
        Assert.assertEquals(1, plist.size());

        propertyBox.set("department", "car", "client", 3L);
        propertyBox.set("department", "car", "client", 2L);
        propertyBox.renameProperty("department", "bumen");
        plist = propertyService.queryTargets("bumen", "car", "client");
        Assert.assertEquals(3, plist.size());

        propertyBox.removeProperty("bumen", true);
    }

    @Test
    public void testGetItemProperties() throws Exception {
        propertyBox.set("department", "qiche", "client", 1L);
        propertyBox.set("department", "gonglue", "client", 1L);
        propertyBox.set("department", "qiche", "client", 2L);
        propertyBox.set("department", "jiudian", "client", 1L);
        propertyBox.set("department", "qiche", "client", 3L);
        propertyBox.set("department", "gonglue", "client", 6L);

        List<Property> l = propertyService.getProperties("client", 1L);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("jiudian", l.get(0).getValue());

        propertyBox.removeProperty("department", true);
    }
}
