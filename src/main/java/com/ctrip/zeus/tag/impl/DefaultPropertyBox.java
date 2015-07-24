package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.tag.entity.Property;
import org.springframework.stereotype.Component;

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
    public void renameProperty(String oldName, String newName) throws Exception {
        // if key does not exist, simply return
        PropertyKeyDo kd = propertyKeyDao.findByName(oldName, PropertyKeyEntity.READSET_FULL);
        if (kd == null)
            return;
        // otherwise update key first
        propertyKeyDao.update(kd.setName(newName), PropertyKeyEntity.UPDATESET_FULL);
    }

    @Override
    public void renameProperty(String oldName, String newName, String oldValue, String newValue) throws Exception {
        // if key does not exist, simply return
        PropertyKeyDo kd = propertyKeyDao.findByName(oldName, PropertyKeyEntity.READSET_FULL);
        if (kd == null)
            return;
        // otherwise check and update key first
        if (!oldName.equalsIgnoreCase(newName)) {
            List<PropertyDo> list = propertyDao.findAllByKey(kd.getId(), PropertyEntity.READSET_FULL);
            if (list.size() > 1)
                throw new ValidationException("More than one property value is attached to " + oldName + ". Cannot update.");
            else
                propertyKeyDao.update(kd.setName(newName), PropertyKeyEntity.UPDATESET_FULL);
        }
        PropertyDo d = propertyDao.findByKeyAndValue(kd.getId(), oldValue, PropertyEntity.READSET_FULL);
        if (d == null)
            return;
        else
            propertyDao.update(d.setPropertyValue(newValue), PropertyEntity.UPDATESET_FULL);
    }

    @Override
    public void add(String pname, String pvalue, String type, Long[] itemIds) throws Exception {
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        PropertyDo d = null;
        if (kd == null) {
            kd = new PropertyKeyDo().setName(pname);
            propertyKeyDao.insert(kd);
        } else {
            d = propertyDao.findByKeyAndValue(kd.getId(), pvalue, PropertyEntity.READSET_FULL);
        }
        if (d == null) {
            d = new PropertyDo().setPropertyKeyId(kd.getId()).setPropertyValue(pvalue);
            propertyDao.insert(d);
        }
        PropertyItemDo[] l = new PropertyItemDo[itemIds.length];
        for (int i = 0; i < itemIds.length; i++) {
            l[i] = new PropertyItemDo().setPropertyId(d.getId()).setType(type).setItemId(itemIds[i]);
        }
        propertyItemDao.insert(l);
    }

    @Override
    public void delete(String pname, String pvalue, String type, Long[] itemIds) throws Exception {
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null)
            return;
        PropertyDo d = propertyDao.findByKeyAndValue(kd.getId(), pvalue, PropertyEntity.READSET_FULL);
        if (d == null)
            return;
        if (itemIds != null) {
            PropertyItemDo[] l = new PropertyItemDo[itemIds.length];
            for (int i = 0; i < itemIds.length; i++) {
                l[i] = new PropertyItemDo().setPropertyId(d.getId()).setType(type).setItemId(itemIds[i]);
            }
            propertyItemDao.deleteByPropertyAndItems(l);
        }
        else
            propertyItemDao.deleteByPropertyAndType(new PropertyItemDo().setPropertyId(d.getId()).setType(type));
    }
}
