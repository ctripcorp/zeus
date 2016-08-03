package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.query.command.PropQueryCommand;
import com.ctrip.zeus.service.query.command.QueryCommand;
import com.ctrip.zeus.service.query.filter.FilterSet;
import com.ctrip.zeus.service.query.filter.QueryExecuter;
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
    public Set<Long> queryByCommand(QueryCommand command, final String type) throws Exception {
        Set<Long> result = null;

        PropQueryCommand propQuery = (PropQueryCommand) command;
        while (propQuery != null) {
            final PropQueryCommand tmp = propQuery;
            Long[] filteredPropIds = new QueryExecuter.Builder<Long>()
                    .addFilter(new FilterSet<Long>() {
                        @Override
                        public boolean shouldFilter() throws Exception {
                            return tmp.hasValue(tmp.union_prop);
                        }

                        @Override
                        public Set<Long> filter() throws Exception {
                            return unionQuery(tmp.getProperties(tmp.union_prop), type);
                        }
                    })
                    .addFilter(new FilterSet<Long>() {
                        @Override
                        public boolean shouldFilter() throws Exception {
                            return tmp.hasValue(tmp.join_prop);
                        }

                        @Override
                        public Set<Long> filter() throws Exception {
                            return joinQuery(tmp.getProperties(tmp.join_prop), type);
                        }
                    })
                    .addFilter(new FilterSet<Long>() {
                        @Override
                        public boolean shouldFilter() throws Exception {
                            return tmp.hasValue(tmp.item_type);
                        }

                        @Override
                        public Set<Long> filter() throws Exception {
                            List<String> t = new ArrayList<>();
                            for (String s : tmp.getValue(tmp.item_type)) {
                                t.add(s.trim());
                            }
                            Set<Long> result = new HashSet<>();
                            for (String s : t) {
                                result.retainAll(queryByType(s));
                            }
                            return result;
                        }
                    }).build(Long.class).run();

            if (filteredPropIds != null) {
                if (filteredPropIds.length == 0) return new HashSet<>();

                Set<Long> tmpIds = new HashSet<>();
                for (Long i : filteredPropIds) {
                    tmpIds.add(i);
                }

                if (result == null) {
                    result = tmpIds;
                } else {
                    result.retainAll(tmpIds);
                }
            }
            propQuery = propQuery.next();
        }
        return result;
    }

    @Override
    public Set<Long> queryByType(String type) throws Exception {
        Set<Long> propIds = new HashSet<>();
        for (PropertyItemDo d : propertyItemDao.findAllByType(type, PropertyItemEntity.READSET_FULL)) {
            propIds.add(d.getPropertyId());
        }
        return propIds;
    }

    @Override
    public List<Property> getProperties(Long[] propIds) throws Exception {
        Map<Long, String> rIdName = new HashMap<>();
        Set<Long> propKeys = new HashSet<>();
        List<PropertyDo> props = propertyDao.findAllByIds(propIds, PropertyEntity.READSET_FULL);
        for (PropertyDo d : props) {
            propKeys.add(d.getPropertyKeyId());
            rIdName.put(d.getId(), d.getPropertyValue());
        }

        Map<Long, String> rKeyName = new HashMap<>();
        for (PropertyKeyDo d : propertyKeyDao.findAllByIds(propKeys.toArray(new Long[propKeys.size()]), PropertyKeyEntity.READSET_FULL)) {
            rKeyName.put(d.getId(), d.getName());
        }

        List<Property> result = new ArrayList<>();
        for (PropertyDo p : props) {
            String k = rKeyName.get(p.getPropertyKeyId());
            String v = rIdName.get(p.getId());
            if (k != null && v != null) {
                result.add(new Property().setName(k).setValue(v));
            }
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
    public Set<Long> queryTargets(String pname, String type) throws Exception {
        Set<Long> result = new HashSet<>();
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null) return result;

        List<Long> propIds = new ArrayList<>();
        for (PropertyDo d : propertyDao.findAllByKey(kd.getId(), PropertyEntity.READSET_FULL)) {
            propIds.add(d.getId());
        }
        if (propIds.size() == 0) return result;

        for (PropertyItemDo d : propertyItemDao.findAllByProperties(propIds.toArray(new Long[propIds.size()]), PropertyItemEntity.READSET_FULL)) {
            result.add(d.getItemId());
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
    public Map<Property, List<Long>> queryTargetGroup(String pname, String type) throws Exception {
        Map<Property, List<Long>> result = new HashMap<>();
        PropertyKeyDo kd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (kd == null) return result;

        Map<Long, Property> props = new HashMap<>();
        for (PropertyDo d : propertyDao.findAllByKey(kd.getId(), PropertyEntity.READSET_FULL)) {
            props.put(d.getId(), new Property().setName(kd.getName()).setValue(d.getPropertyValue()));
        }
        if (props.size() == 0) return result;

        Map<Long, List<Long>> rPropItems = new HashMap<>();
        for (PropertyItemDo d : propertyItemDao.findAllByPropertiesAndType(props.keySet().toArray(new Long[props.size()]), type, PropertyItemEntity.READSET_FULL)) {
            List<Long> l = rPropItems.get(d.getPropertyId());
            if (l == null) {
                l = new ArrayList<>();
                rPropItems.put(d.getPropertyId(), l);
            }
            l.add(d.getItemId());
        }
        for (Map.Entry<Long, List<Long>> e : rPropItems.entrySet()) {
            Property p = props.get(e.getKey());
            if (p == null) continue;
            result.put(p, e.getValue());
        }
        return result;
    }

    @Override
    public Map<Long, List<Property>> getProperties(String type, Long[] itemIds) throws Exception {
        Map<Long, List<Long>> rItemProp = new HashMap<>();
        Set<Long> propIds = new HashSet<>();

        for (PropertyItemDo d : propertyItemDao.findAllByItemsAndType(itemIds, type, PropertyItemEntity.READSET_FULL)) {
            propIds.add(d.getPropertyId());
            List<Long> l = rItemProp.get(d.getItemId());
            if (l == null) {
                l = new ArrayList<>();
                rItemProp.put(d.getItemId(), l);
            }
            l.add(d.getPropertyId());
        }

        if (propIds.size() == 0) return new HashMap<>();

        Map<Long, String> rKeyIdName = new HashMap<>();
        for (PropertyKeyDo d : propertyKeyDao.findAll(PropertyKeyEntity.READSET_FULL)) {
            rKeyIdName.put(d.getId(), d.getName());
        }

        Map<Long, Property> rIdProp = new HashMap<>();
        for (PropertyDo d : propertyDao.findAllByIds(propIds.toArray(new Long[propIds.size()]), PropertyEntity.READSET_FULL)) {
            String key = rKeyIdName.get(d.getPropertyKeyId());
            if (key != null) {
                rIdProp.put(d.getId(), new Property().setName(key).setValue(d.getPropertyValue()));
            }
        }

        Map<Long, List<Property>> result = new HashMap<>();
        for (Map.Entry<Long, List<Long>> e : rItemProp.entrySet()) {
            List<Property> l = new ArrayList<>();
            result.put(e.getKey(), l);
            for (Long i : e.getValue()) {
                Property p = rIdProp.get(i);
                if (p != null) l.add(p);
            }
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
