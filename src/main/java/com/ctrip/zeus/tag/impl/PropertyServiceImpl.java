package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.query.command.PropQueryCommand;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.entity.Property;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

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
    public Set<Long> queryByCommand(QueryCommand command, String type) throws Exception {
        Set<Long> result = null;
        PropQueryCommand propQuery = (PropQueryCommand) command;
        while (propQuery != null) {
            if (propQuery.hasValue(propQuery.union_prop)) {
                if (result == null) {
                    result = unionQuery(propQuery.getProperties(propQuery.union_prop), type);
                } else {
                    result.retainAll(unionQuery(propQuery.getProperties(propQuery.union_prop), type));
                }
            }
            if (propQuery.hasValue(propQuery.join_prop)) {
                if (result == null) {
                    result = joinQuery(propQuery.getProperties(propQuery.join_prop), type);
                } else {
                    result.retainAll(joinQuery(propQuery.getProperties(propQuery.join_prop), type));
                }
            }
            propQuery = propQuery.next();
        }
        return result;
    }

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
    public Set<Long> unionQuery(List<Property> properties, String type) throws Exception {
        Set<String> pnames = new HashSet<>();
        Map<String, Long> pids = new HashMap<>();
        Long obj = 0L;
        for (Property p : properties) {
            pnames.add(p.getName());
            pids.put(p.getName() + ":" + p.getValue(), obj);
        }

        Map<Long, String> pnameRef = new HashMap<>();
        for (PropertyKeyDo e : propertyKeyDao.findByNames(pnames.toArray(new String[pnames.size()]), PropertyKeyEntity.READSET_FULL)) {
            pnameRef.put(e.getId(), e.getName());
        }
        for (PropertyDo e : propertyDao.findAllByKeys(pnameRef.keySet().toArray(new Long[pnameRef.size()]), PropertyEntity.READSET_FULL)) {
            String k = pnameRef.get(e.getPropertyKeyId()) + ":" + e.getPropertyValue();
            if (pids.containsKey(k)) {
                pids.put(k, e.getId());
            }
        }

        Set<Long> result = new HashSet<>();
        for (PropertyItemDo e : propertyItemDao.findAllByProperties(pids.values().toArray(new Long[0]), PropertyItemEntity.READSET_FULL)) {
            if (e.getType().equals(type)) {
                result.add(e.getItemId());
            }
        }
        return result;
    }

    @Override
    public Set<Long> joinQuery(List<Property> properties, String type) throws Exception {
        Set<String> pnames = new HashSet<>();
        Map<String, Long> pids = new HashMap<>();
        Long obj = 0L;
        for (Property p : properties) {
            pnames.add(p.getName());
            pids.put(p.getName() + ":" + p.getValue(), obj);
        }

        Map<Long, String> pnameRef = new HashMap<>();
        for (PropertyKeyDo e : propertyKeyDao.findByNames(pnames.toArray(new String[pnames.size()]), PropertyKeyEntity.READSET_FULL)) {
            pnameRef.put(e.getId(), e.getName());
        }
        if (pnameRef.size() < pnames.size()) return new HashSet<>();

        for (PropertyDo e : propertyDao.findAllByKeys(pnameRef.keySet().toArray(new Long[pnameRef.size()]), PropertyEntity.READSET_FULL)) {
            String k = pnameRef.get(e.getPropertyKeyId()) + ":" + e.getPropertyValue();
            if (pids.containsKey(k)) {
                pids.put(k, e.getId());
            }
        }
        for (Long l : pids.values()) {
            if (l == obj) return new HashSet<>();
        }

        int joinedValue = pids.size();
        Map<Long, Counter> marker = new HashMap<>();
        for (PropertyItemDo e : propertyItemDao.findAllByProperties(pids.values().toArray(new Long[0]), PropertyItemEntity.READSET_FULL)) {
            if (e.getType().equals(type)) {
                Counter m = marker.get(e.getPropertyId());
                if (m == null) {
                    marker.put(e.getItemId(), new Counter());
                } else {
                    m.incr();
                }
            }
        }
        Set<Long> result = new HashSet<>();
        for (Map.Entry<Long, Counter> e : marker.entrySet()) {
            if (e.getValue().get() == joinedValue) result.add(e.getKey());
        }
        return result;
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
        for (PropertyDo e : propertyDao.findAllByKeys(keyRef.keySet().toArray(new Long[keyRef.size()]), PropertyEntity.READSET_FULL)) {
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

    private class Counter {
        int count = 1;

        public void incr() {
            count++;
        }

        public int get() {
            return count;
        }
    }
}
