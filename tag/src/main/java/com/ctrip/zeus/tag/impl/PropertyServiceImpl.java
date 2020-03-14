package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
import com.ctrip.zeus.model.Property;
//import com.ctrip.zeus.service.query.command.PropQueryCommand;
//import com.ctrip.zeus.service.query.command.QueryCommand;
//import com.ctrip.zeus.service.query.filter.FilterSet;
//import com.ctrip.zeus.service.query.filter.QueryExecuter;
import com.ctrip.zeus.tag.PropertyService;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component("propertyService")
public class PropertyServiceImpl implements PropertyService {
    @Resource
    private TagPropertyMapper tagPropertyMapper;
    @Resource
    private TagPropertyItemRMapper tagPropertyItemRMapper;

    private DynamicIntProperty interval = DynamicPropertyFactory.getInstance().getIntProperty("property.service.getpids.interval", 5);
    private DynamicIntProperty step = DynamicPropertyFactory.getInstance().getIntProperty("property.service.getpids.step", 1000);


    @Override
    public Set<Long> queryByType(String type) throws Exception {
        Set<Long> propIds = new HashSet<>();
        if (type == null) {
            return propIds;
        }
        for (TagPropertyItemR d : tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria()
                .andTypeEqualTo(type).example())) {
            propIds.add(d.getPropertyId());
        }
        return propIds;
    }

    @Override
    public List<Property> getProperties(Long[] propIds) throws Exception {
        List<Property> result = new ArrayList<>();
        if (propIds == null || propIds.length == 0) {
            return result;
        }
        List<TagProperty> props = tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria()
                .andIdIn(Arrays.asList(propIds)).example());
        for (TagProperty p : props) {
            String k = p.getPropertyName();
            String v = p.getPropertyValue();
            if (k != null && v != null) {
                result.add(new Property().setName(k).setValue(v));
            }
        }
        return result;
    }

    @Override
    public Property getProperty(String pname, Long itemId, String type) {
        if (pname == null || itemId == null || type == null) {
            return null;
        }
        List<Long> propertyIds = new ArrayList<>();
        for (TagPropertyItemR e : tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria()
                .andItemIdEqualTo(itemId).andTypeEqualTo(type).example())) {
            propertyIds.add(e.getPropertyId());
        }
        if (propertyIds.isEmpty()) return null;
        TagProperty e = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                .andIdIn(propertyIds).andPropertyNameEqualTo(pname).example());
        if (e != null) {
            return new Property().setName(pname).setValue(e.getPropertyValue());
        }
        return null;
    }

    @Override
    public String getPropertyValue(String pname, Long itemId, String type, String defaultValue) {
        Property property = getProperty(pname, itemId, type);
        return property != null ? property.getValue() : defaultValue;
    }

    @Override
    public Set<Long> unionQuery(List<Property> properties, String type) throws Exception {

        Set<Long> result = new HashSet<>();
        if (properties == null || properties.isEmpty() || type == null) {
            return result;
        }
        Map<String, Long> pids = findPidsByProperties(properties);
        if (pids.values().isEmpty()) {
            return result;
        }
        for (TagPropertyItemR e : tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria()
                .andPropertyIdIn(new ArrayList<>(pids.values())).example())) {
            if (e.getType().equalsIgnoreCase(type)) {
                result.add(e.getItemId());
            }
        }
        return result;
    }

    @Override
    public Set<Long> joinQuery(List<Property> properties, String type) throws Exception {
        Set<Long> result = new HashSet<>();
        if (properties == null || properties.isEmpty() || type == null) {
            return result;
        }

        Map<String, Long> pids = findPidsByProperties(properties);
        Long obj = 0L;
        for (Long l : pids.values()) {
            if (l.equals(obj)) return result;
        }

        if (pids.values().isEmpty()) {
            return result;
        }
        int joinedValue = pids.size();
        Map<Long, Set<Long>> marker = new HashMap<>();
        for (TagPropertyItemR e : tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria()
                .andPropertyIdIn(new ArrayList<>(pids.values())).example())) {
            if (e.getType().equalsIgnoreCase(type)) {
                Set<Long> v = marker.get(e.getItemId());
                if (v == null) {
                    v = new HashSet<>();
                    marker.put(e.getItemId(), v);
                }
                v.add(e.getPropertyId());
            }
        }
        for (Map.Entry<Long, Set<Long>> e : marker.entrySet()) {
            if (e.getValue().size() == joinedValue) result.add(e.getKey());
        }
        return result;
    }

    private Map<String, Long> findPidsByProperties(List<Property> properties) {
        Set<String> pnames = new HashSet<>();
        Map<String, Long> pids = new HashMap<>();
        Long obj = 0L;
        if (properties.size() > interval.get()) {
            for (Property p : properties) {
                pnames.add(p.getName());
                pids.put(p.getName() + ":" + p.getValue(), obj);
            }

            for (TagProperty e : tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria()
                    .andPropertyNameIn(new ArrayList<>(pnames)).example())) {
                String k = e.getPropertyName() + ":" + e.getPropertyValue();
                if (pids.containsKey(k)) {
                    pids.put(k, e.getId());
                }
            }
        } else {
            for (Property property : properties) {
                pids.put(property.getName() + ":" + property.getValue(), obj);
                TagProperty e = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                        .andPropertyNameEqualTo(property.getName()).andPropertyValueEqualTo(property.getValue()).example());
                if (e != null) {
                    String k = e.getPropertyName() + ":" + e.getPropertyValue();
                    if (pids.containsKey(k)) {
                        pids.put(k, e.getId());
                    }
                }
            }
        }

        return pids;

    }


    @Override
    public Set<Long> queryTargets(String pname, String type) throws Exception {
        Set<Long> result = new HashSet<>();
        if (pname == null || type == null) return result;

        List<Long> propIds = new ArrayList<>();
        for (TagProperty d : tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria().andPropertyNameEqualTo(pname).example())) {
            propIds.add(d.getId());
        }
        if (propIds.isEmpty()) return result;

        for (TagPropertyItemR d : getTagPropertyItemsByPropIds(type, propIds)) {
            result.add(d.getItemId());
        }
        return result;
    }

    @Override
    public List<Long> queryTargets(String pname, String pvalue, String type) throws Exception {
        List<Long> result = new ArrayList<>();
        if (pname == null || pvalue == null || type == null) return result;
        TagProperty d = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                .andPropertyNameEqualTo(pname).andPropertyValueEqualTo(pvalue).example());
        if (d == null) return result;

        for (TagPropertyItemR propertyItemDo : tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria()
                .andPropertyIdEqualTo(d.getId()).andTypeEqualTo(type).example())) {
            result.add(propertyItemDo.getItemId());
        }
        return result;
    }

    @Override
    public Map<Property, List<Long>> queryTargetGroup(String pname, String type) throws Exception {
        Map<Property, List<Long>> result = new HashMap<>();
        if (pname == null || type == null) return result;

        Map<Long, Property> props = new HashMap<>();
        for (TagProperty d : tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria().andPropertyNameEqualTo(pname).example())) {
            props.put(d.getId(), new Property().setName(d.getPropertyName()).setValue(d.getPropertyValue()));
        }
        if (props.size() == 0) return result;

        Map<Long, List<Long>> rPropItems = new HashMap<>();
        for (TagPropertyItemR d : getTagPropertyItemsByPropIds(type, new ArrayList<>(props.keySet()))) {
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
        Map<Long, List<Property>> result = new HashMap<>();
        if (type == null || itemIds == null || itemIds.length == 0) return result;
        Map<Long, List<Long>> rItemProp = new HashMap<>();
        Set<Long> propIds = new HashSet<>();

        for (TagPropertyItemR d : getTagPropertyItems(type, Arrays.asList(itemIds))) {
            propIds.add(d.getPropertyId());
            List<Long> l = rItemProp.get(d.getItemId());
            if (l == null) {
                l = new ArrayList<>();
                rItemProp.put(d.getItemId(), l);
            }
            l.add(d.getPropertyId());
        }
        if (propIds.isEmpty()) return new HashMap<>();

        Map<Long, Property> rIdProp = new HashMap<>();
        for (TagProperty d : tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria()
                .andIdIn(new ArrayList<>(propIds)).example())) {
            String key = d.getPropertyName();
            if (key != null) {
                rIdProp.put(d.getId(), new Property().setName(key).setValue(d.getPropertyValue()));
            }
        }

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
    public Map<Long, Property> getProperties(String pname, String type, Long[] itemIds) throws Exception {
        Map<Long, Property> result = new HashMap<>();
        if (pname == null || type == null || itemIds == null || itemIds.length == 0) {
            return result;
        }

        List<TagPropertyItemR> items = getTagPropertyItems(type, Arrays.asList(itemIds));

        List<TagProperty> propertyDos = tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria()
                .andPropertyNameEqualTo(pname).example());
        Map<Long, String> propertyMap = new HashMap<>();
        for (TagProperty pd : propertyDos) {
            propertyMap.put(pd.getId(), pd.getPropertyValue());
        }

        for (TagPropertyItemR pid : items) {
            if (propertyMap.containsKey(pid.getPropertyId())) {
                result.put(pid.getItemId(), new Property().setName(pname).setValue(propertyMap.get(pid.getPropertyId())));
            }
        }
        return result;
    }

    @Override
    public List<Property> getAllProperties() throws Exception {
        List<Property> result = new ArrayList<>();
        for (TagProperty e : tagPropertyMapper.selectByExample(new TagPropertyExample())) {
            if (e.getPropertyName() != null) {
                result.add(new Property().setName(e.getPropertyName()).setValue(e.getPropertyValue()));
            }
        }
        return result;
    }

    @Override
    public List<Property> getProperties(String type, Long itemId) throws Exception {
        if (type == null || itemId == null) return new ArrayList<>();
        List<Property> result = new ArrayList<>();
        List<TagPropertyItemR> items = tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria().andTypeEqualTo(type)
                .andItemIdEqualTo(itemId).example());
        if (items == null || items.isEmpty())
            return result;

        List<Long> pids = new ArrayList<>();
        for (TagPropertyItemR e : items) {
            if (e.getType().equalsIgnoreCase(type)) {
                pids.add(e.getPropertyId());
            }
        }
        if (pids.isEmpty()) return result;
        List<TagProperty> properties = tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria().andIdIn(pids).example());
        if (properties == null || properties.isEmpty())
            return new ArrayList<>();
        for (TagProperty property : properties) {
            result.add(new Property().setName(property.getPropertyName()).setValue(property.getPropertyValue()));
        }
        return result;
    }

    List<TagPropertyItemR> getTagPropertyItems(String type, List<Long> itemIds) {
        List<TagPropertyItemR> result = new ArrayList<>();
        int step = this.step.get();
        for (int i = 0; i < itemIds.size() / step + 1; i++) {
            int start = i * step;
            int end = (i + 1) * step > itemIds.size() ? itemIds.size() : (i + 1) * step;
            List<Long> subList = itemIds.subList(start, end);
            if (subList.size() == 0) {
                continue;
            }
            List<TagPropertyItemR> items = tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria().andTypeEqualTo(type)
                    .andItemIdIn(subList).example());
            if (items != null && items.size() > 0) {
                result.addAll(items);
            }
        }
        return result;
    }

    List<TagPropertyItemR> getTagPropertyItemsByPropIds(String type, List<Long> pIds) {
        List<TagPropertyItemR> result = new ArrayList<>();
        int step = this.step.get();
        for (int i = 0; i < pIds.size() / step + 1; i++) {
            int start = i * step;
            int end = (i + 1) * step > pIds.size() ? pIds.size() : (i + 1) * step;
            List<Long> subList = pIds.subList(start, end);
            if (subList.size() == 0) {
                continue;
            }
            List<TagPropertyItemR> items = tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria().andTypeEqualTo(type)
                    .andPropertyIdIn(subList).example());
            if (items != null && items.size() > 0) {
                result.addAll(items);
            }
        }
        return result;
    }
}
