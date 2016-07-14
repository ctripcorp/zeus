package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.entity.Property;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhoumy on 2015/7/21.
 */
@Component("propertyService")
public class PropertyServiceImpl implements PropertyService {
    @Resource
    private PropertyKeyDao propertyKeyDao;
    @Resource
    private PropertyDao propertyDao;
    @Resource
    private PropertyItemDao propertyItemDao;

    @Override
    public Property getProperty(String pname, Long itemId, String type) throws Exception {
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null) return null;

        List<Long> propertyIds = new ArrayList<>();
        for (PropertyItemDo e : propertyItemDao.findAllByItemAndType(itemId, type, PropertyItemEntity.READSET_FULL)) {
            propertyIds.add(e.getPropertyId());
        }
        for (PropertyDo e : propertyDao.findAllByIds(propertyIds.toArray(new Long[propertyIds.size()]), PropertyEntity.READSET_FULL)) {
            if (e.getPropertyKeyId() == kd.getId()) {
                return new Property().setName(pname).setValue(e.getPropertyValue());
            }
        }
        return null;
    }

    @Override
    public List<Long> queryTargets(String pname, String pvalue, String type) throws Exception {
        List<Long> result = new ArrayList<>();
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null) return result;
        PropertyDo d = propertyDao.findByKeyAndValue(kd.getId(), pvalue, PropertyEntity.READSET_FULL);
        if (d == null) return result;

        for (PropertyItemDo propertyItemDo : propertyItemDao.findAllByPropertyAndType(d.getId(), type, PropertyItemEntity.READSET_FULL)) {
            result.add(propertyItemDo.getItemId());
        }
        return result;
    }

    @Override
    public List<Property> getAllProperties() throws Exception {
        List<Property> result = new ArrayList<>();
        Map<Long, String> keyRef = new HashMap<>();

        for (PropertyKeyDo e : propertyKeyDao.findAll(PropertyKeyEntity.READSET_FULL)) {
            keyRef.put(e.getId(), e.getName());
        }
        for (PropertyDo e : propertyDao.findAllByIds(keyRef.keySet().toArray(new Long[keyRef.size()]), PropertyEntity.READSET_FULL)) {
            String kn = keyRef.get(e.getPropertyKeyId());
            if (kn != null) {
                result.add(new Property().setName(kn).setValue(e.getPropertyValue()));
            }
        }
        return result;
    }

    @Override
    public List<Property> getProperties(String type, Long itemId) throws Exception {
        List<PropertyItemDo> items = propertyItemDao.findAllByItemAndType(itemId, type, PropertyItemEntity.READSET_FULL);
        if (items.size() == 0)
            return new ArrayList<>();

        List<Long> pids = new ArrayList<>();
        for (PropertyItemDo e : items) {
            if (e.getType().equals(type)) {
                pids.add(e.getPropertyId());
            }
        }
        List<PropertyDo> properties = propertyDao.findAllByIds(pids.toArray(new Long[pids.size()]), PropertyEntity.READSET_FULL);
        if (properties.size() == 0)
            return new ArrayList<>();

        Map<Long, Property> result = new HashMap<>();
        for (PropertyDo e : properties) {
            result.put(e.getPropertyKeyId(), new Property().setValue(e.getPropertyValue()));
        }

        for (PropertyKeyDo e : propertyKeyDao.findAllByIds(result.keySet().toArray(new Long[result.size()]), PropertyKeyEntity.READSET_FULL)) {
            result.get(e.getId()).setName(e.getName());
        }
        return new ArrayList<>(result.values());
    }
}
