package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.tag.PropertyService;
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
    public List<Long> query(String pname, String pvalue, String type) throws Exception {
        List<Long> result = new ArrayList<>();
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null)
            return result;
        PropertyDo d = propertyDao.findByKeyAndValue(kd.getId(), pvalue, PropertyEntity.READSET_FULL);
        if (d == null)
            return result;
        for (PropertyItemDo propertyItemDo : propertyItemDao.findAllByPropertyAndType(d.getId(), type, PropertyItemEntity.READSET_FULL)) {
            result.add(propertyItemDo.getItemId());
        }
        return result;
    }

    @Override
    public List<Long> query(String pname, String type) throws Exception {
        List<Long> result = new ArrayList<>();
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null)
            return result;
        List<PropertyDo> l = propertyDao.findAllByKey(kd.getId(), PropertyEntity.READSET_FULL);
        if (l.size() == 0)
            return result;
        Long[] pids = new Long[l.size()];
        for (int i = 0; i < l.size(); i++) {
            pids[i] = l.get(i).getPropertyKeyId();
        }
        for (PropertyItemDo propertyItemDo : propertyItemDao.findAllByPropertiesAndType(pids, type, PropertyItemEntity.READSET_FULL)) {
            result.add(propertyItemDo.getItemId());
        }
        return result;
    }

    @Override
    public List<String> getProperties(String type, Long itemId) throws Exception {
        // get all propertyIds from item table
        List<PropertyItemDo> list = propertyItemDao.findAllByItemAndType(itemId, type, PropertyItemEntity.READSET_FULL);
        List<String> result = new ArrayList<>();
        if (list.size() == 0)
            return result;
        Long[] pids = new Long[result.size()];
        for (int i = 0; i < list.size(); i++) {
            pids[i] = list.get(i).getPropertyId();
        }

        // get all properties from property table, and store keyIds
        List<PropertyDo> pref = propertyDao.findAllByIds(pids, PropertyEntity.READSET_FULL);
        if (pref.size() == 0)
            return result;
        Map<Long, String> pkeys = new HashMap<>();
        for (PropertyDo propertyDo : pref) {
            pkeys.put(propertyDo.getPropertyKeyId(), "");
        }
        // fetch key info from property key table
        Long[] pkids = new Long[pkeys.keySet().size()];
        int i = 0;
        for (Long pkid : pkeys.keySet()) {
            pkids[i++] = pkid;
        }
        for (PropertyKeyDo propertyKeyDo : propertyKeyDao.findAllByIds(pkids, PropertyKeyEntity.READSET_FULL)) {
            pkeys.put(propertyKeyDo.getId(), propertyKeyDo.getName());
        }

        // combine key value
        for (PropertyDo propertyDo : pref) {
            result.add("(" + pkeys.get(propertyDo.getPropertyKeyId()) + ", " + propertyDo.getPropertyValue() + ")");
        }
        return result;
    }
}
