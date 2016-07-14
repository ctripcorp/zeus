package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.tag.PropertyBox;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2015/7/16.
 */
@Component("propertyBox")
public class DefaultPropertyBox implements PropertyBox {
    @Resource
    private PropertyDao propertyDao;
    @Resource
    private PropertyKeyDao propertyKeyDao;
    @Resource
    private PropertyItemDao propertyItemDao;

    @Override
    public void removeProperty(String pname) throws Exception {
        PropertyKeyDo d = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        List<PropertyDo> properties = propertyDao.findAllByKey(d.getId(), PropertyEntity.READSET_FULL);

        PropertyItemDo[] pitems = new PropertyItemDo[properties.size()];
        for (int i = 0; i < pitems.length; i++) {
            pitems[i] = new PropertyItemDo().setPropertyId(properties.get(i).getId());
        }
        propertyItemDao.deleteByProperty(pitems);
        propertyDao.delete(properties.toArray(new PropertyDo[properties.size()]));
        propertyKeyDao.delete(d);
    }

    @Override
    public void renameProperty(String originPname, String updatedPname) throws Exception {
        PropertyKeyDo kd = propertyKeyDao.findByName(originPname, PropertyKeyEntity.READSET_FULL);
        if (kd == null) return;
        propertyKeyDao.update(kd.setName(updatedPname), PropertyKeyEntity.UPDATESET_FULL);
    }

    @Override
    public boolean set(String pname, String pvalue, String type, Long itemId) throws Exception {
        PropertyKeyDo pkd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (pkd == null) {
            pkd = new PropertyKeyDo().setName(pname);
            propertyKeyDao.insert(pkd);
        }

        Set<Long> properties = new HashSet<>();
        PropertyDo targetProperty = null;
        for (PropertyDo e : propertyDao.findAllByKey(pkd.getId(), PropertyEntity.READSET_FULL)) {
            properties.add(e.getId());
            if (e.getPropertyValue().equals(pvalue)) {
                targetProperty = e;
            }
        }
        if (targetProperty == null) {
            targetProperty = new PropertyDo().setPropertyKeyId(pkd.getId()).setPropertyValue(pvalue);
            propertyDao.insert(targetProperty);
        }

        List<PropertyItemDo> items = propertyItemDao.findAllByItemAndProperties(itemId, properties.toArray(new Long[properties.size()]), PropertyItemEntity.READSET_FULL);
        PropertyItemDo d = null;
        Iterator<PropertyItemDo> iter = items.iterator();
        while (iter.hasNext()) {
            PropertyItemDo n = iter.next();
            if (n.getType().equals(type)) {
                d = n;
                iter.remove();
                break;
            } else {
                iter.remove();
            }
        }

        if (items.size() > 0) {
            propertyItemDao.deleteById(items.toArray(new PropertyItemDo[items.size()]));
        }
        if (d == null) {
            propertyItemDao.insert(new PropertyItemDo().setPropertyId(targetProperty.getId()).setItemId(itemId).setType(type));
        } else {
            if (d.getPropertyId() == targetProperty.getId()) return false;

            d.setPropertyId(targetProperty.getId());
            propertyItemDao.update(d, PropertyItemEntity.UPDATESET_FULL);
        }
        return true;
    }

    @Override
    public boolean clear(String pname, String pvalue, String type, Long itemId) throws Exception {
        PropertyKeyDo pkd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (pkd == null) return false;

        PropertyDo pd = propertyDao.findByKeyAndValue(pkd.getId(), pvalue, PropertyEntity.READSET_FULL);
        if (pd == null) return false;

        PropertyItemDo pid = propertyItemDao.findByPropertyAndItem(pd.getId(), itemId, type, PropertyItemEntity.READSET_FULL);
        if (pid == null) return false;

        propertyItemDao.deleteById(pid);
        return true;
    }
}
