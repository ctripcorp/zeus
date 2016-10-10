package com.ctrip.zeus.tag.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
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
    public void removeProperty(String pname, boolean force) throws Exception {
        PropertyKeyDo d = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (d == null) return;

        List<PropertyDo> properties = propertyDao.findAllByKey(d.getId(), PropertyEntity.READSET_FULL);
        if (properties.size() == 0) return;

        PropertyItemDo[] pitems = new PropertyItemDo[properties.size()];
        for (int i = 0; i < pitems.length; i++) {
            pitems[i] = new PropertyItemDo().setPropertyId(properties.get(i).getId());
        }

        if (force) {
            propertyItemDao.deleteByProperty(pitems);
        } else {
            Long[] propertyIds = new Long[properties.size()];
            for (int i = 0; i < propertyIds.length; i++) {
                propertyIds[i] = properties.get(i).getId();
            }
            List<PropertyItemDo> check = propertyItemDao.findAllByProperties(propertyIds, PropertyItemEntity.READSET_FULL);
            if (check.size() > 0) {
                throw new ValidationException("Dependency exists with property named - " + pname + ".");
            }
        }
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
    public void set(String pname, String pvalue, String type, Long[] itemIds) throws Exception {
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

        List<PropertyItemDo> adding = new ArrayList<>();
        List<PropertyItemDo> removing = new ArrayList<>();
        List<PropertyItemDo> updating = new ArrayList<>();

        Set<Long> uniqItemIds = new HashSet<>();
        for (Long itemId : itemIds) {
            uniqItemIds.add(itemId);
        }

        Map<Long, PropertyItemDo> items = new HashMap<>();
        for (PropertyItemDo e : propertyItemDao.findAllByProperties(properties.toArray(new Long[properties.size()]), PropertyItemEntity.READSET_FULL)) {
            if (uniqItemIds.contains(e.getItemId())) {
                if (e.getType().equals(type)) {
                    PropertyItemDo dup = items.put(e.getItemId(), e);
                    if (dup != null) {
                        removing.add(dup);
                    }
                }
            }
        }

        for (Long itemId : uniqItemIds) {
            PropertyItemDo d = items.get(itemId);
            if (d == null) {
                adding.add(new PropertyItemDo().setPropertyId(targetProperty.getId()).setItemId(itemId).setType(type));

            } else {
                if (d.getPropertyId() == targetProperty.getId()) continue;

                d.setPropertyId(targetProperty.getId());
                updating.add(d);
            }
        }

        if (adding.size() > 0) {
            propertyItemDao.insert(adding.toArray(new PropertyItemDo[adding.size()]));
        }
        if (removing.size() > 0) {
            propertyItemDao.deleteById(removing.toArray(new PropertyItemDo[removing.size()]));
        }
        if (updating.size() > 0) {
            propertyItemDao.update(updating.toArray(new PropertyItemDo[updating.size()]), PropertyItemEntity.UPDATESET_FULL);
        }
    }

    @Override
    public boolean clear(String type, Long itemId) throws Exception {
        List<PropertyItemDo> list = propertyItemDao.findAllByItemAndType(itemId, type, PropertyItemEntity.READSET_FULL);
        if (list.size() > 0) {
            propertyItemDao.deleteById(list.toArray(new PropertyItemDo[list.size()]));
            return true;
        }
        return false;
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

    @Override
    public void clear(String pname, String pvalue, String type, Long[] itemIds) throws Exception {
        PropertyKeyDo pkd = propertyKeyDao.findByName(pname, PropertyKeyEntity.READSET_FULL);
        if (pkd == null) return;

        PropertyDo pd = propertyDao.findByKeyAndValue(pkd.getId(), pvalue, PropertyEntity.READSET_FULL);
        if (pd == null) return;

        List<PropertyItemDo> removing = new ArrayList<>();
        for (Long itemId : itemIds) {
            PropertyItemDo pid = propertyItemDao.findByPropertyAndItem(pd.getId(), itemId, type, PropertyItemEntity.READSET_FULL);
            if (pid == null) continue;

            removing.add(pid);
        }

        if (removing.size() > 0) {
            propertyItemDao.deleteById(removing.toArray(new PropertyItemDo[removing.size()]));
        }
    }
}
