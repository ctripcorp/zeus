package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.entity.Property;
import org.springframework.stereotype.Component;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/7/16.
 */
@Component("propertyBox")
public class DefaultPropertyBox implements PropertyBox {
    @Resource
    private PropertyDao propertyDao;
    @Resource
    private PropertyItemDao propertyItemDao;
    @Resource
    private PropertyKeyDao propertyKeyDao;

    @Override
    public List<Property> getAllProperties() throws Exception {
        List<Property> result = new ArrayList<>();
        for (PropertyKeyDo propertyKeyDo : propertyKeyDao.findAll(PropertyKeyEntity.READSET_FULL)) {
            Property p = new Property();
            for (PropertyDo propertyDo : propertyDao.findAllByKey(propertyKeyDo.getId(), PropertyEntity.READSET_FULL)) {
                p.addValue(propertyDo.getPropertyValue());
            }
            result.add(p);
        }
        return result;
    }

    @Override
    public void addProperty(String pname, String pvalue) throws Exception {
        PropertyKeyDo d = new PropertyKeyDo().setName(pname);
        propertyKeyDao.insert(d);
        propertyDao.insert(new PropertyDo().setPropertyKeyId(d.getId()).setPropertyValue(pvalue));
    }

    @Override
    public void removeProperty(String pname) throws Exception {
        PropertyKeyDo d = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        List<PropertyDo> list = propertyDao.findAllByKey(d.getId(), PropertyEntity.READSET_FULL);
        for (PropertyDo propertyDo : list) {
            propertyItemDao.deleteByProperty(new PropertyItemDo().setPropertyId(propertyDo.getId()));
            propertyDao.delete(new PropertyDo().setId(propertyDo.getId()));
        }
        propertyKeyDao.delete(new PropertyKeyDo().setId(d.getId()));
    }

    @Override
    public void renameProperty(String pname, String oldValue, String newValue) throws Exception {
        PropertyDo old = findProperty(pname, oldValue);
        propertyDao.update(old.setPropertyValue(newValue), PropertyEntity.UPDATESET_FULL);
    }

    @Override
    public void addItem(String pname, String pvalue, String type, Long itemId) throws Exception {
        PropertyDo d = findProperty(pname, pvalue);
        propertyItemDao.insert(new PropertyItemDo().setPropertyId(d.getId()).setType(type).setItemId(itemId));
    }

    @Override
    public void deleteItem(String pname, String pvalue, String type, Long itemId) throws Exception {
        PropertyDo d = findProperty(pname, pvalue);
        propertyItemDao.deleteByPropertyAndItem(new PropertyItemDo().setPropertyId(d.getId()).setType(type).setItemId(itemId));
    }

    private PropertyDo findProperty(String pname, String pvalue) throws DalException {
        PropertyKeyDo kd= propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null)
            return null;
        return propertyDao.findByKeyAndValue(kd.getId(), pvalue, PropertyEntity.READSET_FULL);
    }
}
