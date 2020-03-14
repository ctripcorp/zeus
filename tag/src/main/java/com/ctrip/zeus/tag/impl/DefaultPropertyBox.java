package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dao.entity.TagPropertyItemRExample.*;
import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.*;
import com.ctrip.zeus.dao.mapper.TagPropertyMapper;
import com.ctrip.zeus.exceptions.TagValidationException;
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
    private TagPropertyMapper tagPropertyMapper;
    @Resource
    private TagPropertyItemRMapper tagPropertyItemRMapper;

    @Override
    public void removeProperty(String pname, boolean force) throws Exception {
        removePropertyMybatis(pname, force);
    }

    private void removePropertyMybatis(String pname, boolean force) throws Exception {
        List<TagProperty> properties = tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria().andPropertyNameEqualTo(pname).example());
        if (properties == null || properties.isEmpty()) return;

        List<Long> propertyIds = new ArrayList<>();
        for (int i = 0; i < properties.size(); i++) {
            propertyIds.add(properties.get(i).getId());
        }

        if (force) {
            tagPropertyItemRMapper.deleteByExample(new TagPropertyItemRExample().createCriteria().andPropertyIdIn(propertyIds).example());
        } else {
            List<TagPropertyItemR> check = tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria().andPropertyIdIn(propertyIds).example());
            if (check != null && !check.isEmpty()) {
                throw new TagValidationException("Dependency exists with property named - " + pname + ".");
            }
        }
        tagPropertyMapper.deleteByExample(new TagPropertyExample().createCriteria().andIdIn(propertyIds).example());
    }

    @Override
    public void renameProperty(String originPname, String updatedPname) throws Exception {
        if (originPname == null) return;
        TagProperty record = new TagProperty();
        record.setPropertyName(updatedPname);
        record.setDatachangeLasttime(null);
        tagPropertyMapper.updateByExampleSelective(record, new TagPropertyExample().createCriteria().andPropertyNameEqualTo(originPname).example());
    }

    @Override
    public boolean set(String pname, String pvalue, String type, Long itemId) throws Exception {
        Set<Long> properties = new HashSet<>();
        TagProperty targetProperty = null;
        for (TagProperty e : tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria().andPropertyNameEqualTo(pname).example())) {
            properties.add(e.getId());
            if (e.getPropertyValue().equalsIgnoreCase(pvalue)) {
                targetProperty = e;
            }
        }
        if (targetProperty == null) {
            targetProperty = new TagProperty();
            targetProperty.setPropertyName(pname);
            targetProperty.setPropertyValue(pvalue);
            tagPropertyMapper.upsert(targetProperty);
            targetProperty = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                    .andPropertyNameEqualTo(pname).
                            andPropertyValueEqualTo(pvalue).
                            example());

            TagPropertyItemR record = new TagPropertyItemR();
            record.setPropertyId(targetProperty.getId());
            record.setItemId(itemId);
            record.setType(type.toLowerCase());
            tagPropertyItemRMapper.insertSelective(record);
        } else {
            Criteria criteria = new TagPropertyItemRExample().createCriteria()
                    .andItemIdEqualTo(itemId).
                            andTypeEqualTo(type).
                            andPropertyIdIn(new ArrayList<>(properties));
            List<TagPropertyItemR> items = tagPropertyItemRMapper.selectByExample(criteria.example());
            if (items.size() <= 1) {
                TagPropertyItemR record = new TagPropertyItemR();
                if (items.size() == 1) {
                    record.setId(items.get(0).getId());
                }
                record.setPropertyId(targetProperty.getId());
                record.setItemId(itemId);
                record.setType(type.toLowerCase());
                tagPropertyItemRMapper.upsert(record);
            } else {
                List<Long> pids = new ArrayList<>();
                for (TagPropertyItemR item : items) {
                    pids.add(item.getPropertyId());
                }
                Criteria c = new TagPropertyItemRExample().createCriteria()
                        .andItemIdEqualTo(itemId).
                                andTypeEqualTo(type).
                                andPropertyIdIn(pids);
                tagPropertyItemRMapper.deleteByExample(c.example());

                TagPropertyItemR record = new TagPropertyItemR();
                record.setPropertyId(targetProperty.getId());
                record.setItemId(itemId);
                record.setType(type.toLowerCase());
                tagPropertyItemRMapper.insertSelective(record);
            }
        }
        return true;
    }

    @Override
    public void set(String pname, String pvalue, String type, Long[] itemIds) throws Exception {
        Set<Long> properties = new HashSet<>();
        TagProperty targetProperty = null;
        for (TagProperty e : tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria().andPropertyNameEqualTo(pname).example())) {
            properties.add(e.getId());
            if (e.getPropertyValue().equals(pvalue)) {
                targetProperty = e;
            }
        }
        if (targetProperty == null) {
            targetProperty = new TagProperty();
            targetProperty.setPropertyName(pname);
            targetProperty.setPropertyValue(pvalue);
            tagPropertyMapper.upsert(targetProperty);
            targetProperty = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                    .andPropertyNameEqualTo(pname).andPropertyValueEqualTo(pvalue).example());
        }

        List<TagPropertyItemR> adding = new ArrayList<>();
        List<TagPropertyItemR> removing = new ArrayList<>();
        List<TagPropertyItemR> updating = new ArrayList<>();

        Set<Long> uniqItemIds = new HashSet<>();
        for (Long itemId : itemIds) {
            uniqItemIds.add(itemId);
        }

        Map<Long, TagPropertyItemR> items = new HashMap<>();
        if (properties.size() > 0) {
            for (TagPropertyItemR e : tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria().andPropertyIdIn(new ArrayList<>(properties)).example())) {
                if (uniqItemIds.contains(e.getItemId())) {
                    if (e.getType().equalsIgnoreCase(type)) {
                        TagPropertyItemR dup = items.put(e.getItemId(), e);
                        if (dup != null) {
                            removing.add(dup);
                        }
                    }
                }
            }
        }

        for (Long itemId : uniqItemIds) {
            TagPropertyItemR d = items.get(itemId);
            if (d == null) {
                TagPropertyItemR tagPropertyItemR = new TagPropertyItemR();
                tagPropertyItemR.setPropertyId(targetProperty.getId());
                tagPropertyItemR.setItemId(itemId);
                tagPropertyItemR.setType(type.toLowerCase());
                adding.add(tagPropertyItemR);
            } else {
                if (d.getPropertyId().longValue() == targetProperty.getId().longValue()) continue;

                d.setPropertyId(targetProperty.getId());
                updating.add(d);
            }
        }

        if (adding.size() > 0) {
            tagPropertyItemRMapper.batchInsert(adding);
        }
        if (removing.size() > 0) {
            tagPropertyItemRMapper.deleteByProperty(removing);
        }
        for (TagPropertyItemR u : updating) {
            u.setDatachangeLasttime(null);
            tagPropertyItemRMapper.updateByPrimaryKey(u);
        }
    }

    @Override
    public boolean clear(String type, Long itemId) throws Exception {
        List<TagPropertyItemR> list = tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria().andItemIdEqualTo(itemId).andTypeEqualTo(type).example());
        if (list.size() > 0) {
            tagPropertyItemRMapper.deleteByProperty(list);
            return true;
        }
        return false;
    }

    @Override
    public boolean clear(String pname, String type, Long itemId) throws Exception {
        List<TagProperty> pds = tagPropertyMapper.selectByExample(new TagPropertyExample().createCriteria().andPropertyNameEqualTo(pname).example());
        if (pds == null || pds.isEmpty()) return false;

        List<TagPropertyItemR> pids = tagPropertyItemRMapper.selectByExample(new TagPropertyItemRExample().createCriteria()
                .andItemIdEqualTo(itemId).andTypeEqualTo(type).example());
        if (pids == null || pids.isEmpty()) return false;
        List<Long> pdIds = new ArrayList<>();
        for (TagProperty pd : pds) {
            pdIds.add(pd.getId());
        }
        List<TagPropertyItemR> toBeDelete = new ArrayList<>();
        for (TagPropertyItemR pid : pids) {
            if (pdIds.contains(pid.getPropertyId())) {
                toBeDelete.add(pid);
            }
        }
        if (toBeDelete.size() > 0) {
            tagPropertyItemRMapper.deleteByProperty(toBeDelete);
        }
        return true;
    }


    @Override
    public boolean clear(String pname, String pvalue, String type, Long itemId) throws Exception {
        TagProperty pd = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                .andPropertyNameEqualTo(pname).andPropertyValueEqualTo(pvalue).example());
        if (pd == null) return false;

        TagPropertyItemR pid = tagPropertyItemRMapper.selectOneByExample(new TagPropertyItemRExample().createCriteria()
                .andPropertyIdEqualTo(pd.getId()).andItemIdEqualTo(itemId).andTypeEqualTo(type).example());
        if (pid == null) return false;

        tagPropertyItemRMapper.deleteByPrimaryKey(pid.getId());

        return true;
    }

    @Override
    public void clear(String pname, String pvalue, String type, Long[] itemIds) throws Exception {
        TagProperty pd = tagPropertyMapper.selectOneByExample(new TagPropertyExample().createCriteria()
                .andPropertyNameEqualTo(pname).andPropertyValueEqualTo(pvalue).example());
        if (pd == null) return;

        List<TagPropertyItemR> removing = new ArrayList<>();
        for (Long itemId : itemIds) {
            TagPropertyItemR pid = tagPropertyItemRMapper.selectOneByExample(new TagPropertyItemRExample().createCriteria()
                    .andPropertyIdEqualTo(pd.getId()).andItemIdEqualTo(itemId).andTypeEqualTo(type).example());
            if (pid == null) continue;

            removing.add(pid);
        }

        if (removing.size() > 0) {
            tagPropertyItemRMapper.deleteByProperty(removing);
        }
    }
}
