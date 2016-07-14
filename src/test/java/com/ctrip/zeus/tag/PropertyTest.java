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
    public void testsetAndRemoveItem() throws Exception {
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

        propertyBox.removeProperty("department");
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

        propertyBox.removeProperty("bumen");
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

        propertyBox.removeProperty("department");
    }
}
